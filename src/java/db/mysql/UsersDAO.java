package db.mysql;

import db.valueObjects.User;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public final class UsersDAO extends BaseDAO {

    public static final String TABLE_NAME = "Users";
    public static final String COL_EMAIL = "email";
    private static final String COL_PASSWORD = "password";

    private static final String CREATE_TABLE = 
            "CREATE TABLE IF NOT EXISTS " + TABLE_NAME + " (`" + 
            COL_EMAIL + "` VARCHAR(30) NOT NULL , `" + 
            COL_PASSWORD + "` VARCHAR(128) NOT NULL, " +
            "PRIMARY KEY (" + COL_EMAIL + ")) ENGINE = InnoDB CHARACTER SET utf8 COLLATE utf8_bin;";

    public UsersDAO() {
        super(TABLE_NAME);
        createTableIfNotExists(CREATE_TABLE);
    }

    public boolean isValid(String username, String password) throws SQLException {
        Statement stmt = conn.createStatement();
        ResultSet rs = stmt.executeQuery(
                "SELECT * FROM " + TABLE_NAME
                + " WHERE " + getCol(COL_EMAIL) + " = \"" + username + "\""
                + " AND " + getCol(COL_PASSWORD) + " = \"" + password + "\";");
        return rs.next();
    }

    public void insert(String email, String password) throws SQLException {
        String SQL = "INSERT INTO "
                + TABLE_NAME
                + " (`" + COL_EMAIL + "`, `" + COL_PASSWORD + "`)"
                + " VALUES (?,?);";
        System.out.println("insert SQL=" + SQL);

        PreparedStatement preparedStatement = conn.prepareStatement(SQL);
        preparedStatement.setString(1, email);
        preparedStatement.setString(2, password);
        preparedStatement.executeUpdate();

        System.out.println("Created " + TABLE_NAME + "Record");
    }

    public void remove(String email) throws SQLException {
        String SQL = "DELETE FROM " + TABLE_NAME + " WHERE " + getCol(COL_EMAIL) + " = \"" + email + "\";";
        System.out.println("delete SQL=" + SQL);

        PreparedStatement preparedStatement = conn.prepareStatement(SQL);
        preparedStatement.executeUpdate();

        System.out.println("Delete " + TABLE_NAME + "Record");
    }

    public User getUserByEmail(String email) throws SQLException {
        Statement stmt = conn.createStatement();
        ResultSet rs = stmt.executeQuery("SELECT FROM " + TABLE_NAME + " WHERE " + COL_EMAIL + " = \"" + email + "\";");
        System.out.println("Delete " + TABLE_NAME + "Record");

        return resultSetAsVO(rs);
    }

    @Override
    public User resultSetAsVO(ResultSet rs) throws SQLException {
        User user = new User();

        user.username = (rs.getString(COL_EMAIL));
        user.password = (rs.getString(COL_PASSWORD));

        return user;
    }

    public void updatePassword(String username, String newPassword) throws SQLException {
        String SQL = 
                 "UPDATE " + TABLE_NAME +
                " SET " + COL_PASSWORD + " = \"" + newPassword + "\"" +
                " WHERE " + getCol(COL_EMAIL) + " = \"" + username + "\";";
        System.out.println("update SQL=" + SQL);

        PreparedStatement preparedStatement = conn.prepareStatement(SQL);
        preparedStatement.executeUpdate();

        System.out.println("Upadted " + TABLE_NAME + "Record");
    }
}
