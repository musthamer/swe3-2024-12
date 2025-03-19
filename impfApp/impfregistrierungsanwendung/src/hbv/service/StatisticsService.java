package hbv.service;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class StatisticsService {
    public List<String[]> getPopularVaccinesOverall() throws SQLException {
        String sql = "SELECT a.vaccine, COUNT(b.booking_id) AS cnt " +
                     "FROM appointment a " +
                     "JOIN booking b ON a.appointment_id = b.appointment_id " +
                     "GROUP BY a.vaccine ORDER BY cnt DESC";
        List<String[]> result = new ArrayList<>();
        try (Connection conn = Database.getConnection(); 
             PreparedStatement ps = conn.prepareStatement(sql); 
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                String vaccine = rs.getString("vaccine");
                String count = String.valueOf(rs.getInt("cnt"));
                result.add(new String[]{vaccine, count});
            }
        }
        return result;
    }

    public List<String[]> getBusiestCenters() throws SQLException {
        String sql = "SELECT a.location AS centerName, COUNT(b.booking_id) AS cnt " +
                     "FROM appointment a " +
                     "JOIN booking b ON a.appointment_id = b.appointment_id " +
                     "GROUP BY a.location ORDER BY cnt DESC";
        List<String[]> result = new ArrayList<>();
        try (Connection conn = Database.getConnection(); 
             PreparedStatement ps = conn.prepareStatement(sql); 
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                String centerName = rs.getString("centerName");
                String count = String.valueOf(rs.getInt("cnt"));
                result.add(new String[]{centerName, count});
            }
        }
        return result;
    }

    public List<String[]> getPopularVaccinesForCenter(String centerName) throws SQLException {
        String sql = "SELECT a.vaccine, COUNT(b.booking_id) AS cnt " +
                     "FROM appointment a " +
                     "JOIN booking b ON a.appointment_id = b.appointment_id " +
                     "WHERE a.location=? GROUP BY a.vaccine ORDER BY cnt DESC";
        List<String[]> result = new ArrayList<>();
        try (Connection conn = Database.getConnection(); 
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, centerName);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    String vaccine = rs.getString("vaccine");
                    String count = String.valueOf(rs.getInt("cnt"));
                    result.add(new String[]{vaccine, count});
                }
            }
        }
        return result;
    }
}
