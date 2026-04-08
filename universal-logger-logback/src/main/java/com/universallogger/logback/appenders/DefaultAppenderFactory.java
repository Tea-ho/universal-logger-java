package com.universallogger.logback.appenders;

import com.universallogger.config.LoggerConfig;
import com.universallogger.logback.formatters.ConsoleFormatter;
import com.universallogger.logback.formatters.FileFormatter;

import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.filter.ThresholdFilter;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.Appender;
import ch.qos.logback.core.ConsoleAppender;
import ch.qos.logback.core.rolling.RollingFileAppender;
import ch.qos.logback.core.rolling.SizeAndTimeBasedRollingPolicy;
import ch.qos.logback.core.rolling.TimeBasedRollingPolicy;
import ch.qos.logback.core.util.FileSize;

import java.lang.reflect.Field;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.TimeZone;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * AppenderFactory의 기본 구현체
 *
 * 콘솔, 파일, 에러 파일, 웹훅, 이메일 앱펜더를 생성하는 기본 팩토리입니다.
 * LoggerInstance에서 설정에 따라 적절한 앱펜더를 생성하는 데 사용됩니다.
 *
 * <p>주요 기능:</p>
 * <ul>
 *   <li>콘솔 앱펜더 - ConsoleFormatter를 사용한 컬러 출력</li>
 *   <li>파일 앱펜더 - SizeAndTimeBasedRollingPolicy를 사용한 롤링</li>
 *   <li>웹훅 앱펜더 - 비동기 HTTP POST 전송</li>
 *   <li>이메일 앱펜더 - SMTP를 통한 알림 전송</li>
 *   <li>Timezone 지원 - 로그 timestamp에 지정된 timezone 적용</li>
 * </ul>
 *
 * @see AppenderFactory
 * @see LoggerInstance
 */
public class DefaultAppenderFactory implements AppenderFactory {

    private static final Logger log = LoggerFactory.getLogger(DefaultAppenderFactory.class);

    /** %d{...} 패턴 매칭용 정규식 */
    private static final Pattern DATE_PATTERN = Pattern.compile("%d\\{([^}]+)}");

    /** Timezone 설정 (null이면 서버 기본값 사용) */
    private String timezone;

    /**
     * 기본 생성자 - 서버 기본 Timezone 사용
     */
    public DefaultAppenderFactory() {
        this.timezone = TimeZone.getDefault().getID();
    }

    /**
     * Timezone 설정
     *
     * @param timezone Timezone ID (예: "Asia/Seoul", "UTC")
     */
    public void setTimezone(String timezone) {
        this.timezone = timezone;
    }

    /**
     * 현재 설정된 Timezone 반환
     *
     * @return Timezone ID
     */
    public String getTimezone() {
        return timezone;
    }

    @Override
    public Appender<ILoggingEvent> createConsoleAppender(
            LoggerContext context,
            LoggerConfig.OutputConfig outputConfig,
            String appName) {

        ConsoleAppender<ILoggingEvent> appender = new ConsoleAppender<>();
        appender.setContext(context);
        appender.setName("CONSOLE");

        // 콘솔 포맷터 설정 (컬러 지원)
        ConsoleFormatter formatter = new ConsoleFormatter();
        formatter.setContext(context);
        formatter.setPattern(applyTimezoneToPattern(outputConfig.getFormat()));
        formatter.setColors(outputConfig.getColors());
        formatter.start();

        appender.setEncoder(formatter);

        // 임계값 필터 추가
        addThresholdFilter(appender, outputConfig.getLevel());

        appender.start();
        return appender;
    }

    @Override
    public Appender<ILoggingEvent> createFileAppender(
            LoggerContext context,
            LoggerConfig.OutputConfig outputConfig,
            String appName) {

        RollingFileAppender<ILoggingEvent> appender = new RollingFileAppender<>();
        appender.setContext(context);
        appender.setName("FILE");

        // 파일 경로 설정
        String filePath = replacePlaceholders(outputConfig.getPath(), appName);
        createParentDirectories(filePath);
        appender.setFile(filePath);

        // 롤링 정책 설정
        LoggerConfig.RotationConfig rotation = outputConfig.getRotation();
        String maxSize = rotation != null && rotation.getMaxSize() != null ? rotation.getMaxSize() : "50MB";
        int maxFiles = rotation != null && rotation.getMaxFiles() > 0 ? rotation.getMaxFiles() : 30;

        SizeAndTimeBasedRollingPolicy<ILoggingEvent> rollingPolicy =
            new SizeAndTimeBasedRollingPolicy<>();
        rollingPolicy.setContext(context);
        rollingPolicy.setParent(appender);
        // compress 설정에 따라 파일 패턴 결정
        String filePattern = buildFileNamePattern(filePath, rotation != null && rotation.isCompress());
        rollingPolicy.setFileNamePattern(filePattern);
        rollingPolicy.setMaxFileSize(FileSize.valueOf(maxSize));
        rollingPolicy.setMaxHistory(maxFiles);
        // 전체 로그 용량 상한 (디스크 고갈 방지: maxSize * maxFiles)
        FileSize totalSizeCap = calculateTotalSizeCap(maxSize, maxFiles);
        rollingPolicy.setTotalSizeCap(totalSizeCap);
        rollingPolicy.start();

        // Logback 1.2.x DefaultArchiveRemover의 %i 미처리 버그 우회:
        // 커스텀 SizeAndTimeBasedArchiveRemover로 교체하여 자정 롤오버 시 정상 삭제 보장
        injectCustomArchiveRemover(rollingPolicy, filePath,
                rotation != null && rotation.isCompress(), maxFiles, totalSizeCap.getSize());

        appender.setRollingPolicy(rollingPolicy);
        appender.setTriggeringPolicy(rollingPolicy);

        // 파일 포맷터 설정
        FileFormatter formatter = new FileFormatter();
        formatter.setContext(context);
        formatter.setPattern(applyTimezoneToPattern(outputConfig.getFormat()));
        formatter.start();

        appender.setEncoder(formatter);

        // 임계값 필터 추가
        addThresholdFilter(appender, outputConfig.getLevel());

        appender.start();
        return appender;
    }

    @Override
    public Appender<ILoggingEvent> createErrorFileAppender(
            LoggerContext context,
            LoggerConfig.OutputConfig outputConfig,
            String appName) {

        RollingFileAppender<ILoggingEvent> appender = new RollingFileAppender<>();
        appender.setContext(context);
        appender.setName("ERRORFILE");

        // 파일 경로 설정
        String filePath = replacePlaceholders(outputConfig.getPath(), appName);
        createParentDirectories(filePath);
        appender.setFile(filePath);

        // 롤링 정책 설정
        LoggerConfig.RotationConfig errorRotation = outputConfig.getRotation();
        String errorMaxSize = errorRotation != null && errorRotation.getMaxSize() != null ? errorRotation.getMaxSize() : "50MB";
        int errorMaxFiles = errorRotation != null && errorRotation.getMaxFiles() > 0 ? errorRotation.getMaxFiles() : 30;

        SizeAndTimeBasedRollingPolicy<ILoggingEvent> rollingPolicy =
            new SizeAndTimeBasedRollingPolicy<>();
        rollingPolicy.setContext(context);
        rollingPolicy.setParent(appender);
        // compress 설정에 따라 파일 패턴 결정
        String filePattern = buildFileNamePattern(filePath, errorRotation != null && errorRotation.isCompress());
        rollingPolicy.setFileNamePattern(filePattern);
        rollingPolicy.setMaxFileSize(FileSize.valueOf(errorMaxSize));
        rollingPolicy.setMaxHistory(errorMaxFiles);
        // 전체 로그 용량 상한 (디스크 고갈 방지: maxSize * maxFiles)
        FileSize errorTotalSizeCap = calculateTotalSizeCap(errorMaxSize, errorMaxFiles);
        rollingPolicy.setTotalSizeCap(errorTotalSizeCap);
        rollingPolicy.start();

        // Logback 1.2.x DefaultArchiveRemover의 %i 미처리 버그 우회:
        // 커스텀 SizeAndTimeBasedArchiveRemover로 교체하여 자정 롤오버 시 정상 삭제 보장
        injectCustomArchiveRemover(rollingPolicy, filePath,
                errorRotation != null && errorRotation.isCompress(), errorMaxFiles, errorTotalSizeCap.getSize());

        appender.setRollingPolicy(rollingPolicy);
        appender.setTriggeringPolicy(rollingPolicy);

        // 파일 포맷터 설정
        FileFormatter formatter = new FileFormatter();
        formatter.setContext(context);
        formatter.setPattern(applyTimezoneToPattern(outputConfig.getFormat()));
        formatter.start();

        appender.setEncoder(formatter);

        // 임계값 필터 추가 (에러 레벨 이상만)
        addThresholdFilter(appender, outputConfig.getLevel());

        appender.start();
        return appender;
    }

    @Override
    public Appender<ILoggingEvent> createWebhookAppender(
            LoggerContext context,
            LoggerConfig.OutputConfig outputConfig) {

        WebhookAppender appender = new WebhookAppender();
        appender.setContext(context);
        appender.setName("WEBHOOK");

        if (outputConfig.getWebhook() != null) {
            appender.setUrl(outputConfig.getWebhook().getUrl());
            appender.setFormat(outputConfig.getWebhook().getFormat());

            // AlertPolicy 설정 전달 (Rate Limiting + Deduplication)
            if (outputConfig.getWebhook().getAlertPolicy() != null) {
                appender.setAlertPolicy(outputConfig.getWebhook().getAlertPolicy());
            }
        }

        // 임계값 필터 추가 (레벨 필터링)
        addThresholdFilter(appender, outputConfig.getLevel());

        appender.start();
        return appender;
    }

    @Override
    public Appender<ILoggingEvent> createEmailAppender(
            LoggerContext context,
            LoggerConfig.OutputConfig outputConfig) {

        EmailAppender appender = new EmailAppender();
        appender.setContext(context);
        appender.setName("EMAIL");

        if (outputConfig.getEmail() != null) {
            appender.setEmailConfig(outputConfig.getEmail());
        }

        // 임계값 필터 추가
        addThresholdFilter(appender, outputConfig.getLevel());

        appender.start();
        return appender;
    }

    // ========== 헬퍼 메서드 ==========

    /**
     * SizeAndTimeBasedRollingPolicy에 커스텀 ArchiveRemover 주입
     *
     * Logback 1.2.x의 {@code DefaultArchiveRemover}는 {@code %i} 인덱스가 포함된 파일 패턴을
     * 올바르게 처리하지 못해 {@code maxHistory}에 따른 파일 삭제가 동작하지 않습니다.
     * {@code TimeBasedRollingPolicy.archiveRemover} 필드를 reflection으로 교체합니다.
     *
     * @param rollingPolicy  대상 롤링 정책
     * @param filePath       기본 로그 파일 경로
     * @param compress       gz 압축 여부
     * @param maxHistory     보관할 최대 일수
     * @param totalSizeCap   전체 용량 상한 (bytes)
     */
    private void injectCustomArchiveRemover(
            SizeAndTimeBasedRollingPolicy<ILoggingEvent> rollingPolicy,
            String filePath,
            boolean compress,
            int maxHistory,
            long totalSizeCap) {
        try {
            SizeAndTimeBasedArchiveRemover customRemover =
                    new SizeAndTimeBasedArchiveRemover(filePath, compress);
            customRemover.setMaxHistory(maxHistory);
            customRemover.setTotalSizeCap(totalSizeCap);

            // TimeBasedRollingPolicy(부모 클래스)의 archiveRemover 필드 교체
            Field field = TimeBasedRollingPolicy.class.getDeclaredField("archiveRemover");
            field.setAccessible(true);
            field.set(rollingPolicy, customRemover);

            log.debug("[UniversalLogger] 커스텀 ArchiveRemover 주입 완료: {}", filePath);
        } catch (Exception e) {
            log.warn("[UniversalLogger] 커스텀 ArchiveRemover 주입 실패 - maxHistory가 동작하지 않을 수 있습니다. 원인: {}",
                    e.getMessage());
        }
    }

    /**
     * 임계값 필터 추가
     */
    private void addThresholdFilter(Appender<ILoggingEvent> appender, String level) {
        if (level != null && !level.isEmpty()) {
            ThresholdFilter thresholdFilter = new ThresholdFilter();
            thresholdFilter.setLevel(level);
            thresholdFilter.start();
            appender.addFilter(thresholdFilter);
        }
    }

    /**
     * 부모 디렉토리 생성
     */
    private void createParentDirectories(String filePath) {
        if (filePath != null) {
            File file = new File(filePath);
            File parentDir = file.getParentFile();
            if (parentDir != null && !parentDir.exists()) {
                boolean created = parentDir.mkdirs();
                if (!created && !parentDir.exists()) {
                    log.warn("Failed to create log directory: {}", parentDir.getAbsolutePath());
                }
            }
        }
    }

    /**
     * 플레이스홀더 치환
     */
    private String replacePlaceholders(String path, String appName) {
        if (path == null) {
            return path;
        }
        if (appName != null) {
            return path.replace("{appName}", appName);
        }
        return path;
    }

    /**
     * 전체 로그 용량 상한 계산 (maxSize * maxFiles)
     *
     * @param maxSize 개별 파일 최대 크기 (예: "50MB")
     * @param maxFiles 최대 파일 수
     * @return 전체 용량 상한
     */
    private FileSize calculateTotalSizeCap(String maxSize, int maxFiles) {
        try {
            long sizeBytes = FileSize.valueOf(maxSize).getSize();
            return new FileSize(sizeBytes * maxFiles);
        } catch (Exception e) {
            // 파싱 실패 시 안전 기본값 5GB
            return FileSize.valueOf("5GB");
        }
    }

    /**
     * 파일명 패턴 생성
     *
     * compress가 true이면 .gz 확장자 추가
     * compress가 false이면 압축 없이 일반 파일로 저장
     *
     * @param filePath 기본 파일 경로
     * @param compress 압축 여부
     * @return 롤링 파일 패턴
     */
    private String buildFileNamePattern(String filePath, boolean compress) {
        if (compress) {
            return filePath + ".%d{yyyy-MM-dd}.%i.gz";
        } else {
            return filePath + ".%d{yyyy-MM-dd}.%i";
        }
    }

    /**
     * 패턴에 Timezone 적용
     *
     * %d{yyyy-MM-dd HH:mm:ss} 형식을 %d{yyyy-MM-dd HH:mm:ss, Asia/Seoul} 형식으로 변환합니다.
     * 이미 timezone이 지정된 경우(%d{..., ...}) 변환하지 않습니다.
     *
     * @param pattern 원본 패턴
     * @return Timezone이 적용된 패턴
     */
    private String applyTimezoneToPattern(String pattern) {
        if (pattern == null || timezone == null || timezone.isEmpty()) {
            return pattern;
        }

        Matcher matcher = DATE_PATTERN.matcher(pattern);
        StringBuffer result = new StringBuffer();

        while (matcher.find()) {
            String dateFormat = matcher.group(1);
            // 이미 timezone이 지정되어 있으면 (쉼표 포함) 스킵
            if (!dateFormat.contains(",")) {
                matcher.appendReplacement(result, "%d{" + dateFormat + ", " + timezone + "}");
            }
        }
        matcher.appendTail(result);

        return result.toString();
    }
}
