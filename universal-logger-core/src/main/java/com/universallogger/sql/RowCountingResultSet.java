package com.universallogger.sql;

import com.universallogger.config.LoggerConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;
import java.io.Reader;
import java.math.BigDecimal;
import java.net.URL;
import java.sql.*;
import java.util.Calendar;
import java.util.Map;

/**
 * 행 카운팅을 위한 ResultSet 래퍼 클래스
 *
 * MyBatis의 ResultSetLogger와 유사하게 동작하며, SELECT 쿼리 결과의 행 수를 추적합니다.
 * 결과 집합이 소비될 때 행 수를 카운트하고, 완료 시 총 행 수를 로깅합니다.
 *
 * <p>주요 기능:</p>
 * <ul>
 *   <li>next() 호출 시마다 행 카운트 증가</li>
 *   <li>getter 메서드 호출 시 행 접근 추적</li>
 *   <li>ResultSet 종료 시 총 행 수 로깅 ([SQL-ROWS] 형식)</li>
 * </ul>
 *
 * @see SqlCaptureProxy
 */
class RowCountingResultSet implements ResultSet {
    private static final Logger logger = LoggerFactory.getLogger("SQL");
    private final ResultSet targetResultSet;
    private final String callerInfo;
    private final LoggerConfig config;
    private int rows = 0;
    private int currentRow = 0;
    private volatile boolean countLogged = false;

    public RowCountingResultSet(ResultSet targetResultSet, String callerInfo, LoggerConfig config) {
        this.targetResultSet = targetResultSet;
        this.callerInfo = callerInfo;
        this.config = config;
    }

    @Override
    public boolean next() throws SQLException {
        boolean hasNext = targetResultSet.next();
        if (hasNext) {
            currentRow++;
            if (currentRow > rows) {
                rows = currentRow;
            }
        } else {
            // 더 이상 행이 없을 때 총 행 수 로깅 (MyBatis ResultSetLogger 방식)
            logRowCount();
        }
        return hasNext;
    }

    // getter 메서드를 통한 행 접근 추적 (MyBatis가 이 메서드들을 호출함)
    private void trackRowAccess() {
        if (currentRow > rows) {
            rows = currentRow;
        }
    }

    @Override
    public <T> T unwrap(Class<T> iface) throws SQLException {
        if (iface.isInstance(this)) {
            return iface.cast(this);
        }
        // 언래핑 전에 행 카운트 로깅 (MyBatis가 ResultSet을 언래핑할 수 있음)
        logRowCount();
        return targetResultSet.unwrap(iface);
    }

    @Override
    public boolean isWrapperFor(Class<?> iface) throws SQLException {
        return iface.isInstance(this) || targetResultSet.isWrapperFor(iface);
    }

    @Override
    public void close() throws SQLException {
        logRowCount();
        targetResultSet.close();
    }

    private void logRowCount() {
        if (!countLogged && rows > 0) {
            countLogged = true;
            logger.info("[SQL-ROWS] Total: " + rows);
        }
    }

    // 나머지 모든 메서드는 targetResultSet에 위임
    @Override public boolean wasNull() throws SQLException { return targetResultSet.wasNull(); }
    @Override public String getString(int columnIndex) throws SQLException { trackRowAccess(); return targetResultSet.getString(columnIndex); }
    @Override public boolean getBoolean(int columnIndex) throws SQLException { trackRowAccess(); return targetResultSet.getBoolean(columnIndex); }
    @Override public byte getByte(int columnIndex) throws SQLException { trackRowAccess(); return targetResultSet.getByte(columnIndex); }
    @Override public short getShort(int columnIndex) throws SQLException { trackRowAccess(); return targetResultSet.getShort(columnIndex); }
    @Override public int getInt(int columnIndex) throws SQLException { trackRowAccess(); return targetResultSet.getInt(columnIndex); }
    @Override public long getLong(int columnIndex) throws SQLException { trackRowAccess(); return targetResultSet.getLong(columnIndex); }
    @Override public float getFloat(int columnIndex) throws SQLException { trackRowAccess(); return targetResultSet.getFloat(columnIndex); }
    @Override public double getDouble(int columnIndex) throws SQLException { trackRowAccess(); return targetResultSet.getDouble(columnIndex); }
    @Override public BigDecimal getBigDecimal(int columnIndex, int scale) throws SQLException { trackRowAccess(); return targetResultSet.getBigDecimal(columnIndex, scale); }
    @Override public byte[] getBytes(int columnIndex) throws SQLException { trackRowAccess(); return targetResultSet.getBytes(columnIndex); }
    @Override public Date getDate(int columnIndex) throws SQLException { trackRowAccess(); return targetResultSet.getDate(columnIndex); }
    @Override public Time getTime(int columnIndex) throws SQLException { trackRowAccess(); return targetResultSet.getTime(columnIndex); }
    @Override public Timestamp getTimestamp(int columnIndex) throws SQLException { trackRowAccess(); return targetResultSet.getTimestamp(columnIndex); }
    @Override public InputStream getAsciiStream(int columnIndex) throws SQLException { return targetResultSet.getAsciiStream(columnIndex); }
    @Override public InputStream getUnicodeStream(int columnIndex) throws SQLException { return targetResultSet.getUnicodeStream(columnIndex); }
    @Override public InputStream getBinaryStream(int columnIndex) throws SQLException { return targetResultSet.getBinaryStream(columnIndex); }
    @Override public String getString(String columnLabel) throws SQLException { trackRowAccess(); return targetResultSet.getString(columnLabel); }
    @Override public boolean getBoolean(String columnLabel) throws SQLException { trackRowAccess(); return targetResultSet.getBoolean(columnLabel); }
    @Override public byte getByte(String columnLabel) throws SQLException { trackRowAccess(); return targetResultSet.getByte(columnLabel); }
    @Override public short getShort(String columnLabel) throws SQLException { trackRowAccess(); return targetResultSet.getShort(columnLabel); }
    @Override public int getInt(String columnLabel) throws SQLException { trackRowAccess(); return targetResultSet.getInt(columnLabel); }
    @Override public long getLong(String columnLabel) throws SQLException { trackRowAccess(); return targetResultSet.getLong(columnLabel); }
    @Override public float getFloat(String columnLabel) throws SQLException { trackRowAccess(); return targetResultSet.getFloat(columnLabel); }
    @Override public double getDouble(String columnLabel) throws SQLException { trackRowAccess(); return targetResultSet.getDouble(columnLabel); }
    @Override public BigDecimal getBigDecimal(String columnLabel, int scale) throws SQLException { trackRowAccess(); return targetResultSet.getBigDecimal(columnLabel, scale); }
    @Override public byte[] getBytes(String columnLabel) throws SQLException { trackRowAccess(); return targetResultSet.getBytes(columnLabel); }
    @Override public Date getDate(String columnLabel) throws SQLException { trackRowAccess(); return targetResultSet.getDate(columnLabel); }
    @Override public Time getTime(String columnLabel) throws SQLException { trackRowAccess(); return targetResultSet.getTime(columnLabel); }
    @Override public Timestamp getTimestamp(String columnLabel) throws SQLException { trackRowAccess(); return targetResultSet.getTimestamp(columnLabel); }
    @Override public InputStream getAsciiStream(String columnLabel) throws SQLException { return targetResultSet.getAsciiStream(columnLabel); }
    @Override public InputStream getUnicodeStream(String columnLabel) throws SQLException { return targetResultSet.getUnicodeStream(columnLabel); }
    @Override public InputStream getBinaryStream(String columnLabel) throws SQLException { return targetResultSet.getBinaryStream(columnLabel); }
    @Override public SQLWarning getWarnings() throws SQLException { return targetResultSet.getWarnings(); }
    @Override public void clearWarnings() throws SQLException { targetResultSet.clearWarnings(); }
    @Override public String getCursorName() throws SQLException { return targetResultSet.getCursorName(); }
    @Override public ResultSetMetaData getMetaData() throws SQLException { return targetResultSet.getMetaData(); }
    @Override public Object getObject(int columnIndex) throws SQLException { trackRowAccess(); return targetResultSet.getObject(columnIndex); }
    @Override public Object getObject(String columnLabel) throws SQLException { trackRowAccess(); return targetResultSet.getObject(columnLabel); }
    @Override public int findColumn(String columnLabel) throws SQLException { return targetResultSet.findColumn(columnLabel); }
    @Override public Reader getCharacterStream(int columnIndex) throws SQLException { return targetResultSet.getCharacterStream(columnIndex); }
    @Override public Reader getCharacterStream(String columnLabel) throws SQLException { return targetResultSet.getCharacterStream(columnLabel); }
    @Override public BigDecimal getBigDecimal(int columnIndex) throws SQLException { return targetResultSet.getBigDecimal(columnIndex); }
    @Override public BigDecimal getBigDecimal(String columnLabel) throws SQLException { return targetResultSet.getBigDecimal(columnLabel); }
    @Override public boolean isBeforeFirst() throws SQLException { return targetResultSet.isBeforeFirst(); }
    @Override public boolean isAfterLast() throws SQLException { return targetResultSet.isAfterLast(); }
    @Override public boolean isFirst() throws SQLException { return targetResultSet.isFirst(); }
    @Override public boolean isLast() throws SQLException { return targetResultSet.isLast(); }
    @Override public void beforeFirst() throws SQLException { targetResultSet.beforeFirst(); }
    @Override public void afterLast() throws SQLException { targetResultSet.afterLast(); }
    @Override public boolean first() throws SQLException { return targetResultSet.first(); }
    @Override public boolean last() throws SQLException { return targetResultSet.last(); }
    @Override public int getRow() throws SQLException { return targetResultSet.getRow(); }
    @Override public boolean absolute(int row) throws SQLException { return targetResultSet.absolute(row); }
    @Override public boolean relative(int rows) throws SQLException { return targetResultSet.relative(rows); }
    @Override public boolean previous() throws SQLException { return targetResultSet.previous(); }
    @Override public void setFetchDirection(int direction) throws SQLException { targetResultSet.setFetchDirection(direction); }
    @Override public int getFetchDirection() throws SQLException { return targetResultSet.getFetchDirection(); }
    @Override public void setFetchSize(int rows) throws SQLException { targetResultSet.setFetchSize(rows); }
    @Override public int getFetchSize() throws SQLException { return targetResultSet.getFetchSize(); }
    @Override public int getType() throws SQLException { return targetResultSet.getType(); }
    @Override public int getConcurrency() throws SQLException { return targetResultSet.getConcurrency(); }
    @Override public boolean rowUpdated() throws SQLException { return targetResultSet.rowUpdated(); }
    @Override public boolean rowInserted() throws SQLException { return targetResultSet.rowInserted(); }
    @Override public boolean rowDeleted() throws SQLException { return targetResultSet.rowDeleted(); }
    @Override public void updateNull(int columnIndex) throws SQLException { targetResultSet.updateNull(columnIndex); }
    @Override public void updateBoolean(int columnIndex, boolean x) throws SQLException { targetResultSet.updateBoolean(columnIndex, x); }
    @Override public void updateByte(int columnIndex, byte x) throws SQLException { targetResultSet.updateByte(columnIndex, x); }
    @Override public void updateShort(int columnIndex, short x) throws SQLException { targetResultSet.updateShort(columnIndex, x); }
    @Override public void updateInt(int columnIndex, int x) throws SQLException { targetResultSet.updateInt(columnIndex, x); }
    @Override public void updateLong(int columnIndex, long x) throws SQLException { targetResultSet.updateLong(columnIndex, x); }
    @Override public void updateFloat(int columnIndex, float x) throws SQLException { targetResultSet.updateFloat(columnIndex, x); }
    @Override public void updateDouble(int columnIndex, double x) throws SQLException { targetResultSet.updateDouble(columnIndex, x); }
    @Override public void updateBigDecimal(int columnIndex, BigDecimal x) throws SQLException { targetResultSet.updateBigDecimal(columnIndex, x); }
    @Override public void updateString(int columnIndex, String x) throws SQLException { targetResultSet.updateString(columnIndex, x); }
    @Override public void updateBytes(int columnIndex, byte[] x) throws SQLException { targetResultSet.updateBytes(columnIndex, x); }
    @Override public void updateDate(int columnIndex, Date x) throws SQLException { targetResultSet.updateDate(columnIndex, x); }
    @Override public void updateTime(int columnIndex, Time x) throws SQLException { targetResultSet.updateTime(columnIndex, x); }
    @Override public void updateTimestamp(int columnIndex, Timestamp x) throws SQLException { targetResultSet.updateTimestamp(columnIndex, x); }
    @Override public void updateAsciiStream(int columnIndex, InputStream x, int length) throws SQLException { targetResultSet.updateAsciiStream(columnIndex, x, length); }
    @Override public void updateBinaryStream(int columnIndex, InputStream x, int length) throws SQLException { targetResultSet.updateBinaryStream(columnIndex, x, length); }
    @Override public void updateCharacterStream(int columnIndex, Reader x, int length) throws SQLException { targetResultSet.updateCharacterStream(columnIndex, x, length); }
    @Override public void updateObject(int columnIndex, Object x, int scaleOrLength) throws SQLException { targetResultSet.updateObject(columnIndex, x, scaleOrLength); }
    @Override public void updateObject(int columnIndex, Object x) throws SQLException { targetResultSet.updateObject(columnIndex, x); }
    @Override public void updateNull(String columnLabel) throws SQLException { targetResultSet.updateNull(columnLabel); }
    @Override public void updateBoolean(String columnLabel, boolean x) throws SQLException { targetResultSet.updateBoolean(columnLabel, x); }
    @Override public void updateByte(String columnLabel, byte x) throws SQLException { targetResultSet.updateByte(columnLabel, x); }
    @Override public void updateShort(String columnLabel, short x) throws SQLException { targetResultSet.updateShort(columnLabel, x); }
    @Override public void updateInt(String columnLabel, int x) throws SQLException { targetResultSet.updateInt(columnLabel, x); }
    @Override public void updateLong(String columnLabel, long x) throws SQLException { targetResultSet.updateLong(columnLabel, x); }
    @Override public void updateFloat(String columnLabel, float x) throws SQLException { targetResultSet.updateFloat(columnLabel, x); }
    @Override public void updateDouble(String columnLabel, double x) throws SQLException { targetResultSet.updateDouble(columnLabel, x); }
    @Override public void updateBigDecimal(String columnLabel, BigDecimal x) throws SQLException { targetResultSet.updateBigDecimal(columnLabel, x); }
    @Override public void updateString(String columnLabel, String x) throws SQLException { targetResultSet.updateString(columnLabel, x); }
    @Override public void updateBytes(String columnLabel, byte[] x) throws SQLException { targetResultSet.updateBytes(columnLabel, x); }
    @Override public void updateDate(String columnLabel, Date x) throws SQLException { targetResultSet.updateDate(columnLabel, x); }
    @Override public void updateTime(String columnLabel, Time x) throws SQLException { targetResultSet.updateTime(columnLabel, x); }
    @Override public void updateTimestamp(String columnLabel, Timestamp x) throws SQLException { targetResultSet.updateTimestamp(columnLabel, x); }
    @Override public void updateAsciiStream(String columnLabel, InputStream x, int length) throws SQLException { targetResultSet.updateAsciiStream(columnLabel, x, length); }
    @Override public void updateBinaryStream(String columnLabel, InputStream x, int length) throws SQLException { targetResultSet.updateBinaryStream(columnLabel, x, length); }
    @Override public void updateCharacterStream(String columnLabel, Reader reader, int length) throws SQLException { targetResultSet.updateCharacterStream(columnLabel, reader, length); }
    @Override public void updateObject(String columnLabel, Object x, int scaleOrLength) throws SQLException { targetResultSet.updateObject(columnLabel, x, scaleOrLength); }
    @Override public void updateObject(String columnLabel, Object x) throws SQLException { targetResultSet.updateObject(columnLabel, x); }
    @Override public void insertRow() throws SQLException { targetResultSet.insertRow(); }
    @Override public void updateRow() throws SQLException { targetResultSet.updateRow(); }
    @Override public void deleteRow() throws SQLException { targetResultSet.deleteRow(); }
    @Override public void refreshRow() throws SQLException { targetResultSet.refreshRow(); }
    @Override public void cancelRowUpdates() throws SQLException { targetResultSet.cancelRowUpdates(); }
    @Override public void moveToInsertRow() throws SQLException { targetResultSet.moveToInsertRow(); }
    @Override public void moveToCurrentRow() throws SQLException { targetResultSet.moveToCurrentRow(); }
    @Override public Statement getStatement() throws SQLException { return targetResultSet.getStatement(); }
    @Override public Object getObject(int columnIndex, Map<String, Class<?>> map) throws SQLException { return targetResultSet.getObject(columnIndex, map); }
    @Override public Ref getRef(int columnIndex) throws SQLException { return targetResultSet.getRef(columnIndex); }
    @Override public Blob getBlob(int columnIndex) throws SQLException { return targetResultSet.getBlob(columnIndex); }
    @Override public Clob getClob(int columnIndex) throws SQLException { return targetResultSet.getClob(columnIndex); }
    @Override public Array getArray(int columnIndex) throws SQLException { return targetResultSet.getArray(columnIndex); }
    @Override public Object getObject(String columnLabel, Map<String, Class<?>> map) throws SQLException { return targetResultSet.getObject(columnLabel, map); }
    @Override public Ref getRef(String columnLabel) throws SQLException { return targetResultSet.getRef(columnLabel); }
    @Override public Blob getBlob(String columnLabel) throws SQLException { return targetResultSet.getBlob(columnLabel); }
    @Override public Clob getClob(String columnLabel) throws SQLException { return targetResultSet.getClob(columnLabel); }
    @Override public Array getArray(String columnLabel) throws SQLException { return targetResultSet.getArray(columnLabel); }
    @Override public Date getDate(int columnIndex, Calendar cal) throws SQLException { return targetResultSet.getDate(columnIndex, cal); }
    @Override public Date getDate(String columnLabel, Calendar cal) throws SQLException { return targetResultSet.getDate(columnLabel, cal); }
    @Override public Time getTime(int columnIndex, Calendar cal) throws SQLException { return targetResultSet.getTime(columnIndex, cal); }
    @Override public Time getTime(String columnLabel, Calendar cal) throws SQLException { return targetResultSet.getTime(columnLabel, cal); }
    @Override public Timestamp getTimestamp(int columnIndex, Calendar cal) throws SQLException { return targetResultSet.getTimestamp(columnIndex, cal); }
    @Override public Timestamp getTimestamp(String columnLabel, Calendar cal) throws SQLException { return targetResultSet.getTimestamp(columnLabel, cal); }
    @Override public URL getURL(int columnIndex) throws SQLException { return targetResultSet.getURL(columnIndex); }
    @Override public URL getURL(String columnLabel) throws SQLException { return targetResultSet.getURL(columnLabel); }
    @Override public void updateRef(int columnIndex, Ref x) throws SQLException { targetResultSet.updateRef(columnIndex, x); }
    @Override public void updateRef(String columnLabel, Ref x) throws SQLException { targetResultSet.updateRef(columnLabel, x); }
    @Override public void updateBlob(int columnIndex, Blob x) throws SQLException { targetResultSet.updateBlob(columnIndex, x); }
    @Override public void updateBlob(String columnLabel, Blob x) throws SQLException { targetResultSet.updateBlob(columnLabel, x); }
    @Override public void updateClob(int columnIndex, Clob x) throws SQLException { targetResultSet.updateClob(columnIndex, x); }
    @Override public void updateClob(String columnLabel, Clob x) throws SQLException { targetResultSet.updateClob(columnLabel, x); }
    @Override public void updateArray(int columnIndex, Array x) throws SQLException { targetResultSet.updateArray(columnIndex, x); }
    @Override public void updateArray(String columnLabel, Array x) throws SQLException { targetResultSet.updateArray(columnLabel, x); }
    @Override public RowId getRowId(int columnIndex) throws SQLException { return targetResultSet.getRowId(columnIndex); }
    @Override public RowId getRowId(String columnLabel) throws SQLException { return targetResultSet.getRowId(columnLabel); }
    @Override public void updateRowId(int columnIndex, RowId x) throws SQLException { targetResultSet.updateRowId(columnIndex, x); }
    @Override public void updateRowId(String columnLabel, RowId x) throws SQLException { targetResultSet.updateRowId(columnLabel, x); }
    @Override public int getHoldability() throws SQLException { return targetResultSet.getHoldability(); }
    @Override public boolean isClosed() throws SQLException { return targetResultSet.isClosed(); }
    @Override public void updateNString(int columnIndex, String nString) throws SQLException { targetResultSet.updateNString(columnIndex, nString); }
    @Override public void updateNString(String columnLabel, String nString) throws SQLException { targetResultSet.updateNString(columnLabel, nString); }
    @Override public void updateNClob(int columnIndex, NClob nClob) throws SQLException { targetResultSet.updateNClob(columnIndex, nClob); }
    @Override public void updateNClob(String columnLabel, NClob nClob) throws SQLException { targetResultSet.updateNClob(columnLabel, nClob); }
    @Override public NClob getNClob(int columnIndex) throws SQLException { return targetResultSet.getNClob(columnIndex); }
    @Override public NClob getNClob(String columnLabel) throws SQLException { return targetResultSet.getNClob(columnLabel); }
    @Override public SQLXML getSQLXML(int columnIndex) throws SQLException { return targetResultSet.getSQLXML(columnIndex); }
    @Override public SQLXML getSQLXML(String columnLabel) throws SQLException { return targetResultSet.getSQLXML(columnLabel); }
    @Override public void updateSQLXML(int columnIndex, SQLXML xmlObject) throws SQLException { targetResultSet.updateSQLXML(columnIndex, xmlObject); }
    @Override public void updateSQLXML(String columnLabel, SQLXML xmlObject) throws SQLException { targetResultSet.updateSQLXML(columnLabel, xmlObject); }
    @Override public String getNString(int columnIndex) throws SQLException { return targetResultSet.getNString(columnIndex); }
    @Override public String getNString(String columnLabel) throws SQLException { return targetResultSet.getNString(columnLabel); }
    @Override public Reader getNCharacterStream(int columnIndex) throws SQLException { return targetResultSet.getNCharacterStream(columnIndex); }
    @Override public Reader getNCharacterStream(String columnLabel) throws SQLException { return targetResultSet.getNCharacterStream(columnLabel); }
    @Override public void updateNCharacterStream(int columnIndex, Reader x, long length) throws SQLException { targetResultSet.updateNCharacterStream(columnIndex, x, length); }
    @Override public void updateNCharacterStream(String columnLabel, Reader reader, long length) throws SQLException { targetResultSet.updateNCharacterStream(columnLabel, reader, length); }
    @Override public void updateAsciiStream(int columnIndex, InputStream x, long length) throws SQLException { targetResultSet.updateAsciiStream(columnIndex, x, length); }
    @Override public void updateBinaryStream(int columnIndex, InputStream x, long length) throws SQLException { targetResultSet.updateBinaryStream(columnIndex, x, length); }
    @Override public void updateCharacterStream(int columnIndex, Reader x, long length) throws SQLException { targetResultSet.updateCharacterStream(columnIndex, x, length); }
    @Override public void updateAsciiStream(String columnLabel, InputStream x, long length) throws SQLException { targetResultSet.updateAsciiStream(columnLabel, x, length); }
    @Override public void updateBinaryStream(String columnLabel, InputStream x, long length) throws SQLException { targetResultSet.updateBinaryStream(columnLabel, x, length); }
    @Override public void updateCharacterStream(String columnLabel, Reader reader, long length) throws SQLException { targetResultSet.updateCharacterStream(columnLabel, reader, length); }
    @Override public void updateBlob(int columnIndex, InputStream inputStream, long length) throws SQLException { targetResultSet.updateBlob(columnIndex, inputStream, length); }
    @Override public void updateBlob(String columnLabel, InputStream inputStream, long length) throws SQLException { targetResultSet.updateBlob(columnLabel, inputStream, length); }
    @Override public void updateClob(int columnIndex, Reader reader, long length) throws SQLException { targetResultSet.updateClob(columnIndex, reader, length); }
    @Override public void updateClob(String columnLabel, Reader reader, long length) throws SQLException { targetResultSet.updateClob(columnLabel, reader, length); }
    @Override public void updateNClob(int columnIndex, Reader reader, long length) throws SQLException { targetResultSet.updateNClob(columnIndex, reader, length); }
    @Override public void updateNClob(String columnLabel, Reader reader, long length) throws SQLException { targetResultSet.updateNClob(columnLabel, reader, length); }
    @Override public void updateNCharacterStream(int columnIndex, Reader x) throws SQLException { targetResultSet.updateNCharacterStream(columnIndex, x); }
    @Override public void updateNCharacterStream(String columnLabel, Reader reader) throws SQLException { targetResultSet.updateNCharacterStream(columnLabel, reader); }
    @Override public void updateAsciiStream(int columnIndex, InputStream x) throws SQLException { targetResultSet.updateAsciiStream(columnIndex, x); }
    @Override public void updateBinaryStream(int columnIndex, InputStream x) throws SQLException { targetResultSet.updateBinaryStream(columnIndex, x); }
    @Override public void updateCharacterStream(int columnIndex, Reader x) throws SQLException { targetResultSet.updateCharacterStream(columnIndex, x); }
    @Override public void updateAsciiStream(String columnLabel, InputStream x) throws SQLException { targetResultSet.updateAsciiStream(columnLabel, x); }
    @Override public void updateBinaryStream(String columnLabel, InputStream x) throws SQLException { targetResultSet.updateBinaryStream(columnLabel, x); }
    @Override public void updateCharacterStream(String columnLabel, Reader reader) throws SQLException { targetResultSet.updateCharacterStream(columnLabel, reader); }
    @Override public void updateBlob(int columnIndex, InputStream inputStream) throws SQLException { targetResultSet.updateBlob(columnIndex, inputStream); }
    @Override public void updateBlob(String columnLabel, InputStream inputStream) throws SQLException { targetResultSet.updateBlob(columnLabel, inputStream); }
    @Override public void updateClob(int columnIndex, Reader reader) throws SQLException { targetResultSet.updateClob(columnIndex, reader); }
    @Override public void updateClob(String columnLabel, Reader reader) throws SQLException { targetResultSet.updateClob(columnLabel, reader); }
    @Override public void updateNClob(int columnIndex, Reader reader) throws SQLException { targetResultSet.updateNClob(columnIndex, reader); }
    @Override public void updateNClob(String columnLabel, Reader reader) throws SQLException { targetResultSet.updateNClob(columnLabel, reader); }
    @Override public <T> T getObject(int columnIndex, Class<T> type) throws SQLException { return targetResultSet.getObject(columnIndex, type); }
    @Override public <T> T getObject(String columnLabel, Class<T> type) throws SQLException { return targetResultSet.getObject(columnLabel, type); }
}
