package hbv.web;

import jakarta.servlet.*;
import jakarta.servlet.http.*;
import java.io.*;
import java.sql.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.charset.StandardCharsets;

public class CancelServlet extends HttpServlet {
    private String readTemplate(String filename, HttpServletRequest req) throws IOException {
        String realPath = req.getServletContext().getRealPath("/static/" + filename);
        return new String(Files.readAllBytes(Paths.get(realPath)), StandardCharsets.UTF_8);
    }
    
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
      throws ServletException, IOException {
        int bookingId = Integer.parseInt(req.getParameter("booking_id"));
        int userId = ((Number) req.getSession(false).getAttribute("userId")).intValue();
        try (Connection conn = hbv.service.Database.getConnection()) {
            PreparedStatement psQuery = conn.prepareStatement("SELECT appointment_id FROM booking WHERE booking_id=? AND user_id=?");
            psQuery.setInt(1, bookingId);
            psQuery.setInt(2, userId);
            ResultSet rs = psQuery.executeQuery();
            if(rs.next()){
                int appointmentId = rs.getInt("appointment_id");
                PreparedStatement psDelete = conn.prepareStatement("DELETE FROM booking WHERE booking_id=?");
                psDelete.setInt(1, bookingId);
                psDelete.executeUpdate();
                PreparedStatement psUpdate = conn.prepareStatement("UPDATE appointment SET remaining_capacity = remaining_capacity + 1 WHERE appointment_id=?");
                psUpdate.setInt(1, appointmentId);
                psUpdate.executeUpdate();
            }
            resp.sendRedirect("appointments");
        } catch(SQLException e) {
            resp.setContentType("text/html;charset=UTF-8");
            PrintWriter out = resp.getWriter();
            String header = readTemplate("header.html", req).replace("<!-- Title placeholder -->", "Stornierungsfehler");
            out.println(header);
            out.println("<p>Fehler: " + e.getMessage() + "</p>");
            out.println("<button onclick=\"window.location.href='appointments'\">Zurück</button>");
            String footer = readTemplate("footer.html", req);
            out.println(footer);
        }
    }
}
