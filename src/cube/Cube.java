/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cube;


import java.util.Scanner;
import com.fazecast.jSerialComm.SerialPort;

import java.sql.*;
import java.util.Properties;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 *
 * @author root
 */
public class Cube {

    public static void main(String[] args) {

        //serial connection
        SerialPort port = SerialPort.getCommPort("ttyACM0"); // USB serial connection
        port.setBaudRate(115200);
        port.setComPortTimeouts(SerialPort.TIMEOUT_SCANNER, 1, 1);
        if (port.openPort() == false) {
            System.err.println("Unable to open the serial port. Exiting.");
            System.exit(1);
        }

        Properties connConfig = new Properties();
        connConfig.setProperty("user", "minty");
        connConfig.setProperty("password", "");

        Scanner s = new Scanner(port.getInputStream()); // eat first line
        s = new Scanner(port.getInputStream());
        System.err.println("Scanner.");
        while (s.hasNextLine()) {
                   String fftd = "";
            try {

                String line = s.nextLine();
                String[] token = line.split(",");
//                System.err.println(token[1]);

                if (token[0].equals("  1")) {
                    System.out.println(String.format("dtype = %3s  device = %s : %s", token[0], token[1], line));
                    token[2] = "";
                }
                if (token[0].equals("  2")) {
                    System.out.println(String.format("dtype = %3s  device = %s cpu = %s : %s", token[0], token[1], token[2], line));
                }
                if (token[0].equals("  8")) {
                    System.out.println(String.format("dtype = %3s  device = %s hit  low = %s : %s", token[0], token[1], token[2], line));
                    fftd=token[2];
                    token[2] = "";
                }
                if (token[0].equals("  9")) {
                    System.out.println(String.format("dtype = %3s  device = %s hit high = %s : %s", token[0], token[1], token[2], line));
                    fftd=token[2];
                    token[2] = "";
                }

                try {
                    Connection conn = DriverManager.getConnection("jdbc:mariadb://10.1.1.172/", connConfig);

                    // Prepare INSERT Statement to Add IMU data
                    try ( PreparedStatement prep = conn.prepareStatement(
                            "INSERT INTO hits.imu (dtype, device, cpu, str,fft) VALUES (?, ?, ?, ?, ?)",
                            Statement.RETURN_GENERATED_KEYS)) {
                        // Add Contact
                        prep.setString(1, token[0]);
                        prep.setString(2, token[1]);
                        prep.setString(3, token[2]);
                        prep.setString(4, line);
                        prep.setString(5, fftd);
                        prep.addBatch();

                        // Execute Prepared Statements in Batch
                        System.out.println("Batch Counts");
                        int[] updateCounts = prep.executeBatch();
                        for (int count : updateCounts) {
                            // Print Counts
                            System.out.println(count);
                        }
                    }

                } catch (SQLException ex) {
                    // handle any errors
                    System.out.println("SQLException: " + ex.getMessage());
                    System.out.println("SQLState: " + ex.getSQLState());
                    System.out.println("VendorError: " + ex.getErrorCode());
                }

            } catch (Exception e) {
            }
        }
        s.close();
        System.err.println("Lost communication with the serial port. Exiting.");
        System.exit(1);
    }

}
