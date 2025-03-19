package hbv.service;

import hbv.model.Appointment;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class AppointmentService {
  public List<Appointment> getAllAppointments() throws SQLException {
    return getFilteredAppointments(null, null);
  }

  public List<Appointment> getFilteredAppointments(String vaccine, String center)
      throws SQLException {
    List<Appointment> appointments = new ArrayList<>();
    String sql = "SELECT * FROM appointment";
    boolean hasWhere = false;
    if (vaccine != null && !vaccine.trim().isEmpty()) {
      sql += " WHERE vaccine = ?";
      hasWhere = true;
    }
    if (center != null && !center.trim().isEmpty()) {
      sql += hasWhere ? " AND location = ?" : " WHERE location = ?";
    }
    try (Connection conn = Database.getConnection();
        PreparedStatement ps = conn.prepareStatement(sql)) {
      int idx = 1;
      if (vaccine != null && !vaccine.trim().isEmpty()) {
        ps.setString(idx++, vaccine);
      }
      if (center != null && !center.trim().isEmpty()) {
        ps.setString(idx++, center);
      }
      try (ResultSet rs = ps.executeQuery()) {
        while (rs.next()) {
          Appointment appointment =
              new Appointment(
                  rs.getInt("appointment_id"),
                  rs.getDate("date_slot"),
                  rs.getString("time_slot"),
                  rs.getString("vaccine"),
                  rs.getInt("capacity"),
                  rs.getInt("remaining_capacity"),
                  rs.getString("location"),
                  rs.getString("provider"));
          appointments.add(appointment);
        }
      }
    }
    return appointments;
  }

  public Appointment getAppointmentById(int appointmentId) throws SQLException {
    String sql = "SELECT * FROM appointment WHERE appointment_id = ?";
    try (Connection conn = Database.getConnection();
        PreparedStatement ps = conn.prepareStatement(sql)) {
      ps.setInt(1, appointmentId);
      try (ResultSet rs = ps.executeQuery()) {
        if (rs.next()) {
          return new Appointment(
              rs.getInt("appointment_id"),
              rs.getDate("date_slot"),
              rs.getString("time_slot"),
              rs.getString("vaccine"),
              rs.getInt("capacity"),
              rs.getInt("remaining_capacity"),
              rs.getString("location"),
              rs.getString("provider"));
        }
      }
    }
    return null;
  }
}
