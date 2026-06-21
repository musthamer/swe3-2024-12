package hbv.service;

import hbv.utils.DbUtils;
import java.sql.*;
import org.json.*;

public class AdminService {

  private final VaccinationCenterService centerService = new VaccinationCenterService();

  public JSONArray getAllCenters() throws Exception {
    JSONArray centers = new JSONArray();

    for (var center : centerService.getAllCenters()) {
      JSONObject entry = new JSONObject();
      entry.put("id", center.get("id"));
      entry.put("name", center.get("name"));
      entry.put("address", center.get("address"));
      centers.put(entry);
    }

    return centers;
  }

  public JSONArray getVaccinesForCenter(int centerId) throws Exception {
    JSONArray vaccines = new JSONArray();

    try (Connection conn = DbUtils.getConnection()) {
      PreparedStatement ps =
          conn.prepareStatement(
              "SELECT v.id, v.name, COALESCE(cv.available_doses, 0) as doses FROM vaccine v LEFT"
                  + " JOIN vaccination_center_vaccine cv ON v.id = cv.vaccine_id AND cv.center_id ="
                  + " ?");
      ps.setInt(1, centerId);
      ResultSet rs = ps.executeQuery();

      while (rs.next()) {
        JSONObject vaccine = new JSONObject();
        vaccine.put("id", rs.getInt("id"));
        vaccine.put("name", rs.getString("name"));
        vaccine.put("doses", rs.getInt("doses"));
        vaccines.put(vaccine);
      }
    }

    return vaccines;
  }

  public boolean createCenter(String name, String address) throws Exception {
    return centerService.createCenter(name, address) > 0;
  }

  public boolean updateInventory(int centerId, int vaccineId, int dosesToAdd) throws Exception {
    try (Connection conn = DbUtils.getConnection()) {
      PreparedStatement checkPs =
          conn.prepareStatement(
              "SELECT available_doses FROM vaccination_center_vaccine WHERE center_id = ? AND"
                  + " vaccine_id = ?");
      checkPs.setInt(1, centerId);
      checkPs.setInt(2, vaccineId);
      ResultSet rs = checkPs.executeQuery();

      if (rs.next()) {
        int currentDoses = rs.getInt("available_doses");
        int newTotalDoses = currentDoses + dosesToAdd;

        PreparedStatement updatePs =
            conn.prepareStatement(
                "UPDATE vaccination_center_vaccine SET available_doses = ? WHERE center_id = ? AND"
                    + " vaccine_id = ?");
        updatePs.setInt(1, newTotalDoses);
        updatePs.setInt(2, centerId);
        updatePs.setInt(3, vaccineId);
        return updatePs.executeUpdate() > 0;
      } else {
        PreparedStatement insertPs =
            conn.prepareStatement(
                "INSERT INTO vaccination_center_vaccine (center_id, vaccine_id, available_doses)"
                    + " VALUES (?, ?, ?)");
        insertPs.setInt(1, centerId);
        insertPs.setInt(2, vaccineId);
        insertPs.setInt(3, dosesToAdd);
        return insertPs.executeUpdate() > 0;
      }
    }
  }
}
