package hbv.service;

import hbv.model.Booking;
import java.sql.SQLException;
import java.util.List;

public interface BookingService {
  int bookAppointment(int userId, int appointmentId, String bookingName) throws Exception;

  List<Booking> getBookingsForUser(int userId) throws SQLException;
}
