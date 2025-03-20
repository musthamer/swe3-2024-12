package hbv.service;

import hbv.model.Booking;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import hbv.service.RedisConfig;
import hbv.service.BookingDocumentBuilder;
import redis.clients.jedis.Jedis;

public class BookingServiceImpl implements BookingService {

    @Override
    public int bookAppointment(int userId, int appointmentId, String bookingName) throws Exception {
        try (Connection conn = Database.getConnection()) {
            PreparedStatement psCheck = conn.prepareStatement("SELECT remaining_capacity FROM appointment WHERE appointment_id=?");
            psCheck.setInt(1, appointmentId);
            ResultSet rs = psCheck.executeQuery();
            if (rs.next()) {
                if (rs.getInt("remaining_capacity") <= 0) {
                    throw new SQLException("Termin ausgebucht.");
                }
            } else {
                throw new SQLException("Termin nicht gefunden.");
            }
            rs.close();
            psCheck.close();

            PreparedStatement psInsert = conn.prepareStatement(
                "INSERT INTO booking (user_id, appointment_id, booking_name) VALUES (?,?,?)",
                Statement.RETURN_GENERATED_KEYS
            );
            psInsert.setInt(1, userId);
            psInsert.setInt(2, appointmentId);
            psInsert.setString(3, bookingName);
            psInsert.executeUpdate();

            ResultSet generatedKeys = psInsert.getGeneratedKeys();
            int bookingId = (generatedKeys.next()) ? generatedKeys.getInt(1) : -1;
            generatedKeys.close();
            psInsert.close();

            PreparedStatement psUpdate = conn.prepareStatement(
                "UPDATE appointment SET remaining_capacity = remaining_capacity - 1 WHERE appointment_id=?"
            );
            psUpdate.setInt(1, appointmentId);
            psUpdate.executeUpdate();
            psUpdate.close();

            // 🔹 Neue Logik zur Speicherung in Redis
            if (bookingId != -1) {
                String email = Database.getEmailByUserId(userId);  // E-Mail aus der Datenbank holen

                // QR-Code und PDF generieren und in Redis speichern
                new BookingDocumentBuilder()
                        .setBookingId(String.valueOf(bookingId))
                        .setEmail(email)
                        .generateQRCode()
                        .generatePdf()
                        .build();

                // 🔹 PDF und QR-Code aus Redis abrufen und E-Mail versenden
                try (Jedis jedis = RedisConfig.getConnection()) {
			byte[] pdfData = jedis.get(("pdf:" + bookingId).getBytes());
                    byte[] qrData = jedis.get(("qr:" + bookingId).getBytes());

                    if (pdfData == null || qrData == null) {
                        throw new Exception("PDF oder QR-Code nicht in Redis gefunden.");
                    }

                    // E-Mail-Service aufrufen und Anhänge senden
                    EmailService.getInstance().generateBookingEmailWithAttachments(
                        email,       // Empfänger
                        pdfData,     // PDF-Datei aus Redis
                        qrData,      // QR-Code aus Redis
                        String.valueOf(bookingId)
                    );
                }
            }
            return bookingId;
        }
    }

    @Override
    public List<Booking> getBookingsForUser(int userId) throws SQLException {
        List<Booking> bookings = new ArrayList<>();
        String sql = "SELECT booking_id, user_id, appointment_id, booking_name, booking_time FROM booking WHERE user_id = ?";
        try (Connection conn = Database.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Booking booking = new Booking(
                        rs.getInt("booking_id"),
                        rs.getInt("user_id"),
                        rs.getInt("appointment_id"),
                        rs.getString("booking_name"),
                        rs.getTimestamp("booking_time")
                    );
                    bookings.add(booking);
                }
            }
        }
        return bookings;
    }

    @Override
    public List<Booking> getBookingsForCenter(String centerName) throws SQLException {
        String sql = "SELECT b.booking_id, b.user_id, b.appointment_id, b.booking_name, b.booking_time " +
                     "FROM booking b " +
                     "JOIN appointment a ON b.appointment_id = a.appointment_id " +
                     "WHERE a.location = ?";
        List<Booking> bookings = new ArrayList<>();
        try (Connection conn = Database.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, centerName);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Booking booking = new Booking(
                        rs.getInt("booking_id"),
                        rs.getInt("user_id"),
                        rs.getInt("appointment_id"),
                        rs.getString("booking_name"),
                        rs.getTimestamp("booking_time")
                    );
                    bookings.add(booking);
                }
            }
        }
        return bookings;
    }
}

