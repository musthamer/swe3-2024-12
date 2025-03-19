package hbv.service;

import hbv.model.Booking;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class BookingServiceImpl implements BookingService {
  @Override
  public int bookAppointment(int userId, int appointmentId, String bookingName) throws Exception {
    try (Connection conn = Database.getConnection()) {
      PreparedStatement psCheck =
          conn.prepareStatement(
              "SELECT remaining_capacity FROM appointment WHERE appointment_id=?");
      psCheck.setInt(1, appointmentId);
      ResultSet rs = psCheck.executeQuery();
      if (rs.next()) {
        if (rs.getInt("remaining_capacity") <= 0) throw new SQLException("Termin ausgebucht.");
      } else {
        throw new SQLException("Termin nicht gefunden.");
      }
      rs.close();
      psCheck.close();
      PreparedStatement psInsert =
          conn.prepareStatement(
              "INSERT INTO booking (user_id, appointment_id, booking_name) VALUES (?,?,?)",
              Statement.RETURN_GENERATED_KEYS);
      psInsert.setInt(1, userId);
      psInsert.setInt(2, appointmentId);
      psInsert.setString(3, bookingName);
      psInsert.executeUpdate();
      ResultSet generatedKeys = psInsert.getGeneratedKeys();
      int bookingId = (generatedKeys.next()) ? generatedKeys.getInt(1) : -1;
      generatedKeys.close();
      psInsert.close();
      PreparedStatement psUpdate =
          conn.prepareStatement(
              "UPDATE appointment SET remaining_capacity = remaining_capacity - 1 WHERE"
                  + " appointment_id=?");
      psUpdate.setInt(1, appointmentId);
      psUpdate.executeUpdate();
      psUpdate.close();
      return bookingId;
    }
  }

  @Override
  public List<Booking> getBookingsForUser(int userId) throws SQLException {
    List<Booking> bookings = new ArrayList<>();
    String sql =
        "SELECT booking_id, user_id, appointment_id, booking_name, booking_time FROM booking WHERE"
            + " user_id = ?";
    try (Connection conn = Database.getConnection();
        PreparedStatement ps = conn.prepareStatement(sql)) {
      ps.setInt(1, userId);
      try (ResultSet rs = ps.executeQuery()) {
        while (rs.next()) {
          Booking booking =
              new Booking(
                  rs.getInt("booking_id"),
                  rs.getInt("user_id"),
                  rs.getInt("appointment_id"),
                  rs.getString("booking_name"),
                  rs.getTimestamp("booking_time"));
          bookings.add(booking);
        }
      }
    }
    return bookings;
  }
}
