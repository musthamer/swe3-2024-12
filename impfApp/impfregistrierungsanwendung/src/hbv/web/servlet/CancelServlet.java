package hbv.web.servlet;

import hbv.service.Database;
import jakarta.servlet.*;
import jakarta.servlet.http.*;
import java.io.IOException;
import java.sql.*;

public class CancelServlet extends HttpServlet {
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        HttpSession session = req.getSession(false);
        if(session == null || session.getAttribute("userId") == null) {
            resp.sendRedirect("login");
            return;
        }
        int userId = (Integer) session.getAttribute("userId");
        int bookingId = Integer.parseInt(req.getParameter("booking_id"));
        try (Connection conn = Database.getConnection()) {
            PreparedStatement ps = conn.prepareStatement("DELETE FROM booking WHERE booking_id=? AND user_id=?");
            ps.setInt(1, bookingId);
            ps.setInt(2, userId);
            int affected = ps.executeUpdate();
            ps.close();
            if(affected > 0) {
                resp.sendRedirect("dashboard");
            } else {
                resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "Buchung nicht gefunden oder nicht stornierbar.");
            }
        } catch(SQLException e) {
            throw new ServletException("Stornierungsfehler: " + e.getMessage(), e);
        }
    }
}
