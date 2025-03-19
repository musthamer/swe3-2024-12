package hbv.service;

import hbv.model.Booking;
import java.sql.SQLException;
import java.util.List;

public class LoggingBookingServiceDecorator implements BookingService {
    private BookingService delegate;

    public LoggingBookingServiceDecorator(BookingService delegate) {
        this.delegate = delegate;
    }

    @Override
    public int bookAppointment(int userId, int appointmentId, String bookingName) throws Exception {
        System.out.println("Buchungsversuch für Termin " + appointmentId + " von Nutzer " + userId);
        int bookingId = delegate.bookAppointment(userId, appointmentId, bookingName);
        System.out.println("Buchung erfolgreich: " + bookingId);
        return bookingId;
    }

    @Override
    public List<Booking> getBookingsForUser(int userId) throws SQLException {
        return delegate.getBookingsForUser(userId);
    }

    @Override
    public List<Booking> getBookingsForCenter(String centerName) throws SQLException {
        return delegate.getBookingsForCenter(centerName);
    }
}
