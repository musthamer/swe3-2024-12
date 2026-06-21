package hbv.service;

import hbv.utils.DbUtils;
import java.sql.*;
import java.util.*;

public class TimeslotService {

  public List<Map<String, Object>> getAvailableTimeslotsForCenter(int centerId) throws Exception {
    List<Map<String, Object>> timeslots = new ArrayList<>();

    System.out.println("Suche Termine für Zentrum " + centerId);

    try (Connection connection = DbUtils.getConnection()) {
      PreparedStatement ps =
          connection.prepareStatement(
              "SELECT t.id, t.start_time, t.end_time, t.capacity, "
                  + "COALESCE(COUNT(b.id), 0) AS booked_count "
                  + "FROM timeslot t "
                  + "LEFT JOIN booking b ON t.id = b.timeslot_id AND b.status = 'CONFIRMED' "
                  + "WHERE t.center_id = ? AND t.start_time > NOW() "
                  + "GROUP BY t.id, t.start_time, t.end_time, t.capacity "
                  + "HAVING COALESCE(COUNT(b.id), 0) < t.capacity "
                  + "ORDER BY t.start_time");
      ps.setInt(1, centerId);

      System.out.println("SQL ausführen: " + ps.toString());

      try (ResultSet rs = ps.executeQuery()) {
        while (rs.next()) {
          Map<String, Object> timeslot = new HashMap<>();
          timeslot.put("id", rs.getInt("id"));
          timeslot.put("startTime", rs.getTimestamp("start_time").getTime());
          timeslot.put("endTime", rs.getTimestamp("end_time").getTime());
          timeslot.put("capacity", rs.getInt("capacity"));
          timeslot.put("bookedCount", rs.getInt("booked_count"));

          int availableSlots = rs.getInt("capacity") - rs.getInt("booked_count");
          timeslot.put("freiePlaetze", availableSlots);

          timeslots.add(timeslot);
        }
      }
    }

    System.out.println("Gefundene Termine: " + timeslots.size());
    return timeslots;
  }
}
