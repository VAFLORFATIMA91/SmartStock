package config;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import net.proteanit.sql.DbUtils;

public class DBConnection {

    // Connection Method to SQLITE
    public static Connection connectDB() {
        Connection con = null;
        try {
            Class.forName("org.sqlite.JDBC");
            con = DriverManager.getConnection("jdbc:sqlite:store1.db");
            System.out.println("Connection Successful");
        } catch (Exception e) {
            System.out.println("Connection Failed: " + e.getMessage());
        }
        return con;
    }

    // Get Connection
    public static Connection getConnection() {
        return connectDB(); // Directly return the connection
    }

    // Add or Update Record
    public static void execute(String sql, Object... values) {
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            for (int i = 0; i < values.length; i++) {
                if (values[i] instanceof Integer) {
                    pstmt.setInt(i + 1, (Integer) values[i]);
                } else if (values[i] instanceof Double) {
                    pstmt.setDouble(i + 1, (Double) values[i]);
                } else if (values[i] instanceof Float) {
                    pstmt.setFloat(i + 1, (Float) values[i]);
                } else if (values[i] instanceof Long) {
                    pstmt.setLong(i + 1, (Long) values[i]);
                } else if (values[i] instanceof Boolean) {
                    pstmt.setBoolean(i + 1, (Boolean) values[i]);
                } else if (values[i] instanceof java.util.Date) {
                    pstmt.setDate(i + 1, new java.sql.Date(((java.util.Date) values[i]).getTime()));
                } else {
                    pstmt.setString(i + 1, values[i].toString());
                }
            }

            pstmt.executeUpdate();
            System.out.println("Record executed successfully!");

        } catch (SQLException e) {
            System.out.println("Error executing record: " + e.getMessage());
        }
    }

    // Display data to JTable
    public static void displayData(String sql, javax.swing.JTable table) {
        try (Connection conn = getConnection();
             PreparedStatement pst = conn.prepareStatement(sql);
             ResultSet rs = pst.executeQuery()) {

            table.setModel(DbUtils.resultSetToTableModel(rs));

        } catch (SQLException e) {
            System.out.println("Error displaying data: " + e.getMessage());
        }
    }
}