package io.universallogger.logback.appenders;

import ch.qos.logback.core.rolling.helper.ArchiveRemover;
import ch.qos.logback.core.spi.ContextAwareBase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * SizeAndTimeBasedRollingPolicy 전용 커스텀 ArchiveRemover
 *
 * <p>문제 배경:</p>
 * Logback 1.2.x의 {@code DefaultArchiveRemover}는 {@code TimeBasedRollingPolicy}를 위해
 * 설계되어 있어 {@code %i} 인덱스가 포함된 파일 패턴을 올바르게 처리하지 못합니다.
 * 결과적으로 {@code maxHistory} 설정값과 무관하게 오래된 로그 파일이 삭제되지 않습니다.
 * (Logback 1.3.x에서 {@code SizeAndTimeBasedArchiveRemover}로 수정됨)
 *
 * <p>해결 방법:</p>
 * 이 클래스는 파일 시스템에서 직접 아카이브 파일을 스캔하여 날짜 기반으로 삭제합니다.
 * {@code DefaultAppenderFactory}에서 reflection을 통해 {@code rollingPolicy.start()} 이후
 * {@code TimeBasedRollingPolicy.archiveRemover} 필드를 이 구현체로 교체합니다.
 *
 * <p>지원 파일 패턴:</p>
 * <ul>
 *   <li>일반: {@code application.log.2026-01-01.0}</li>
 *   <li>압축: {@code application.log.2026-01-01.0.gz}</li>
 * </ul>
 *
 * @see DefaultAppenderFactory
 */
public class SizeAndTimeBasedArchiveRemover extends ContextAwareBase implements ArchiveRemover {

    private static final Logger log = LoggerFactory.getLogger(SizeAndTimeBasedArchiveRemover.class);

    /** 날짜 포맷 (파일명 파싱용) */
    private static final String DATE_FORMAT = "yyyy-MM-dd";

    /** 현재 로그 파일 기본 경로 (예: C:/log/PHM_LIMS/application.log) */
    private final String baseFilePath;

    /** gz 압축 여부 */
    private final boolean compress;

    /** 아카이브 파일명 매칭 정규식 (날짜 그룹 캡처 포함) */
    private final Pattern archiveFilePattern;

    /** maxHistory 일수 (기본값 30) */
    private int maxHistory = 30;

    /** 전체 로그 용량 상한 (bytes, 0이면 미적용) */
    private long totalSizeCap = 0;

    /**
     * @param baseFilePath 현재 로그 파일 경로 (예: C:/log/PHM_LIMS/application.log)
     * @param compress     gz 압축 여부
     */
    public SizeAndTimeBasedArchiveRemover(String baseFilePath, boolean compress) {
        this.baseFilePath = baseFilePath;
        this.compress = compress;

        // 아카이브 파일명 패턴 생성
        // 예: ^application\.log\.(\d{4}-\d{2}-\d{2})\.\d+$
        // 압축 시: ^application\.log\.(\d{4}-\d{2}-\d{2})\.\d+\.gz$
        String baseName = Pattern.quote(new File(baseFilePath).getName());
        String patternStr = "^" + baseName
                + "\\.(\\d{4}-\\d{2}-\\d{2})"  // 날짜 캡처 그룹
                + "\\.\\d+"                      // %i 인덱스
                + (compress ? "\\.gz" : "")      // 압축 확장자
                + "$";
        this.archiveFilePattern = Pattern.compile(patternStr);
    }

    /**
     * 만료된 아카이브 파일 삭제
     *
     * 자정 롤오버 시 Logback 내부에서 호출됩니다.
     * {@code maxHistory}일 이전의 모든 아카이브 파일을 삭제합니다.
     *
     * @param now 현재 시각
     */
    @Override
    public void clean(Date now) {
        File baseFile = new File(baseFilePath);
        File parentDir = baseFile.getParentFile();

        if (parentDir == null || !parentDir.exists()) {
            log.warn("[UniversalLogger] 로그 디렉토리를 찾을 수 없습니다: {}", parentDir);
            return;
        }

        // 삭제 기준 날짜 계산 (now - maxHistory일, 해당 날짜 00:00:00 기준)
        Calendar cal = Calendar.getInstance();
        cal.setTime(now);
        cal.add(Calendar.DAY_OF_YEAR, -maxHistory);
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        Date cutoffDate = cal.getTime();

        // DateFormat은 스레드 비안전이므로 매 호출마다 생성
        DateFormat df = new SimpleDateFormat(DATE_FORMAT);

        File[] files = parentDir.listFiles();
        if (files == null) {
            return;
        }

        int deletedCount = 0;
        for (File file : files) {
            Matcher matcher = archiveFilePattern.matcher(file.getName());
            if (!matcher.matches()) {
                continue;
            }
            try {
                Date fileDate = df.parse(matcher.group(1));
                if (fileDate.before(cutoffDate)) {
                    if (file.delete()) {
                        log.debug("[UniversalLogger] 만료 로그 아카이브 삭제: {}", file.getAbsolutePath());
                        deletedCount++;
                    } else {
                        log.warn("[UniversalLogger] 로그 아카이브 삭제 실패: {}", file.getAbsolutePath());
                    }
                }
            } catch (ParseException e) {
                log.warn("[UniversalLogger] 파일명에서 날짜 파싱 실패: {}", file.getName());
            }
        }

        if (deletedCount > 0) {
            log.info("[UniversalLogger] 만료 로그 아카이브 {}개 삭제 완료 (maxHistory={}일)", deletedCount, maxHistory);
        }

        // totalSizeCap 초과 시 오래된 파일부터 추가 삭제
        if (totalSizeCap > 0) {
            capTotalSize(parentDir, df);
        }
    }

    /**
     * 전체 로그 용량 상한 초과 시 오래된 파일부터 삭제
     *
     * @param dir 로그 디렉토리
     * @param df  날짜 포맷터
     */
    private void capTotalSize(File dir, DateFormat df) {
        File[] files = dir.listFiles();
        if (files == null) {
            return;
        }

        // 아카이브 파일만 수집
        List<File> archiveFiles = new ArrayList<>();
        for (File file : files) {
            if (archiveFilePattern.matcher(file.getName()).matches()) {
                archiveFiles.add(file);
            }
        }

        // 파일명 알파벳 순 정렬 = 날짜+인덱스 오름차순 (오래된 순)
        Collections.sort(archiveFiles);

        // 전체 크기 계산
        long totalSize = 0;
        for (File file : archiveFiles) {
            totalSize += file.length();
        }

        // 상한 초과 시 오래된 파일부터 삭제
        for (File file : archiveFiles) {
            if (totalSize <= totalSizeCap) {
                break;
            }
            long fileSize = file.length();
            if (file.delete()) {
                totalSize -= fileSize;
                log.debug("[UniversalLogger] totalSizeCap 초과로 삭제: {}", file.getAbsolutePath());
            }
        }
    }

    /**
     * 비동기 아카이브 정리
     *
     * 자정 롤오버 시 Logback 내부에서 호출됩니다.
     * 별도 daemon 스레드에서 {@link #clean(Date)}를 실행합니다.
     *
     * @param now 현재 시각
     * @return 비동기 작업 Future
     */
    @Override
    public Future<?> cleanAsynchronously(Date now) {
        FutureTask<?> task = new FutureTask<>(new Runnable() {
            @Override
            public void run() {
                clean(now);
            }
        }, null);
        Thread cleanThread = new Thread(task);
        cleanThread.setDaemon(true);
        cleanThread.setName("UniversalLogger-ArchiveCleaner");
        cleanThread.start();
        return task;
    }

    @Override
    public void setMaxHistory(int maxHistory) {
        this.maxHistory = maxHistory;
    }

    @Override
    public void setTotalSizeCap(long totalSizeCap) {
        this.totalSizeCap = totalSizeCap;
    }
}
