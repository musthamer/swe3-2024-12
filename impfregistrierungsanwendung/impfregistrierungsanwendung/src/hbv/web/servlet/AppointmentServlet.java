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
    int userId = ((Number) session.getAttribute("userId")).intValue();
    try {
      BookingService bookingService = new LoggingBookingServiceDecorator(new BookingServiceImpl());
      int bookingId = bookingService.bookAppointment(userId, appointmentId, bookingName);
      resp.sendRedirect("confirmation?booking_id=" + bookingId);
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
