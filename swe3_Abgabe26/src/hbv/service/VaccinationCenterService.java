package hbv.service;

import hbv.utils.DbUtils;
import java.sql.*;
import java.util.*;

public class VaccinationCenterService {

  public List<Map<String, Object>> getAllCenters() throws Exception {
    List<Map<String, Object>> centers = new ArrayList<>();

    try (Connection connection = DbUtils.getConnection()) {
      PreparedStatement ps =
          connection.prepareStatement("SELECT id, name, address FROM vaccination_center");
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

  /** Lädt verfügbare Impfstoffe für ein bestimmtes Impfzentrum */
  public List<Map<String, Object>> getVaccinesForCenter(int centerId) throws Exception {
    List<Map<String, Object>> vaccines = new ArrayList<>();

    try (Connection connection = DbUtils.getConnection()) {
      PreparedStatement ps =
          connection.prepareStatement(
              "SELECT v.id, v.name, v.manufacturer, cv.available_doses "
                  + "FROM vaccine v "
                  + "JOIN vaccination_center_vaccine cv ON v.id = cv.vaccine_id "
                  + "WHERE cv.center_id = ? AND cv.available_doses > 0");
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

  /** Erstellt ein neues Impfzentrum */
  public int createCenter(String name, String address) throws Exception {
    int centerId = -1;

    try (Connection connection = DbUtils.getConnection()) {
      PreparedStatement ps =
          connection.prepareStatement(
              "INSERT INTO vaccination_center (name, address) VALUES (?, ?)",
              Statement.RETURN_GENERATED_KEYS);
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

  /**
   * Ruft statistische Daten für alle Impfzentren ab
   *
   * @return Eine Liste mit statistischen Daten für jedes Impfzentrum
   */
  public List<Map<String, Object>> getAllCentersStatistics() throws Exception {
    List<Map<String, Object>> allStats = new ArrayList<>();

    try (Connection connection = DbUtils.getConnection()) {
      // Basisinformationen zu Impfzentren abrufen
      PreparedStatement ps =
          connection.prepareStatement(
              "SELECT id, name, address FROM vaccination_center ORDER BY name");
      ResultSet rs = ps.executeQuery();

      while (rs.next()) {
        Map<String, Object> centerStats = new HashMap<>();
        int centerId = rs.getInt("id");

        centerStats.put("id", centerId);
        centerStats.put("centerName", rs.getString("name"));
        centerStats.put("address", rs.getString("address"));

        // Zählung der Buchungen pro Impfzentrum
        PreparedStatement bookingStats =
            connection.prepareStatement(
                "SELECT COUNT(*) AS total_bookings FROM booking b "
                    + "JOIN timeslot t ON b.timeslot_id = t.id "
                    + "WHERE t.center_id = ? AND b.status = 'CONFIRMED'");
        bookingStats.setInt(1, centerId);
        ResultSet bookingRs = bookingStats.executeQuery();

        int totalBookings = 0;
        if (bookingRs.next()) {
          totalBookings = bookingRs.getInt("total_bookings");
        }
        centerStats.put("totalBookings", totalBookings);

        // Verfügbare Slots berechnen
        PreparedStatement availableSlotsPs =
            connection.prepareStatement(
                "SELECT COUNT(*) AS available_slots FROM timeslot t WHERE t.center_id = ? AND"
                    + " t.capacity > (SELECT COUNT(*) FROM booking b WHERE b.timeslot_id = t.id AND"
                    + " b.status = 'CONFIRMED') AND t.start_time > NOW()");
        availableSlotsPs.setInt(1, centerId);
        ResultSet availableSlotsRs = availableSlotsPs.executeQuery();

        int availableSlots = 0;
        if (availableSlotsRs.next()) {
          availableSlots = availableSlotsRs.getInt("available_slots");
        }
        centerStats.put("availableSlots", availableSlots);

        // Belegungsrate berechnen (sofern verfügbare Zeitfenster existieren)
        PreparedStatement totalSlotsPs =
            connection.prepareStatement(
                "SELECT SUM(capacity) AS total_capacity FROM timeslot WHERE center_id = ? AND"
                    + " start_time > NOW()");
        totalSlotsPs.setInt(1, centerId);
        ResultSet totalSlotsRs = totalSlotsPs.executeQuery();

        int totalCapacity = 0;
        if (totalSlotsRs.next()) {
          totalCapacity = totalSlotsRs.getInt("total_capacity");
        }

        int occupancyRate = 0;
        if (totalCapacity > 0) {
          occupancyRate = (int) (((double) totalBookings / totalCapacity) * 100);
        }
        centerStats.put("occupancyRate", occupancyRate);

        // Verfügbare Impfstoffdosen abrufen
        PreparedStatement vaccineStats =
            connection.prepareStatement(
                "SELECT v.name, vcv.available_doses "
                    + "FROM vaccination_center_vaccine vcv "
                    + "JOIN vaccine v ON vcv.vaccine_id = v.id "
                    + "WHERE vcv.center_id = ?");
        vaccineStats.setInt(1, centerId);
        ResultSet vaccineRs = vaccineStats.executeQuery();

        Map<String, Integer> vaccines = new HashMap<>();
        while (vaccineRs.next()) {
          vaccines.put(vaccineRs.getString("name"), vaccineRs.getInt("available_doses"));
        }
        centerStats.put("vaccines", vaccines);

        allStats.add(centerStats);
      }
    }

    return allStats;
  }

  /**
   * Ruft statistische Daten für ein einzelnes Impfzentrum ab
   *
   * @param centerId ID des Impfzentrums
   * @return Statistische Daten für das angegebene Impfzentrum
   */
  public Map<String, Object> getCenterStatistics(int centerId) throws Exception {
    Map<String, Object> centerStats = new HashMap<>();

    try (Connection connection = DbUtils.getConnection()) {
      // Basisinformationen zum Impfzentrum abrufen
      PreparedStatement ps =
          connection.prepareStatement(
              "SELECT id, name, address FROM vaccination_center WHERE id = ?");
      ps.setInt(1, centerId);
      ResultSet rs = ps.executeQuery();

      if (rs.next()) {
        centerStats.put("id", rs.getInt("id"));
        centerStats.put("centerName", rs.getString("name"));
        centerStats.put("address", rs.getString("address"));

        // Zählung der Buchungen
        PreparedStatement bookingStats =
            connection.prepareStatement(
                "SELECT COUNT(*) AS total_bookings FROM booking b "
                    + "JOIN timeslot t ON b.timeslot_id = t.id "
                    + "WHERE t.center_id = ? AND b.status = 'CONFIRMED'");
        bookingStats.setInt(1, centerId);
        ResultSet bookingRs = bookingStats.executeQuery();

        int totalBookings = 0;
        if (bookingRs.next()) {
          totalBookings = bookingRs.getInt("total_bookings");
        }
        centerStats.put("totalBookings", totalBookings);

        // Verfügbare Slots berechnen
        PreparedStatement availableSlotsPs =
            connection.prepareStatement(
                "SELECT COUNT(*) AS available_slots FROM timeslot t WHERE t.center_id = ? AND"
                    + " t.capacity > (SELECT COUNT(*) FROM booking b WHERE b.timeslot_id = t.id AND"
                    + " b.status = 'CONFIRMED') AND t.start_time > NOW()");
        availableSlotsPs.setInt(1, centerId);
        ResultSet availableSlotsRs = availableSlotsPs.executeQuery();

        int availableSlots = 0;
        if (availableSlotsRs.next()) {
          availableSlots = availableSlotsRs.getInt("available_slots");
        }
        centerStats.put("availableSlots", availableSlots);

        // Belegungsrate berechnen
        PreparedStatement totalSlotsPs =
            connection.prepareStatement(
                "SELECT SUM(capacity) AS total_capacity FROM timeslot WHERE center_id = ? AND"
                    + " start_time > NOW()");
        totalSlotsPs.setInt(1, centerId);
        ResultSet totalSlotsRs = totalSlotsPs.executeQuery();

        int totalCapacity = 0;
        if (totalSlotsRs.next()) {
          totalCapacity = totalSlotsRs.getInt("total_capacity");
        }

        int occupancyRate = 0;
        if (totalCapacity > 0) {
          occupancyRate = (int) (((double) totalBookings / totalCapacity) * 100);
        }
        centerStats.put("occupancyRate", occupancyRate);

        // Aktuelle Buchungen abrufen
        PreparedStatement currentBookingsPs =
            connection.prepareStatement(
                "SELECT b.id, p.first_name, p.last_name, t.start_time, v.name AS vaccine_name "
                    + "FROM booking b "
                    + "JOIN person p ON b.person_id = p.id "
                    + "JOIN timeslot t ON b.timeslot_id = t.id "
                    + "JOIN vaccine v ON b.vaccine_id = v.id "
                    + "WHERE t.center_id = ? AND b.status = 'CONFIRMED' AND t.start_time > NOW() "
                    + "ORDER BY t.start_time LIMIT 10");
        currentBookingsPs.setInt(1, centerId);
        ResultSet currentBookingsRs = currentBookingsPs.executeQuery();

        List<Map<String, Object>> currentBookings = new ArrayList<>();
        while (currentBookingsRs.next()) {
          Map<String, Object> booking = new HashMap<>();
          booking.put("id", currentBookingsRs.getInt("id"));
          booking.put(
              "patientName",
              currentBookingsRs.getString("first_name")
                  + " "
                  + currentBookingsRs.getString("last_name"));
          booking.put("appointmentTime", currentBookingsRs.getTimestamp("start_time").toString());
          booking.put("vaccineName", currentBookingsRs.getString("vaccine_name"));
          currentBookings.add(booking);
        }
        centerStats.put("currentBookings", currentBookings);

        // Impfstoffbestand abrufen
        PreparedStatement vaccineStats =
            connection.prepareStatement(
                "SELECT v.id, v.name, vcv.available_doses "
                    + "FROM vaccination_center_vaccine vcv "
                    + "JOIN vaccine v ON vcv.vaccine_id = v.id "
                    + "WHERE vcv.center_id = ?");
        vaccineStats.setInt(1, centerId);
        ResultSet vaccineRs = vaccineStats.executeQuery();

        List<Map<String, Object>> vaccines = new ArrayList<>();
        while (vaccineRs.next()) {
          Map<String, Object> vaccine = new HashMap<>();
          vaccine.put("id", vaccineRs.getInt("id"));
          vaccine.put("name", vaccineRs.getString("name"));
          vaccine.put("availableDoses", vaccineRs.getInt("available_doses"));
          vaccines.add(vaccine);
        }
        centerStats.put("vaccines", vaccines);
      }
    }

    return centerStats;
  }
}
