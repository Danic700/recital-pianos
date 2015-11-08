package db.mysql;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;
import db.valueObjects.Post;
import java.util.ArrayList;
import java.sql.PreparedStatement;

public class PostsDAO extends BaseDAO {

    private static final String TABLE_NAME = "Posts";
    public static final String COL_ID = "id";
    public static final String COL_USERNAME = "username";
    public static final String COL_TEXT = "text";
    public static final String COL_DATE = "date";
    public static final String COL_NAME = "name";
    public static final String COL_EMAIL = "email";
    public static final String COL_PHONE = "phone";
    public static final String COL_IMG_URL = "img";

    private static final String CREATE_TABLE = "CREATE TABLE IF NOT EXISTS " + TABLE_NAME + " (`"
            + COL_ID + "` INT NOT NULL AUTO_INCREMENT, `"
            + COL_USERNAME + "` VARCHAR(30) NOT NULL , `"
            + COL_TEXT + "` VARCHAR(1000) NOT NULL , `"
            + COL_DATE + "` TIMESTAMP DEFAULT CURRENT_TIMESTAMP , `"
            + COL_NAME + "` VARCHAR(20) NOT NULL , `"
            + COL_EMAIL + "` VARCHAR(30) NULL , `"
            + COL_PHONE + "` VARCHAR(15) NULL , `"
            + COL_IMG_URL + "` VARCHAR(256) NOT NULL, "
            + "PRIMARY KEY (" + COL_ID + ")) ENGINE = InnoDB CHARACTER SET utf8 COLLATE utf8_bin;";

    public PostsDAO() {
        super(TABLE_NAME);
        createTableIfNotExists(CREATE_TABLE);
    }

    public List<Post> getPosts(int numOfPosts, int offset) throws SQLException {
        Statement stmt = conn.createStatement();
        ResultSet rs = stmt.executeQuery("SELECT * FROM " + TABLE_NAME + " ORDER BY " + getCol(COL_DATE) + " DESC LIMIT " + numOfPosts + " OFFSET " + offset + ";");
        return resultSetAsVO(rs);
    }

    public List<Post> getAllPosts() throws SQLException {
        Statement stmt = conn.createStatement();
        ResultSet rs = stmt.executeQuery("SELECT * FROM " + TABLE_NAME + " ORDER BY " + getCol(COL_DATE) + " DESC;");
        return resultSetAsVO(rs);
    }

    public List<Post> getPostByUsername(String username) throws SQLException {
        Statement stmt = conn.createStatement();
        ResultSet rs = stmt.executeQuery("SELECT * FROM " + TABLE_NAME + " WHERE " + getCol(COL_USERNAME) + " = \"" + username + "\";");
        System.out.println("Select " + TABLE_NAME + "Record");
        return resultSetAsVO(rs);
    }

    public List<Post> getPostById(String id) throws SQLException {
        Statement stmt = conn.createStatement();
        ResultSet rs = stmt.executeQuery("SELECT * FROM " + TABLE_NAME + " WHERE " + getCol(COL_ID) + " = " + id + ";");
        System.out.println("Select " + TABLE_NAME + "Record");
        return resultSetAsVO(rs);
    }

    public Post insert(Post post) throws SQLException {
        String SQL = "INSERT INTO "
                + TABLE_NAME
                + " (`" + COL_USERNAME + "`, `" + COL_TEXT + "`, `" + COL_NAME + "`, `" + COL_EMAIL + "`, `" + COL_PHONE + "`, `" + COL_IMG_URL + "`)"
                + " VALUES (\"" + post.username + "\", \"" + post.text + "\", \"" + post.name + "\", \"" + post.email + "\", \"" + post.phone + "\", \"" + post.img + "\");";
        System.out.println("insert SQL=" + SQL);

        Statement st = conn.createStatement();
        st.executeUpdate(SQL, Statement.RETURN_GENERATED_KEYS);
        ResultSet rs = st.getGeneratedKeys();

        if (rs.next()) {
            post.id = rs.getInt(1);
        }

        post = getPostById(String.valueOf(post.id)).get(0);

        System.out.println("Created " + TABLE_NAME + "Record");

        return post;
    }

    public boolean removePostByID(String id, String userName) throws SQLException {
        String userValidCondition = " AND " + getCol(COL_USERNAME) + " = \"" + userName + "\"";
        return removePostByIdHelper(id, userValidCondition);
    }

    public boolean removePostByID(String id) throws SQLException {
        return removePostByIdHelper(id, "");
    }

    private boolean removePostByIdHelper(String id, String userValidCondition) throws SQLException {
        String SQL = "DELETE FROM " + TABLE_NAME + " WHERE " + getCol(COL_ID) + " = " + id + userValidCondition + ";";
        System.out.println("delete SQL=" + SQL);

        PreparedStatement preparedStatement = conn.prepareStatement(SQL);
        
        if (preparedStatement.executeUpdate() != 0) {
            System.out.println("Delete " + TABLE_NAME + "Record");
            return true;
        }
        
        return false;
    }

    @Override
    public List<Post> resultSetAsVO(ResultSet rs) throws SQLException {
        List<Post> output = new ArrayList<>();
        while (rs.next()) {
            Post post = new Post();
            post.id = (rs.getInt(COL_ID));
            post.username = (rs.getString(COL_USERNAME));
            post.text = (rs.getString(COL_TEXT));
            post.date = (rs.getTimestamp(COL_DATE));
            post.name = (rs.getString(COL_NAME));
            post.email = (rs.getString(COL_EMAIL));
            post.phone = (rs.getString(COL_PHONE));
            post.img = (rs.getString(COL_IMG_URL));
            output.add(post);
        }
        return output;
    }
}
