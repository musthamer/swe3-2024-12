package hbv.service;

import hbv.utils.DbUtils;
import java.sql.*;
import java.util.*;
import org.json.*;

public class AdminService {
  public JSONArray getAllCenters() throws Exception {
    JSONArray centers = new JSONArray();

    try (Connection conn = DbUtils.getConnection()) {
      PreparedStatement ps =
          conn.prepareStatement("SELECT id, name, address FROM vaccination_center");
      ResultSet rs = ps.executeQuery();

      while (rs.next()) {
        JSONObject center = new JSONObject();
        center.put("id", rs.getInt("id"));
        center.put("name", rs.getString("name"));
        center.put("address", rs.getString("address"));
        centers.put(center);
      }
    }

    return centers;
  }

  /** Gibt alle Impfstoffe für ein Zentrum zurück */
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

  /** Erstellt ein neues Impfzentrum */
  public boolean createCenter(String name, String address) throws Exception {
    try (Connection conn = DbUtils.getConnection()) {
      PreparedStatement ps =
          conn.prepareStatement("INSERT INTO vaccination_center (name, address) VALUES (?, ?)");
      ps.setString(1, name);
      ps.setString(2, address);
      return ps.executeUpdate() > 0;
    }
  }

  /** Aktualisiert den Impfbestand */
  public boolean updateInventory(int centerId, int vaccineId, int dosesToAdd) throws Exception {
    try (Connection conn = DbUtils.getConnection()) {
      // Zuerst prüfen, ob ein Eintrag bereits existiert
      PreparedStatement checkPs =
          conn.prepareStatement(
              "SELECT available_doses FROM vaccination_center_vaccine WHERE center_id = ? AND"
                  + " vaccine_id = ?");
      checkPs.setInt(1, centerId);
      checkPs.setInt(2, vaccineId);
      ResultSet rs = checkPs.executeQuery();

      if (rs.next()) {
        // Vorhandenen Bestand auslesen
        int currentDoses = rs.getInt("available_doses");
        int newTotalDoses = currentDoses + dosesToAdd;

        // Eintrag aktualisieren mit der Summe
        PreparedStatement updatePs =
            conn.prepareStatement(
                "UPDATE vaccination_center_vaccine SET available_doses = ? WHERE center_id = ? AND"
                    + " vaccine_id = ?");
        updatePs.setInt(1, newTotalDoses);
        updatePs.setInt(2, centerId);
        updatePs.setInt(3, vaccineId);
        return updatePs.executeUpdate() > 0;
      } else {
        // Neuen Eintrag erstellen
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
