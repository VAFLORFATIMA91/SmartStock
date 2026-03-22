package config;

import java.sql.Connection;
import java.sql.DriverManager; 
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import net.proteanit.sql.DbUtils;

public class config {

    // ================= CONNECT TO SQLITE =================
    public static Connection connectDB() {
        Connection con = null;
        try {
            Class.forName("org.sqlite.JDBC"); // Load SQLite JDBC driver
            con = DriverManager.getConnection("jdbc:sqlite:store1.db"); // Connect to DB

            // 🔥 Enable WAL mode (reduces locking)
            try (PreparedStatement ps = con.prepareStatement("PRAGMA journal_mode=WAL")) {
                ps.execute();
            }

            // 🔥 Set busy timeout (wait 5 seconds before throwing lock error)
            try (PreparedStatement ps = con.prepareStatement("PRAGMA busy_timeout = 5000")) {
                ps.execute();
            }

            System.out.println("Connection Successful");

        } catch (Exception e) {
            System.out.println("Connection Failed: " + e);
        }
        return con;
    }

    // ================= ADD RECORD =================
    public void addRecord(String sql, Object... values) {

        try (Connection conn = connectDB();
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
                    pstmt.setDate(i + 1,
                            new java.sql.Date(((java.util.Date) values[i]).getTime()));

                } else if (values[i] instanceof java.sql.Date) {
                    pstmt.setDate(i + 1, (java.sql.Date) values[i]);

                } else if (values[i] instanceof java.sql.Timestamp) {
                    pstmt.setTimestamp(i + 1, (java.sql.Timestamp) values[i]);

                } else {
                    pstmt.setString(i + 1, values[i].toString());
                }
            }

            pstmt.executeUpdate();
            System.out.println("Record added successfully!");

        } catch (SQLException e) {
            System.out.println("Error adding record: " + e.getMessage());
        }
    }

    // ================= DISPLAY DATA =================
    public void displayData(String sql, javax.swing.JTable table) {

        try (Connection conn = connectDB();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {

            table.setModel(DbUtils.resultSetToTableModel(rs));

        } catch (SQLException e) {
            System.out.println("Error displaying data: " + e.getMessage());
        }
        
    }
        
        // ================= UPDATE / DELETE =================
    public void update(String sql, Object... values) {

    try (Connection conn = connectDB();
         PreparedStatement pstmt = conn.prepareStatement(sql)) {

        for (int i = 0; i < values.length; i++) {

            if (values[i] instanceof Integer) {
                pstmt.setInt(i + 1, (Integer) values[i]);

            } else if (values[i] instanceof Double) {
                pstmt.setDouble(i + 1, (Double) values[i]);

            } else {
                pstmt.setString(i + 1, values[i].toString());
            }
        }

        pstmt.executeUpdate();
        System.out.println("Update/Delete successful!");

        } catch (SQLException e) {
        System.out.println("Error updating record: " + e.getMessage());
        }
    }
    
    // ================= SELECT QUERY =================
     public ResultSet select(String sql) {

    ResultSet rs = null;

    try {
        Connection conn = connectDB();
        PreparedStatement pstmt = conn.prepareStatement(sql);
        rs = pstmt.executeQuery();

        } catch (SQLException e) {
        System.out.println("Error selecting data: " + e.getMessage());
        }

        return rs;
    }
}
    
