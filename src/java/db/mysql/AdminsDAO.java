package db.mysql;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class AdminsDAO extends BaseDAO {

    private static final String TABLE_NAME = "Admins";
    private static final String COL_EMAIL = "email";

    private static final String CREATE_TABLE
            = "CREATE TABLE IF NOT EXISTS " + TABLE_NAME + " (`"
            + COL_EMAIL + "` VARCHAR(30) NOT NULL ,  "
            + "PRIMARY KEY (" + COL_EMAIL + "), "
            + "FOREIGN KEY (" + COL_EMAIL + ") REFERENCES " + UsersDAO.TABLE_NAME + "(" + UsersDAO.COL_EMAIL + "))  ENGINE = InnoDB CHARACTER SET utf8 COLLATE utf8_bin;";

    public AdminsDAO() {
        super(TABLE_NAME);
        createTableIfNotExists(CREATE_TABLE);
    }

    public boolean isAdmin(String email) throws SQLException {
        Statement stmt = conn.createStatement();
        String SQL = "SELECT * FROM " + TABLE_NAME
                + " WHERE " + COL_EMAIL + " = \"" + email + "\";";

        ResultSet rs = stmt.executeQuery(SQL);
        return rs.next();
    }

    public boolean insert(String email) throws SQLException {
        String SQL = "INSERT INTO "
                + TABLE_NAME
                + " (" + COL_EMAIL + ")"
                + " VALUES (?);";
        System.out.println("insert SQL=" + SQL);

        PreparedStatement preparedStatement = conn.prepareStatement(SQL);
        preparedStatement.setString(1, email);
        if (preparedStatement.executeUpdate() == 1) {
            System.out.println("Created " + TABLE_NAME + "Record");
            return true;
        }
        return false;
    }

    public boolean remove(String email) throws SQLException {
        String SQL = "DELETE FROM " + TABLE_NAME + " WHERE " + getCol(COL_EMAIL) + " = \"" + email + "\";";
        System.out.println("delete SQL=" + SQL);

        PreparedStatement preparedStatement = conn.prepareStatement(SQL);
        if (preparedStatement.executeUpdate() == 1) {
            System.out.println("Delete " + TABLE_NAME + "Record");
            return true;
        }
        return false;
    }

    @Override
    public String resultSetAsVO(ResultSet rs) throws SQLException {
        return (rs.getString(COL_EMAIL));
    }
}
