package db.mysql;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import db.valueObjects.Piano;
import java.sql.PreparedStatement;
import javafx.util.Pair;

public class PianosDAO extends BaseDAO {

    private static final String TABLE_PIANO_NAME = "Pianos";
    private static final String TABLE_MANUFACTURER_NAME = "Manufacturers";

    public static final String COL_PIANO_ID = "pianoId";
    public static final String COL_IS_UPRIGHT = "isUpright";
    public static final String COL_MODEL = "model";
    public static final String COL_SIZE = "size";
    public static final String COL_COLOR = "color";
    public static final String COL_FINISH = "finish";
    public static final String COL_YEAR = "year";
    public static final String COL_IS_NEW = "isNew";
    public static final String COL_IMG_URL = "imgUrl";

    private static final String COL_MANUFACTURER_ID = "manufacturerId";
    public static final String COL_MANUFACTURER = "manufacturers";
    public static final String COL_COUNTRY = "country";
    private static final String COL_REF_NUM = "referenceNumber";//TODO ref

    private static final String CREATE_PIANO_TABLE = "CREATE TABLE IF NOT EXISTS " + TABLE_PIANO_NAME + " (`"
            + COL_PIANO_ID + "` INT NOT NULL AUTO_INCREMENT, `"
            + COL_IS_UPRIGHT + "` BIT(1) NOT NULL , `"
            + COL_MODEL + "` VARCHAR(30) , `"
            + COL_MANUFACTURER_ID + "` INT NOT NULL , `"
            + COL_SIZE + "` SMALLINT NOT NULL , `"
            + COL_COLOR + "` VARCHAR(30) NOT NULL , `"
            + COL_FINISH + "` VARCHAR(30) NOT NULL , `"
            + COL_YEAR + "` SMALLINT , `"
            + COL_IS_NEW + "` BIT(1) NOT NULL , `"
            + COL_IMG_URL + "` VARCHAR(256) NOT NULL, "
            + "PRIMARY KEY (" + COL_PIANO_ID + "), FOREIGN KEY (" + COL_MANUFACTURER_ID + ") REFERENCES " + TABLE_MANUFACTURER_NAME + "(" + COL_MANUFACTURER_ID + "))  ENGINE = InnoDB CHARACTER SET utf8 COLLATE utf8_bin;";

    private final MfrDAO mfrDAO;

    public PianosDAO() {
        super(TABLE_PIANO_NAME);
        mfrDAO = new MfrDAO();
        createTableIfNotExists(CREATE_PIANO_TABLE);
    }

    public List<Piano> getAllPianosList() throws SQLException {
        Statement stmt = conn.createStatement();
        String mfrCompare = mfrDAO.getCol(COL_MANUFACTURER_ID) + " = " + getCol(COL_MANUFACTURER_ID);
        ResultSet rs = stmt.executeQuery(
                "SELECT * FROM " + TABLE_PIANO_NAME + " JOIN " + TABLE_MANUFACTURER_NAME + " ON " + mfrCompare + ";");
        return resultSetAsVO(rs);
    }

    public List<Piano> getUprightPianosList() throws SQLException {
        return getPianosList(true);
    }

    public List<Piano> getGrandPianosList() throws SQLException {
        return getPianosList(false);
    }

    private List<Piano> getPianosList(boolean isUpright) throws SQLException {
        Statement stmt = conn.createStatement();
        String mfrCompare = mfrDAO.getCol(COL_MANUFACTURER_ID) + " = " + getCol(COL_MANUFACTURER_ID);
        ResultSet rs = stmt.executeQuery(
                "SELECT * FROM " + TABLE_PIANO_NAME + " JOIN " + TABLE_MANUFACTURER_NAME + " ON " + mfrCompare
                + " WHERE " + getCol(COL_IS_UPRIGHT) + " = " + (isUpright ? "1;" : "0;"));
        return resultSetAsVO(rs);
    }

    public List<Piano> getPianoById(int pinaoId) throws SQLException {
        Statement stmt = conn.createStatement();
        String mfrCompare = mfrDAO.getCol(COL_MANUFACTURER_ID) + " = " + getCol(COL_MANUFACTURER_ID);
        ResultSet rs = stmt.executeQuery(
                "SELECT * FROM " + TABLE_PIANO_NAME + " JOIN " + TABLE_MANUFACTURER_NAME + " ON " + mfrCompare
                + " WHERE " + getCol(COL_PIANO_ID) + " = " + pinaoId);
        return resultSetAsVO(rs);
    }

    @Override
    public List<Piano> resultSetAsVO(ResultSet rs) throws SQLException {
        List<Piano> output = new ArrayList<>();
        while (rs.next()) {
            Piano piano = new Piano();
            piano.id = (rs.getInt(COL_PIANO_ID));
            piano.isUpright = (rs.getBoolean(COL_IS_UPRIGHT));
            piano.model = (rs.getString(COL_MODEL));
            piano.manufacturer = (rs.getString(COL_MANUFACTURER));
            piano.country = (rs.getString(COL_COUNTRY));
            piano.size = (rs.getInt(COL_SIZE));
            piano.color = (rs.getString(COL_COLOR));
            piano.finish = (rs.getString(COL_FINISH));
            piano.year = (rs.getInt(COL_YEAR));
            piano.isNew = (rs.getBoolean(COL_IS_NEW));
            piano.img = (rs.getString(COL_IMG_URL));
            output.add(piano);
        }
        return output;
    }

    public void insert(Piano piano) throws SQLException {

        int mfrID = mfrDAO.insert(piano.manufacturer, piano.country);
        String SQL = "INSERT INTO "
                + TABLE_PIANO_NAME
                + " (`" + COL_IS_UPRIGHT + "`, `" + COL_MODEL + "`, `" + COL_MANUFACTURER_ID + "`, `" + COL_SIZE + "`, `" + COL_COLOR + "`, `" + COL_FINISH + "`, `" + COL_YEAR + "`, `" + COL_IS_NEW + "`, `" + COL_IMG_URL + "`)"
                + " VALUES (?,?,?,?,?,?,?,?,?);";
        System.out.println("insert SQL=" + SQL);

        PreparedStatement preparedStatement = conn.prepareStatement(SQL);
        preparedStatement.setBoolean(1, piano.isUpright);
        preparedStatement.setString(2, piano.model);
        preparedStatement.setInt(3, mfrID);
        preparedStatement.setInt(4, piano.size);
        preparedStatement.setString(5, piano.color);
        preparedStatement.setString(6, piano.finish);
        preparedStatement.setInt(7, piano.year);
        preparedStatement.setBoolean(8, piano.isNew);
        preparedStatement.setString(9, piano.img);
        preparedStatement.executeUpdate();

        System.out.println("Created " + TABLE_PIANO_NAME + "Record");
    }

    public boolean remove(String id) throws SQLException {
        String SQL = "DELETE FROM " + TABLE_PIANO_NAME + " WHERE " + getCol(COL_PIANO_ID) + " = " + id + ";";
        System.out.println("delete SQL=" + SQL);
        PreparedStatement preparedStatement = conn.prepareStatement(SQL);
        if (preparedStatement.executeUpdate() != 0) {
            mfrDAO.deleteIfNoReference();
            System.out.println("Delete " + TABLE_PIANO_NAME + "Record");

            return true;
        }

        return false;
    }

    //nested class to represent the manufacturers DB
    private class MfrDAO extends BaseDAO {

        private static final String CREATE_MANUFACTURERS_TABLE = "CREATE TABLE IF NOT EXISTS " + TABLE_MANUFACTURER_NAME + " (`"
                + COL_MANUFACTURER_ID + "` INT NOT NULL AUTO_INCREMENT, `"
                + COL_MANUFACTURER + "` VARCHAR(30) NOT NULL , `"
                + COL_COUNTRY + "` VARCHAR(30) NOT NULL , "
                + "PRIMARY KEY (" + COL_MANUFACTURER_ID + ")) ENGINE = InnoDB CHARACTER SET utf8 COLLATE utf8_bin;";

        public MfrDAO() {
            super(TABLE_MANUFACTURER_NAME);
            createTableIfNotExists(CREATE_MANUFACTURERS_TABLE);
        }

        public int insert(String manufacturer, String country) throws SQLException {
            if (manufacturer == null || country == null) {
                throw new SQLException("A problem while creating the piano, manufacturer and country can't be null");
            }
            int mfrId;
            String SQL = "SELECT " + COL_MANUFACTURER_ID
                    + " FROM " + TABLE_MANUFACTURER_NAME
                    + " WHERE " + getCol(COL_MANUFACTURER) + " = \"" + manufacturer + "\""
                    + " AND " + getCol(COL_COUNTRY) + " = \"" + country + "\";";
            Statement st = conn.createStatement();
            ResultSet rs = st.executeQuery(SQL);
            if (rs.next()) {
                mfrId = rs.getInt(COL_MANUFACTURER_ID);
                System.out.println("Record in " + TABLE_MANUFACTURER_NAME + " already exists");
            } else {
                SQL = "INSERT INTO " + TABLE_MANUFACTURER_NAME
                        + " (`" + COL_MANUFACTURER + "`, `" + COL_COUNTRY + "`)"
                        + " VALUES ('" + manufacturer + "', '" + country + "');";
                System.out.println("insert SQL=" + SQL);
                st.executeUpdate(SQL, Statement.RETURN_GENERATED_KEYS);
                rs = st.getGeneratedKeys();
                if (rs.next()) {
                    mfrId = rs.getInt(1);
                } else {
                    throw new SQLException("A problem while creating the id for the new manufacturer");
                }
                System.out.println("Created " + TABLE_MANUFACTURER_NAME + "Record");
            }
            return mfrId;
        }

        public void deleteIfNoReference() throws SQLException {
            String SQL = "DELETE FROM " + TABLE_MANUFACTURER_NAME
                    + " WHERE " + getCol(COL_MANUFACTURER_ID) + " NOT IN ("
                    + " SELECT " + TABLE_PIANO_NAME + "." + COL_MANUFACTURER_ID + " FROM " + TABLE_PIANO_NAME + ");";
            System.out.println("delete SQL=" + SQL);

            PreparedStatement preparedStatement = conn.prepareStatement(SQL);
            preparedStatement.executeUpdate();

            System.out.println("Delete " + TABLE_MANUFACTURER_NAME + "Record");
        }

        @Override
        public Pair<String, String> resultSetAsVO(ResultSet rs) throws SQLException {
            rs.next();
            return new Pair<>(rs.getString(COL_MANUFACTURER), rs.getString(COL_COUNTRY));
        }
    }
}
