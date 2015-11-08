package db.mysql;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

class MySqlConn {

    public static final String DB_NAME = "recital_pianos";

    private static Connection conn;
    /* user name and pwd only for local testing*/
    private static final String USER_NAME = "root";
    private static final String PASSWORD = "";


    static {

        try {
            System.out.println("Loading the driver...");
            Class.forName("com.mysql.jdbc.Driver");
            System.out.println("Loading successed  ---------------");

            String jsonEnvVars = java.lang.System.getenv("VCAP_SERVICES");
            if (jsonEnvVars != null) {
                parseUrlFromEnvVarsAndConnect(jsonEnvVars);
            } else {
                //Runs locally - only for maintenance 
                String url = "jdbc:mysql://localhost";
                System.out.println("Connected local host url=" + url);
                conn = DriverManager.getConnection(url, USER_NAME, PASSWORD);
            }

            System.out.println((new StringBuilder("conn successed. conn=")).append(conn).toString());
        } catch (ClassNotFoundException | SQLException ex) {
            System.err.println((new StringBuilder("error loading:")).append(ex.getMessage()).toString());
        }
    }

    private static void parseUrlFromEnvVarsAndConnect(String jsonEnvVars) {
        String url = "";
        try {
            JSONObject jsonObject = new JSONObject(jsonEnvVars);
            JSONArray jsonArray = jsonObject.getJSONArray("mysql-5.1");
            jsonObject = jsonArray.getJSONObject(0);
            jsonObject = jsonObject.getJSONObject("credentials");
            String host = jsonObject.getString("host");
            System.out.println("parseUrlFromEnvVarsAndConnect host=" + host);
            String port = jsonObject.getString("port");
            System.out.println("parseUrlFromEnvVarsAndConnect port=" + port);
            String dbName = jsonObject.getString("name");
            System.out.println("parseUrlFromEnvVarsAndConnect dbName=" + dbName);
            String username = jsonObject.getString("username");
            System.out.println("parseUrlFromEnvVarsAndConnect username=" + username);
            String password = jsonObject.getString("password");

            System.out.println("parseUrlFromEnvVarsAndConnect password=" + password);

            url = "jdbc:mysql://" + host + ":" + port + "/" + dbName;
            conn = DriverManager.getConnection(url, username, password);
        } catch (JSONException | SQLException e) {
            System.err.println("Conn.connect: " + e.getMessage());
        }
    }

    public static Connection getConnection() {
        return conn;
    }
}
