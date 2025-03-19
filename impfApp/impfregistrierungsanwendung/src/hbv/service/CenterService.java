package hbv.service;

import java.sql.*;
import hbv.util.PasswortService;
import hbv.util.PasswortHelper;

public class CenterService {
    // Erstellt neues Impfzentrum inkl. Center-User-Konto.
    public void createCenter(String centerName, String centerEmail, String centerPassword) throws SQLException {
        try (Connection conn = Database.getConnection()) {
            PreparedStatement psCenter = conn.prepareStatement("INSERT INTO vaccine_center (center_name) VALUES (?)");
            psCenter.setString(1, centerName);
            psCenter.executeUpdate();
            psCenter.close();

            // Verwende den PasswortHelper zum Hashen des Passworts
            PasswortService passwortService = new PasswortHelper();
            String centerHash = passwortService.hashePasswortMitSalt(centerPassword);

            PreparedStatement psAccount = conn.prepareStatement(
                "INSERT INTO user_account (email, vorname, nachname, password_hash, role, assigned_center, created_at) VALUES (?,?,?,?,?, ?,CURRENT_TIMESTAMP)"
            );
            psAccount.setString(1, centerEmail);
            psAccount.setString(2, "Center");
            psAccount.setString(3, "User");
            psAccount.setString(4, centerHash);
            psAccount.setString(5, "center");
            psAccount.setString(6, centerName);
            psAccount.executeUpdate();
            psAccount.close();
        }
    }
}
