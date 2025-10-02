package com.universallogger.appenders.database;

import java.sql.Connection;
import java.sql.SQLException;

/**
 * 데이터베이스 연결 인터페이스
 * 모든 데이터베이스 구현체는 이 인터페이스를 구현해야 함
 */
public interface DatabaseConnection {

    /**
     * 데이터베이스 연결 생성
     *
     * @return Connection 객체
     * @throws SQLException 연결 실패 시
     */
    Connection getConnection() throws SQLException;

    /**
     * 연결 종료
     */
    void close();

    /**
     * 로그 테이블 생성 (존재하지 않을 경우)
     *
     * @throws SQLException 테이블 생성 실패 시
     */
    void createTableIfNotExists() throws SQLException;

    /**
     * 연결 테스트
     *
     * @return 연결 성공 여부
     */
    boolean testConnection();

    /**
     * 데이터베이스 타입 반환
     *
     * @return 데이터베이스 타입 (oracle, mysql, mssql 등)
     */
    String getDatabaseType();
}