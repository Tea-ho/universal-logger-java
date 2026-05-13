package io.universallogger.sql;

import java.io.InputStream;
import java.io.Reader;
import java.math.BigDecimal;
import java.net.URL;
import java.sql.*;
import java.util.Calendar;
import java.util.Map;

/**
 * SQL 캡처를 위한 CallableStatement 래퍼 클래스
 *
 * 저장 프로시저(Stored Procedure) 호출을 위한 CallableStatement를 래핑하여
 * SQL 실행 정보를 캡처하고 로깅합니다.
 * {@link SqlCapturePreparedStatement}를 상속받아 파라미터 바인딩 추적 기능을 재사용합니다.
 *
 * <p>주요 기능:</p>
 * <ul>
 *   <li>저장 프로시저 호출 SQL 로깅</li>
 *   <li>IN/OUT 파라미터 추적</li>
 *   <li>실행 결과 로깅</li>
 * </ul>
 *
 * @see SqlCapturePreparedStatement
 * @see SqlCaptureProxy
 */
class SqlCaptureCallableStatement extends SqlCapturePreparedStatement implements CallableStatement {
    private final CallableStatement targetCallableStatement;

    public SqlCaptureCallableStatement(CallableStatement targetCallableStatement, String sql, io.universallogger.config.LoggerConfig config) {
        super(targetCallableStatement, sql, config);
        this.targetCallableStatement = targetCallableStatement;
    }

    // CallableStatement 메서드 구현 (target에 위임)
    @Override public void registerOutParameter(int parameterIndex, int sqlType) throws SQLException { targetCallableStatement.registerOutParameter(parameterIndex, sqlType); }
    @Override public void registerOutParameter(int parameterIndex, int sqlType, int scale) throws SQLException { targetCallableStatement.registerOutParameter(parameterIndex, sqlType, scale); }
    @Override public boolean wasNull() throws SQLException { return targetCallableStatement.wasNull(); }
    @Override public String getString(int parameterIndex) throws SQLException { return targetCallableStatement.getString(parameterIndex); }
    @Override public boolean getBoolean(int parameterIndex) throws SQLException { return targetCallableStatement.getBoolean(parameterIndex); }
    @Override public byte getByte(int parameterIndex) throws SQLException { return targetCallableStatement.getByte(parameterIndex); }
    @Override public short getShort(int parameterIndex) throws SQLException { return targetCallableStatement.getShort(parameterIndex); }
    @Override public int getInt(int parameterIndex) throws SQLException { return targetCallableStatement.getInt(parameterIndex); }
    @Override public long getLong(int parameterIndex) throws SQLException { return targetCallableStatement.getLong(parameterIndex); }
    @Override public float getFloat(int parameterIndex) throws SQLException { return targetCallableStatement.getFloat(parameterIndex); }
    @Override public double getDouble(int parameterIndex) throws SQLException { return targetCallableStatement.getDouble(parameterIndex); }
    @Override public BigDecimal getBigDecimal(int parameterIndex, int scale) throws SQLException { return targetCallableStatement.getBigDecimal(parameterIndex, scale); }
    @Override public byte[] getBytes(int parameterIndex) throws SQLException { return targetCallableStatement.getBytes(parameterIndex); }
    @Override public Date getDate(int parameterIndex) throws SQLException { return targetCallableStatement.getDate(parameterIndex); }
    @Override public Time getTime(int parameterIndex) throws SQLException { return targetCallableStatement.getTime(parameterIndex); }
    @Override public Timestamp getTimestamp(int parameterIndex) throws SQLException { return targetCallableStatement.getTimestamp(parameterIndex); }
    @Override public Object getObject(int parameterIndex) throws SQLException { return targetCallableStatement.getObject(parameterIndex); }
    @Override public BigDecimal getBigDecimal(int parameterIndex) throws SQLException { return targetCallableStatement.getBigDecimal(parameterIndex); }
    @Override public Object getObject(int parameterIndex, Map<String, Class<?>> map) throws SQLException { return targetCallableStatement.getObject(parameterIndex, map); }
    @Override public Ref getRef(int parameterIndex) throws SQLException { return targetCallableStatement.getRef(parameterIndex); }
    @Override public Blob getBlob(int parameterIndex) throws SQLException { return targetCallableStatement.getBlob(parameterIndex); }
    @Override public Clob getClob(int parameterIndex) throws SQLException { return targetCallableStatement.getClob(parameterIndex); }
    @Override public Array getArray(int parameterIndex) throws SQLException { return targetCallableStatement.getArray(parameterIndex); }
    @Override public Date getDate(int parameterIndex, Calendar cal) throws SQLException { return targetCallableStatement.getDate(parameterIndex, cal); }
    @Override public Time getTime(int parameterIndex, Calendar cal) throws SQLException { return targetCallableStatement.getTime(parameterIndex, cal); }
    @Override public Timestamp getTimestamp(int parameterIndex, Calendar cal) throws SQLException { return targetCallableStatement.getTimestamp(parameterIndex, cal); }
    @Override public void registerOutParameter(int parameterIndex, int sqlType, String typeName) throws SQLException { targetCallableStatement.registerOutParameter(parameterIndex, sqlType, typeName); }
    @Override public void registerOutParameter(String parameterName, int sqlType) throws SQLException { targetCallableStatement.registerOutParameter(parameterName, sqlType); }
    @Override public void registerOutParameter(String parameterName, int sqlType, int scale) throws SQLException { targetCallableStatement.registerOutParameter(parameterName, sqlType, scale); }
    @Override public void registerOutParameter(String parameterName, int sqlType, String typeName) throws SQLException { targetCallableStatement.registerOutParameter(parameterName, sqlType, typeName); }
    @Override public URL getURL(int parameterIndex) throws SQLException { return targetCallableStatement.getURL(parameterIndex); }
    @Override public void setURL(String parameterName, URL val) throws SQLException { targetCallableStatement.setURL(parameterName, val); }
    @Override public void setNull(String parameterName, int sqlType) throws SQLException { targetCallableStatement.setNull(parameterName, sqlType); }
    @Override public void setBoolean(String parameterName, boolean x) throws SQLException { targetCallableStatement.setBoolean(parameterName, x); }
    @Override public void setByte(String parameterName, byte x) throws SQLException { targetCallableStatement.setByte(parameterName, x); }
    @Override public void setShort(String parameterName, short x) throws SQLException { targetCallableStatement.setShort(parameterName, x); }
    @Override public void setInt(String parameterName, int x) throws SQLException { targetCallableStatement.setInt(parameterName, x); }
    @Override public void setLong(String parameterName, long x) throws SQLException { targetCallableStatement.setLong(parameterName, x); }
    @Override public void setFloat(String parameterName, float x) throws SQLException { targetCallableStatement.setFloat(parameterName, x); }
    @Override public void setDouble(String parameterName, double x) throws SQLException { targetCallableStatement.setDouble(parameterName, x); }
    @Override public void setBigDecimal(String parameterName, BigDecimal x) throws SQLException { targetCallableStatement.setBigDecimal(parameterName, x); }
    @Override public void setString(String parameterName, String x) throws SQLException { targetCallableStatement.setString(parameterName, x); }
    @Override public void setBytes(String parameterName, byte[] x) throws SQLException { targetCallableStatement.setBytes(parameterName, x); }
    @Override public void setDate(String parameterName, Date x) throws SQLException { targetCallableStatement.setDate(parameterName, x); }
    @Override public void setTime(String parameterName, Time x) throws SQLException { targetCallableStatement.setTime(parameterName, x); }
    @Override public void setTimestamp(String parameterName, Timestamp x) throws SQLException { targetCallableStatement.setTimestamp(parameterName, x); }
    @Override public void setAsciiStream(String parameterName, InputStream x, int length) throws SQLException { targetCallableStatement.setAsciiStream(parameterName, x, length); }
    @Override public void setBinaryStream(String parameterName, InputStream x, int length) throws SQLException { targetCallableStatement.setBinaryStream(parameterName, x, length); }
    @Override public void setObject(String parameterName, Object x, int targetSqlType, int scale) throws SQLException { targetCallableStatement.setObject(parameterName, x, targetSqlType, scale); }
    @Override public void setObject(String parameterName, Object x, int targetSqlType) throws SQLException { targetCallableStatement.setObject(parameterName, x, targetSqlType); }
    @Override public void setObject(String parameterName, Object x) throws SQLException { targetCallableStatement.setObject(parameterName, x); }
    @Override public void setCharacterStream(String parameterName, Reader reader, int length) throws SQLException { targetCallableStatement.setCharacterStream(parameterName, reader, length); }
    @Override public void setDate(String parameterName, Date x, Calendar cal) throws SQLException { targetCallableStatement.setDate(parameterName, x, cal); }
    @Override public void setTime(String parameterName, Time x, Calendar cal) throws SQLException { targetCallableStatement.setTime(parameterName, x, cal); }
    @Override public void setTimestamp(String parameterName, Timestamp x, Calendar cal) throws SQLException { targetCallableStatement.setTimestamp(parameterName, x, cal); }
    @Override public void setNull(String parameterName, int sqlType, String typeName) throws SQLException { targetCallableStatement.setNull(parameterName, sqlType, typeName); }
    @Override public String getString(String parameterName) throws SQLException { return targetCallableStatement.getString(parameterName); }
    @Override public boolean getBoolean(String parameterName) throws SQLException { return targetCallableStatement.getBoolean(parameterName); }
    @Override public byte getByte(String parameterName) throws SQLException { return targetCallableStatement.getByte(parameterName); }
    @Override public short getShort(String parameterName) throws SQLException { return targetCallableStatement.getShort(parameterName); }
    @Override public int getInt(String parameterName) throws SQLException { return targetCallableStatement.getInt(parameterName); }
    @Override public long getLong(String parameterName) throws SQLException { return targetCallableStatement.getLong(parameterName); }
    @Override public float getFloat(String parameterName) throws SQLException { return targetCallableStatement.getFloat(parameterName); }
    @Override public double getDouble(String parameterName) throws SQLException { return targetCallableStatement.getDouble(parameterName); }
    @Override public byte[] getBytes(String parameterName) throws SQLException { return targetCallableStatement.getBytes(parameterName); }
    @Override public Date getDate(String parameterName) throws SQLException { return targetCallableStatement.getDate(parameterName); }
    @Override public Time getTime(String parameterName) throws SQLException { return targetCallableStatement.getTime(parameterName); }
    @Override public Timestamp getTimestamp(String parameterName) throws SQLException { return targetCallableStatement.getTimestamp(parameterName); }
    @Override public Object getObject(String parameterName) throws SQLException { return targetCallableStatement.getObject(parameterName); }
    @Override public BigDecimal getBigDecimal(String parameterName) throws SQLException { return targetCallableStatement.getBigDecimal(parameterName); }
    @Override public Object getObject(String parameterName, Map<String, Class<?>> map) throws SQLException { return targetCallableStatement.getObject(parameterName, map); }
    @Override public Ref getRef(String parameterName) throws SQLException { return targetCallableStatement.getRef(parameterName); }
    @Override public Blob getBlob(String parameterName) throws SQLException { return targetCallableStatement.getBlob(parameterName); }
    @Override public Clob getClob(String parameterName) throws SQLException { return targetCallableStatement.getClob(parameterName); }
    @Override public Array getArray(String parameterName) throws SQLException { return targetCallableStatement.getArray(parameterName); }
    @Override public Date getDate(String parameterName, Calendar cal) throws SQLException { return targetCallableStatement.getDate(parameterName, cal); }
    @Override public Time getTime(String parameterName, Calendar cal) throws SQLException { return targetCallableStatement.getTime(parameterName, cal); }
    @Override public Timestamp getTimestamp(String parameterName, Calendar cal) throws SQLException { return targetCallableStatement.getTimestamp(parameterName, cal); }
    @Override public URL getURL(String parameterName) throws SQLException { return targetCallableStatement.getURL(parameterName); }
    @Override public RowId getRowId(int parameterIndex) throws SQLException { return targetCallableStatement.getRowId(parameterIndex); }
    @Override public RowId getRowId(String parameterName) throws SQLException { return targetCallableStatement.getRowId(parameterName); }
    @Override public void setRowId(String parameterName, RowId x) throws SQLException { targetCallableStatement.setRowId(parameterName, x); }
    @Override public void setNString(String parameterName, String value) throws SQLException { targetCallableStatement.setNString(parameterName, value); }
    @Override public void setNCharacterStream(String parameterName, Reader value, long length) throws SQLException { targetCallableStatement.setNCharacterStream(parameterName, value, length); }
    @Override public void setNClob(String parameterName, NClob value) throws SQLException { targetCallableStatement.setNClob(parameterName, value); }
    @Override public void setClob(String parameterName, Reader reader, long length) throws SQLException { targetCallableStatement.setClob(parameterName, reader, length); }
    @Override public void setBlob(String parameterName, InputStream inputStream, long length) throws SQLException { targetCallableStatement.setBlob(parameterName, inputStream, length); }
    @Override public void setNClob(String parameterName, Reader reader, long length) throws SQLException { targetCallableStatement.setNClob(parameterName, reader, length); }
    @Override public NClob getNClob(int parameterIndex) throws SQLException { return targetCallableStatement.getNClob(parameterIndex); }
    @Override public NClob getNClob(String parameterName) throws SQLException { return targetCallableStatement.getNClob(parameterName); }
    @Override public void setSQLXML(String parameterName, SQLXML xmlObject) throws SQLException { targetCallableStatement.setSQLXML(parameterName, xmlObject); }
    @Override public SQLXML getSQLXML(int parameterIndex) throws SQLException { return targetCallableStatement.getSQLXML(parameterIndex); }
    @Override public SQLXML getSQLXML(String parameterName) throws SQLException { return targetCallableStatement.getSQLXML(parameterName); }
    @Override public String getNString(int parameterIndex) throws SQLException { return targetCallableStatement.getNString(parameterIndex); }
    @Override public String getNString(String parameterName) throws SQLException { return targetCallableStatement.getNString(parameterName); }
    @Override public Reader getNCharacterStream(int parameterIndex) throws SQLException { return targetCallableStatement.getNCharacterStream(parameterIndex); }
    @Override public Reader getNCharacterStream(String parameterName) throws SQLException { return targetCallableStatement.getNCharacterStream(parameterName); }
    @Override public Reader getCharacterStream(int parameterIndex) throws SQLException { return targetCallableStatement.getCharacterStream(parameterIndex); }
    @Override public Reader getCharacterStream(String parameterName) throws SQLException { return targetCallableStatement.getCharacterStream(parameterName); }
    @Override public void setBlob(String parameterName, Blob x) throws SQLException { targetCallableStatement.setBlob(parameterName, x); }
    @Override public void setClob(String parameterName, Clob x) throws SQLException { targetCallableStatement.setClob(parameterName, x); }
    @Override public void setAsciiStream(String parameterName, InputStream x, long length) throws SQLException { targetCallableStatement.setAsciiStream(parameterName, x, length); }
    @Override public void setBinaryStream(String parameterName, InputStream x, long length) throws SQLException { targetCallableStatement.setBinaryStream(parameterName, x, length); }
    @Override public void setCharacterStream(String parameterName, Reader reader, long length) throws SQLException { targetCallableStatement.setCharacterStream(parameterName, reader, length); }
    @Override public void setAsciiStream(String parameterName, InputStream x) throws SQLException { targetCallableStatement.setAsciiStream(parameterName, x); }
    @Override public void setBinaryStream(String parameterName, InputStream x) throws SQLException { targetCallableStatement.setBinaryStream(parameterName, x); }
    @Override public void setCharacterStream(String parameterName, Reader reader) throws SQLException { targetCallableStatement.setCharacterStream(parameterName, reader); }
    @Override public void setNCharacterStream(String parameterName, Reader value) throws SQLException { targetCallableStatement.setNCharacterStream(parameterName, value); }
    @Override public void setClob(String parameterName, Reader reader) throws SQLException { targetCallableStatement.setClob(parameterName, reader); }
    @Override public void setBlob(String parameterName, InputStream inputStream) throws SQLException { targetCallableStatement.setBlob(parameterName, inputStream); }
    @Override public void setNClob(String parameterName, Reader reader) throws SQLException { targetCallableStatement.setNClob(parameterName, reader); }
    @Override public <T> T getObject(int parameterIndex, Class<T> type) throws SQLException { return targetCallableStatement.getObject(parameterIndex, type); }
    @Override public <T> T getObject(String parameterName, Class<T> type) throws SQLException { return targetCallableStatement.getObject(parameterName, type); }
}
