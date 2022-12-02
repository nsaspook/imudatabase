/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cube;

import java.util.Scanner;
import java.sql.*;
import java.util.Properties;
import java.net.Socket;

/**
 *
 * @author root
 */
public class Cube {

    public static void main(String[] args) {

        // source and dest IP configurations
        String ttl_eth_host = "10.1.1.238";
        String mysql_host = "jdbc:mariadb://10.1.1.172/";

//        String ttl_eth_host = "192.168.0.7";
//        String mysql_host = "jdbc:mariadb://10.5.2.94/";

        try {
            // Connect to the TTL to ETH server
            Socket socket = new Socket(ttl_eth_host, 20108);

            Properties connConfig = new Properties();
            connConfig.setProperty("user", "minty");
            connConfig.setProperty("password", "");

            Scanner s = new Scanner(socket.getInputStream());
            System.err.println("Scanner running.");
            while (s.hasNextLine()) {
                String fftd = "";
                String tstamp = "";
                String hosts = "";
                String cmark = "";
                try {
                    String line = s.nextLine();
                    String[] token = line.split(",");

                    if (token[0].equals("  1")) {
                        tstamp = token[11];
                        cmark = token[12];
                        System.out.println(String.format("dtype = %3s  device = %s : %s", token[0], token[1], line, tstamp));
                        token[2] = "";
                    }
                    if (token[0].equals("  2")) {
                        System.out.println(String.format("dtype = %3s  device = %s cpu = %s host = %s : %s", token[0], token[1], token[2], token[6], line));
                        hosts = token[6];
                        cmark = token[7];
                        token[6] = "";
                        token[7] = "";
                    }
                    if (token[0].equals("  8")) {
                        System.out.println(String.format("dtype = %3s  device = %s hit  low = %s : %s", token[0], token[1], token[2], line));
                        fftd = token[2];
                        cmark = token[3];
                        token[2] = "";
                        token[3] = "";
                    }
                    if (token[0].equals("  9")) {
                        System.out.println(String.format("dtype = %3s  device = %s hit high = %s : %s", token[0], token[1], token[2], line));
                        fftd = token[2];
                        cmark = token[3];
                        token[2] = "";
                        token[3] = "";
                    }

                    try {
                        Connection conn = DriverManager.getConnection(mysql_host, connConfig);

                        if (cmark.equals("IMU")) {
                            // Prepare INSERT Statement to Add IMU data
                            try ( PreparedStatement prep = conn.prepareStatement(
                                    "INSERT INTO hits.imu (dtype, device, cpu, str, fft, tstamp, host) VALUES (?, ?, ?, ?, ?, ?, ?)",
                                    Statement.RETURN_GENERATED_KEYS)) {
                                // Add IMU packet data
                                prep.setString(1, token[0]);
                                prep.setString(2, token[1]);
                                prep.setString(3, token[2]);
                                prep.setString(4, line);
                                prep.setString(5, fftd);
                                prep.setString(6, tstamp);
                                prep.setString(7, hosts);
                                prep.addBatch();

                                int[] updateCounts = prep.executeBatch();
                                for (int count : updateCounts) {
                                    // Print Counts
//                            System.out.println(count);
                                }
                            }
                        }
                        conn.close();
                    } catch (SQLException ex) {
                        // handle any errors
                        System.out.println("SQLException: " + ex.getMessage());
                        System.out.println("SQLState: " + ex.getSQLState());
                        System.out.println("VendorError: " + ex.getErrorCode());
                    }
                } catch (Exception e) {
                }
            }
            // Close our streams
            socket.close();
            s.close();
            System.err.println("Lost communication with the IMU socket. Exiting.");
            System.exit(1);

        } catch (Exception e) {
            System.err.println("Unable to open the TCP socket. Exiting.");
            System.exit(1);
        }
    }

}
