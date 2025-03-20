package hbv.web.servlet;

import hbv.service.BookingService;
import hbv.service.BookingServiceImpl;
import hbv.service.LoggingBookingServiceDecorator;
import jakarta.servlet.*;
import jakarta.servlet.http.*;
import java.io.IOException;

public class AppointmentServlet extends HttpServlet {

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        
        int appointmentId = Integer.parseInt(req.getParameter("appointment_id"));
        String bookingName = req.getParameter("booking_name");

        HttpSession session = req.getSession(false);
        if (session == null || session.getAttribute("userId") == null) {
            resp.sendRedirect("login");
            return;
        }

        int userId = (Integer) session.getAttribute("userId");

        try {
            //  Bestehende Buchungslogik mit Logging-Decorator
            BookingService bookingService = new LoggingBookingServiceDecorator(new BookingServiceImpl());
            int bookingId = bookingService.bookAppointment(userId, appointmentId, bookingName);

            //  Neue Funktionalität: QR-Code und PDF bestätigen den Termin
      if (bookingId != -1) {
                resp.sendRedirect("confirmation?booking_id=" + bookingId);
            } else {
                resp.sendRedirect("error?error=booking_failed");
            }

        } catch (Exception e) {
            throw new ServletException("Buchungsfehler: " + e.getMessage(), e);
        }
    } 
	    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        resp.sendRedirect("dashboard");
    }
}

