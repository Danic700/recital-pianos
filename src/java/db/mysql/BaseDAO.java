package db.mysql;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public abstract class BaseDAO {

    protected final String tableName;
    protected final Connection conn;

    public BaseDAO(String tableName) {
        this.tableName = tableName;
        this.conn = MySqlConn.getConnection();
    }

    public String getCol(String colName) {
        return tableName + "." + colName;
    }

    protected void createTableIfNotExists(String createTableQuerry) {
        try {
            Statement st = conn.createStatement();
            st.executeUpdate("create database if not exists " + MySqlConn.DB_NAME + ";");
            st.executeUpdate("use " + MySqlConn.DB_NAME + ";");
            st.executeUpdate(createTableQuerry);
            System.out.println("Created the table=" + createTableQuerry);
        } catch (SQLException ex) {
            System.out.println("FAILED to create the table=" + createTableQuerry);
            System.err.println(ex.getMessage());
        }
    }

    public abstract Object resultSetAsVO(ResultSet rs) throws SQLException;
}
