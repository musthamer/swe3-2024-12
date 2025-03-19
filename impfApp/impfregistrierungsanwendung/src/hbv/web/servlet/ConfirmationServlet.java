package hbv.web.servlet;

import hbv.service.Database;
import jakarta.servlet.*;
import jakarta.servlet.http.*;
import java.io.*;
import java.sql.*;

public class ConfirmationServlet extends HttpServlet {
    private void generatePdf(String inputData, HttpServletResponse resp) throws IOException {
        String scriptPath = getServletContext().getRealPath("/pdfgen/generate_appointment_pdf.sh");
        ProcessBuilder pb = new ProcessBuilder(scriptPath);
        Process process = pb.start();
        try(OutputStream os = process.getOutputStream()) {
            os.write(inputData.getBytes());
        }
        resp.setContentType("application/pdf");
        try(InputStream is = process.getInputStream(); OutputStream os = resp.getOutputStream()) {
            byte[] buffer = new byte[4096];
            int bytesRead;
            while ((bytesRead = is.read(buffer)) != -1) {
                os.write(buffer, 0, bytesRead);
            }
        }
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        String bookingIdParam = req.getParameter("booking_id");
        if(bookingIdParam == null) {
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
        try(Connection conn = Database.getConnection()) {
            String sql = "SELECT u.vorname, u.nachname, u.geburtsdatum, a.date_slot, a.time_slot, a.vaccine, a.location " +
                         "FROM booking b " +
                         "JOIN user_account u ON b.user_id = u.user_id " +
                         "JOIN appointment a ON b.appointment_id = a.appointment_id " +
                         "WHERE b.booking_id = ?";
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setInt(1, bookingId);
            ResultSet rs = ps.executeQuery();
            if(rs.next()) {
                String firstname = rs.getString("vorname");
                String lastname = rs.getString("nachname");
                java.sql.Date birthdate = rs.getDate("geburtsdatum");
                java.sql.Date dateSlot = rs.getDate("date_slot");
                String timeSlot = rs.getString("time_slot");
                String vaccine = rs.getString("vaccine");
                String location = rs.getString("location");
                String street = "";
                String city = "";
                String postalcode = "";
                String starttime = (dateSlot != null ? dateSlot.toString() : "") + "T" + (timeSlot != null ? timeSlot : "");
                String vaccinationcenter = location;
                String validationcode = "VAL" + bookingId;
                String inputData = firstname + ";" + lastname + ";" +
                                   (birthdate != null ? birthdate.toString() : "") + ";" +
                                   street + ";" + city + ";" + postalcode + ";" +
                                   starttime + ";" + vaccine + ";" + vaccinationcenter + ";" +
                                   validationcode + ";";
                generatePdf(inputData, resp);
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
