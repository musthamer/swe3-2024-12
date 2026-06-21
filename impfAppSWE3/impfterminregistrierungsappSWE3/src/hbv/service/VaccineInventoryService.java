package hbv.service;

import hbv.utils.DbUtils;
import java.sql.*;
import java.util.*;

public class VaccineInventoryService {

  public Map<String, Object> getVaccineInventory(int centerId) throws Exception {
    Map<String, Object> inventory = new HashMap<>();

    try (Connection connection = DbUtils.getConnection()) {
      PreparedStatement ps =
          connection.prepareStatement(
              "SELECT v.id, v.name, v.manufacturer, cv.available_doses "
                  + "FROM vaccine v "
                  + "JOIN vaccination_center_vaccine cv ON v.id = cv.vaccine_id "
                  + "WHERE cv.center_id = ?");
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

  public Map<String, Object> updateVaccineInventory(int centerId, int vaccineId, int addDoses)
      throws Exception {
    Map<String, Object> result = new HashMap<>();

    try (Connection connection = DbUtils.getConnection()) {
      PreparedStatement ps =
          connection.prepareStatement(
              "UPDATE vaccination_center_vaccine "
                  + "SET available_doses = available_doses + ? "
                  + "WHERE center_id = ? AND vaccine_id = ?");
      ps.setInt(1, addDoses);
      ps.setInt(2, centerId);
      ps.setInt(3, vaccineId);
      int updated = ps.executeUpdate();

      result.put("success", updated > 0);
      result.put(
          "message",
          updated > 0 ? "Impfstoffbestand erfolgreich aktualisiert" : "Impfstoff nicht gefunden");
    }
    return result;
  }
}
