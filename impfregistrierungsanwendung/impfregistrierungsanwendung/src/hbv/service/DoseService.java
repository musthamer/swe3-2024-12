package hbv.service;
import java.sql.*;
public class DoseService {
    public void addDoses(String vaccine, int quantity, String centerName) throws SQLException {
        try (Connection conn = Database.getConnection()) {
            int centerId = -1;
            PreparedStatement psCenter = conn.prepareStatement("SELECT center_id FROM vaccine_center WHERE center_name = ?");
            psCenter.setString(1, centerName);
            try (ResultSet rs = psCenter.executeQuery()) {
                if (rs.next()) { centerId = rs.getInt("center_id"); }
            }
            psCenter.close();
            if (centerId == -1) { throw new SQLException("Impfzentrum nicht gefunden."); }
            PreparedStatement psCheck = conn.prepareStatement("SELECT stock FROM vaccine_inventory WHERE center_id = ? AND vaccine = ?");
            psCheck.setInt(1, centerId);
            psCheck.setString(2, vaccine);
            boolean exists = false;
            int currentStock = 0;
            try (ResultSet rs = psCheck.executeQuery()) {
                if (rs.next()) { exists = true; currentStock = rs.getInt("stock"); }
            }
            psCheck.close();
            if (exists) {
                int newStock = currentStock + quantity;
                PreparedStatement psUpdate = conn.prepareStatement("UPDATE vaccine_inventory SET stock = ? WHERE center_id = ? AND vaccine = ?");
                psUpdate.setInt(1, newStock);
                psUpdate.setInt(2, centerId);
                psUpdate.setString(3, vaccine);
                psUpdate.executeUpdate();
                psUpdate.close();
            } else {
                PreparedStatement psInsert = conn.prepareStatement("INSERT INTO vaccine_inventory (center_id, vaccine, stock) VALUES (?,?,?)");
                psInsert.setInt(1, centerId);
                psInsert.setString(2, vaccine);
                psInsert.setInt(3, quantity);
                psInsert.executeUpdate();
                psInsert.close();
            }
        }
    }
}
