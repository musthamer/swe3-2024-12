package hbv.web.servlet;

import hbv.service.Database;
import hbv.service.EmailService;
import hbv.service.RedisConfig;  // Import für Redis-Konfiguration
import redis.clients.jedis.Jedis; // Import für Redis
import jakarta.servlet.*;
import jakarta.servlet.http.*;
import java.io.*;
import java.sql.*;

public class ConfirmationServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        String bookingIdParam = req.getParameter("booking_id");
        if (bookingIdParam == null) {
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "Booking ID fehlt");
            return;
        }

        int bookingId;
        try {
            bookingId = Integer.parseInt(bookingIdParam);
        } catch (NumberFormatException e) {
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "Ungültige Booking ID");
            return;
        }

        try (Connection conn = Database.getConnection()) {
            String sql = "SELECT u.vorname, u.nachname, u.geburtsdatum, u.email, " +
                         "a.date_slot, a.time_slot, a.vaccine, a.location " +
                         "FROM booking b " +
                         "JOIN user_account u ON b.user_id = u.user_id " +
                         "JOIN appointment a ON b.appointment_id = a.appointment_id " +
                         "WHERE b.booking_id = ?";
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setInt(1, bookingId);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                String firstname = rs.getString("vorname");
                String lastname = rs.getString("nachname");
                String email = rs.getString("email");

                // 🔹 PDF und QR-Code aus Redis abrufen
                byte[] pdfData;
                byte[] qrData;

                try (Jedis jedis = RedisConfig.getConnection()) {
                    pdfData = jedis.get(("pdf:" + bookingId).getBytes());
                    qrData = jedis.get(("qr:" + bookingId).getBytes());

                    if (pdfData == null || qrData == null) {
                        throw new ServletException("PDF oder QR-Code nicht in Redis gefunden.");
                    }
                }

                // 🔹 Fake-E-Mail versenden mit PDF und QR-Code aus Redis
                try {
                    EmailService.getInstance().generateBookingEmailWithAttachments(
                        email,
                        pdfData,
                        qrData,
                        String.valueOf(bookingId)
                    );
                } catch (Exception e) {
                    throw new ServletException("Fehler beim Senden der E-Mail: " + e.getMessage(), e);
                }

                // 🔹 Erfolgsseite anzeigen
                resp.setContentType("text/html");
                resp.getWriter().println(
                    "<html>" +
                        "<head><title>Termin bestätigt</title></head>" +
                        "<body>" +
                            "<h1>✅ Ihre Buchung war erfolgreich!</h1>" +
                            "<p>Ihre Buchungs-ID: <strong>" + bookingId + "</strong></p>" +
                            "<p>Ein Bestätigungs-PDF und QR-Code wurden erstellt.</p>" +
                            "<p>Prüfen Sie Ihren E-Mail-Posteingang (dies ist eine simulierte E-Mail).</p>" +
                            "<p><a href='dashboard'>Zurück zum Dashboard</a></p>" +
                        "</body>" +
                    "</html>"
                );

            } else {
                resp.sendError(HttpServletResponse.SC_NOT_FOUND, "Buchung nicht gefunden");
            }

            rs.close();
            ps.close();

        } catch (SQLException e) {
            throw new ServletException("Fehler beim Abrufen der Buchungsdaten: " + e.getMessage(), e);
        }
    }
}

