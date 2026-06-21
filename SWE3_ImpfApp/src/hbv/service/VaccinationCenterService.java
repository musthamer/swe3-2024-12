package hbv.service;

import java.sql.*;
import java.util.*;
import hbv.utils.DbUtils;

public class VaccinationCenterService {
    
    public List<Map<String, Object>> getAllCenters() throws Exception {
        List<Map<String, Object>> centers = new ArrayList<>();
        
        try (Connection connection = DbUtils.getConnection()) {
            PreparedStatement ps = connection.prepareStatement(
                "SELECT id, name, address FROM vaccination_center"
            );
            ResultSet rs = ps.executeQuery();
            
            while (rs.next()) {
                Map<String, Object> center = new HashMap<>();
                center.put("id", rs.getInt("id"));
                center.put("name", rs.getString("name"));
                center.put("address", rs.getString("address"));
                centers.add(center);
            }
        }
        
        return centers;
    }
    
    public List<Map<String, Object>> getVaccinesForCenter(int centerId) throws Exception {
        List<Map<String, Object>> vaccines = new ArrayList<>();
        
        try (Connection connection = DbUtils.getConnection()) {
            PreparedStatement ps = connection.prepareStatement(
                "SELECT v.id, v.name, v.manufacturer, cv.available_doses " +
                "FROM vaccine v " +
                "JOIN vaccination_center_vaccine cv ON v.id = cv.vaccine_id " +
                "WHERE cv.center_id = ? AND cv.available_doses > 0"
            );
            ps.setInt(1, centerId);
            ResultSet rs = ps.executeQuery();
            
            while (rs.next()) {
                Map<String, Object> vaccine = new HashMap<>();
                vaccine.put("id", rs.getInt("id"));
                vaccine.put("name", rs.getString("name"));
                vaccine.put("manufacturer", rs.getString("manufacturer"));
                vaccine.put("availableDoses", rs.getInt("available_doses"));
                vaccines.add(vaccine);
            }
        }
        
        return vaccines;
    }
    
    public int createCenter(String name, String address) throws Exception {
        int centerId = -1;
        
        try (Connection connection = DbUtils.getConnection()) {
            PreparedStatement ps = connection.prepareStatement(
                "INSERT INTO vaccination_center (name, address) VALUES (?, ?)",
                Statement.RETURN_GENERATED_KEYS
            );
            ps.setString(1, name);
            ps.setString(2, address);
            ps.executeUpdate();
            
            ResultSet keys = ps.getGeneratedKeys();
            if (keys.next()) {
                centerId = keys.getInt(1);
            }
        }
        
        return centerId;
    }

    public Map<String, Object> getVaccineInventory(int centerId) throws Exception {
        Map<String, Object> inventory = new HashMap<>();

        try (Connection connection = DbUtils.getConnection()) {
            PreparedStatement ps = connection.prepareStatement(
                "SELECT v.id, v.name, v.manufacturer, cv.available_doses " +
                "FROM vaccine v " +
                "JOIN vaccination_center_vaccine cv ON v.id = cv.vaccine_id " +
                "WHERE cv.center_id = ?"
            );
            ps.setInt(1, centerId);
            ResultSet rs = ps.executeQuery();

            List<Map<String, Object>> vaccines = new ArrayList<>();
            while (rs.next()) {
                Map<String, Object> vaccine = new HashMap<>();
                vaccine.put("id", rs.getInt("id"));
                vaccine.put("name", rs.getString("name"));
                vaccine.put("manufacturer", rs.getString("manufacturer"));
                vaccine.put("availableDoses", rs.getInt("available_doses"));
                vaccines.add(vaccine);
            }
            inventory.put("vaccines", vaccines);
        }
        return inventory;
    }
} 