package util;

import javax.swing.JOptionPane;
import javax.swing.JFrame;

public class Session {
    public static int id;
    public static String first_name;
    public static String last_name;
    public static String email;
    public static String status;
    public static String role;

    public static void clear() {
        id = 0;
        first_name = null;
        last_name = null;  // add this line
        email = null;
        status = null;
        role = null;
    }

    // ================= CHECK LOGIN =================
    public static boolean isLoggedIn() {
        return id != 0;
    }

    // ================= REQUIRE LOGIN =================
    public static void requireLogin(JFrame currentForm) {
        if (!isLoggedIn()) {
            JOptionPane.showMessageDialog(currentForm, "You must log in first!", "Access Denied", JOptionPane.WARNING_MESSAGE);
            new main.loginPage().setVisible(true);
            currentForm.dispose();
        }
    }

    // ================= GETTER / SETTER =================
    public static int getId() {
        return id;
    }

    public static void setId(int userId) {
        id = userId;
    }

    // ================= FULL NAME HELPER =================
    public static String getFullName() {
        if (first_name != null && last_name != null) {
            return first_name + " " + last_name;
        } else if (first_name != null) {
            return first_name;
        } else {
            return "Unknown";
        }
    }
}