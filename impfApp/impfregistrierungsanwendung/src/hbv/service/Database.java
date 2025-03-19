package hbv.service;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

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
}
