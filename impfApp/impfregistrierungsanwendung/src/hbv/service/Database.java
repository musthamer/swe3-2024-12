package hbv.service;

import java.sql.*;

public class Database {
    private static final String URL = "jdbc:mariadb://localhost:3306/" + System.getProperty("dbName", "impfregistrierung");
    private static final String USER = System.getProperty("dbUser", "impfadmin");
    private static final String PASSWORD = System.getProperty("dbPass", "impfpass");

    static {
        try {
            Class.forName("org.mariadb.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            throw new RuntimeException("Kein JDBC-Treiber gefunden", e);
        }
    }

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }

    //  NEUE METHODE: E-Mail-Adresse aus der Datenbank abrufen
    public static String getEmailByUserId(int userId) throws SQLException {
        String email = null;
        String sql = "SELECT email FROM user_account WHERE user_id = ?";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                email = rs.getString("email");
            }
        }
        return email;
    }
}

