package db.mysql;

import db.valueObjects.Bench;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class BenchesDAO extends BaseDAO {

    private static final String TABLE_NAME = "Benches";
    private static final String COL_BENCH_ID = "benchId";
    private static final String COL_MODEL = "model";
    private static final String COL_IS_ADJUSTABLE = "isAdjustable";
    private static final String COL_COLOR = "color";
    private static final String COL_FABRIC = "fabric";
    private static final String COL_IMG_URL = "imgUrl";

    private static final String CREATE_TABLE = "CREATE TABLE IF NOT EXISTS " + TABLE_NAME + " (`"
            + COL_BENCH_ID + "` INT NOT NULL AUTO_INCREMENT, `"
            + COL_MODEL + "` VARCHAR(64) NOT NULL , `"
            + COL_IS_ADJUSTABLE + "` BIT NOT NULL , `"
            + COL_COLOR + "` VARCHAR(30) NOT NULL , `"
            + COL_FABRIC + "` VARCHAR(30) NOT NULL , `"
            + COL_IMG_URL + "` VARCHAR(256) NOT NULL, "
            + "PRIMARY KEY (" + COL_BENCH_ID + ")) ENGINE = InnoDB CHARACTER SET utf8 COLLATE utf8_bin;";

    public BenchesDAO() {
        super(TABLE_NAME);
        createTableIfNotExists(CREATE_TABLE);
    }

    public List<Bench> getBenchList() throws SQLException {
        List<Bench> benchList;
        String SQL = "SELECT * FROM " + TABLE_NAME;
        try {
            Statement st = conn.createStatement();
            ResultSet rs = st.executeQuery(SQL);
            benchList = resultSetAsVO(rs);
        } catch (SQLException ex) {
            System.err.println(ex.getMessage());
            benchList = new ArrayList<>();
        }
        return benchList;
    }

    public void insert(Bench bench) throws SQLException {

        String SQL = "INSERT INTO "
                + TABLE_NAME
                + " (`" + COL_MODEL + "`, `" + COL_IS_ADJUSTABLE + "`, `" + COL_COLOR + "`, `" + COL_FABRIC + "`, `" + COL_IMG_URL + "`)"
                + " VALUES (?,?,?,?,?);";
        System.out.println("insert SQL=" + SQL);

        PreparedStatement preparedStatement = conn.prepareStatement(SQL);
        preparedStatement.setString(1, bench.model);
        preparedStatement.setBoolean(2, bench.isAdjustable);
        preparedStatement.setString(3, bench.color);
        preparedStatement.setString(4, bench.fabric);
        preparedStatement.setString(5, bench.img);
        preparedStatement.executeUpdate();

        System.out.println("Created " + TABLE_NAME + "Record");
    }

    public boolean remove(String id) throws SQLException {
        String SQL = "DELETE FROM " + TABLE_NAME + " WHERE " + getCol(COL_BENCH_ID) + " = " + id + ";";
        System.out.println("delete SQL=" + SQL);

        PreparedStatement preparedStatement = conn.prepareStatement(SQL);
        if (preparedStatement.executeUpdate() != 0) {
            System.out.println("Delete " + TABLE_NAME + "Record");
            return true;
        }
        return false;
    }

    @Override
    public List<Bench> resultSetAsVO(ResultSet rs) throws SQLException {
        List<Bench> output = new ArrayList<>();
        while (rs.next()) {
            Bench bench = new Bench();
            bench.id = (rs.getInt(COL_BENCH_ID));
            bench.model = (rs.getString(COL_MODEL));
            bench.color = (rs.getString(COL_COLOR));
            bench.fabric = (rs.getString(COL_FABRIC));
            bench.img = (rs.getString(COL_IMG_URL));
            bench.isAdjustable = (rs.getBoolean(COL_IS_ADJUSTABLE));
            output.add(bench);
        }
        return output;
    }
}
