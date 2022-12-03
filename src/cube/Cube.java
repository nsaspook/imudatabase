/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cube;

import java.sql.*;
import java.util.Properties;

/**
 *
 * @author root
 */
public class Cube {

    @SuppressWarnings("ConvertToTryWithResources")
    public static void main(String[] args) {

        // database IP configurations
        String mysql_host = "jdbc:mariadb://10.1.1.172/";
//        String mysql_host = "jdbc:mariadb://10.5.2.94/";

        Properties connConfig = new Properties();
        connConfig.setProperty("user", "minty");
        connConfig.setProperty("password", "");

        Statement stmt = null;
        ResultSet rs = null;

        try {
            Connection conn = DriverManager.getConnection(mysql_host, connConfig);

            stmt = conn.createStatement();

            stmt.executeUpdate("USE hits");

            rs = stmt.executeQuery(
                    "SELECT timestamp "
                    + "FROM imu "
                    + "order by timestamp ASC");

            stmt.executeUpdate("DROP TABLE IF EXISTS vevent");
            stmt.executeUpdate(
                    "CREATE TABLE vevent ("
                    + "priKey INT NOT NULL AUTO_INCREMENT, "
                    + "dataField VARCHAR(255), PRIMARY KEY (priKey))");

            stmt.executeUpdate(
                    "INSERT INTO vevent (dataField) "
                    + "select (str) "
                    + "from imu "
                    + "where dtype like '  1' AND fft > '  9' LIMIT 2000", 
                    Statement.RETURN_GENERATED_KEYS);

            conn.close();
        } catch (SQLException ex) {
            // handle any errors
            System.out.println("SQLException: " + ex.getMessage());
            System.out.println("SQLState: " + ex.getSQLState());
            System.out.println("VendorError: " + ex.getErrorCode());
        }

    }

}
