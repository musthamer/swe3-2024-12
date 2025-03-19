package hbv.service;
import java.sql.*;
public class CenterService {
    public void createCenter(String centerName, String centerEmail, String centerPassword) throws SQLException {
        try (Connection conn = Database.getConnection()) {
            PreparedStatement psCenter = conn.prepareStatement("INSERT INTO vaccine_center (center_name) VALUES (?)");
            psCenter.setString(1, centerName);
            psCenter.executeUpdate();
            psCenter.close();
            String centerHash = sha256(centerPassword);
            PreparedStatement psAccount = conn.prepareStatement("INSERT INTO user_account (email, vorname, nachname, password_hash, role, assigned_center, created_at) VALUES (?,?,?,?,?, ?,CURRENT_TIMESTAMP)");
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
    private String sha256(String raw) throws SQLException {
        try {
            java.security.MessageDigest md = java.security.MessageDigest.getInstance("SHA-256");
            byte[] bytes = md.digest(raw.getBytes());
            StringBuilder sb = new StringBuilder();
            for (byte b : bytes) { sb.append(String.format("%02x", b)); }
            return sb.toString();
        } catch (java.security.NoSuchAlgorithmException e) {
            throw new SQLException("SHA-256 Algorithm not found", e);
        }
    }
}
