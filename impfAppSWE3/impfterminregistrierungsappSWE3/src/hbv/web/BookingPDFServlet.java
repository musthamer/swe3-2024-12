package hbv.web;

import jakarta.servlet.*;
import jakarta.servlet.http.*;
import java.io.*;
import java.sql.*;
import java.util.*;
import javax.naming.*;
import javax.sql.*;

public class BookingPDFServlet extends HttpServlet {

  @Override
  protected void doGet(HttpServletRequest request, HttpServletResponse response)
      throws IOException, ServletException {

    HttpSession session = request.getSession(false);
    Boolean isLoggedIn = (session != null) ? (Boolean) session.getAttribute("loggedin") : false;
    Integer userId = (session != null) ? (Integer) session.getAttribute("userId") : null;

    if (isLoggedIn == null || !isLoggedIn || userId == null) {
      response.sendRedirect("login");
      return;
    }

    String bookingIdStr = request.getParameter("id");

    if (bookingIdStr == null || bookingIdStr.trim().isEmpty()) {
      response.setContentType("text/html");
      PrintWriter out = response.getWriter();
      out.println("<!DOCTYPE html><html><body>");
      out.println("<h2>Fehler</h2>");
      out.println("<p>Keine Buchungs-ID angegeben.</p>");
      out.println("<p><a href='appointments'>Zurück zu den Terminen</a></p>");
      out.println("</body></html>");
      return;
    }

    int bookingId;
    try {
      bookingId = Integer.parseInt(bookingIdStr);
    } catch (NumberFormatException e) {
      response.setContentType("text/html");
      PrintWriter out = response.getWriter();
      out.println("<!DOCTYPE html><html><body>");
      out.println("<h2>Fehler</h2>");
      out.println("<p>Ungültige Buchungs-ID.</p>");
      out.println("<p><a href='appointments'>Zurück zu den Terminen</a></p>");
      out.println("</body></html>");
      return;
    }

    try {
      Context initCtx = new InitialContext();
      DataSource ds = (DataSource) initCtx.lookup("java:/comp/env/jdbc/mariadb");

      try (Connection connection = ds.getConnection()) {
        // Buchungsdaten abrufen
        PreparedStatement ps =
            connection.prepareStatement(
                "SELECT b.id, p.first_name, p.last_name, t.start_time, c.name AS center_name,"
                    + " v.name AS vaccine_name FROM booking b JOIN person p ON b.person_id = p.id"
                    + " JOIN timeslot t ON b.timeslot_id = t.id JOIN vaccination_center c ON"
                    + " t.center_id = c.id JOIN vaccine v ON b.vaccine_id = v.id WHERE b.id = ? AND"
                    + " b.account_id = ? AND b.status = 'CONFIRMED'");
        ps.setInt(1, bookingId);
        ps.setInt(2, userId);
        ResultSet rs = ps.executeQuery();

        if (rs.next()) {
          String name = rs.getString("first_name") + " " + rs.getString("last_name");
          java.util.Date appointmentDate = rs.getTimestamp("start_time");
          String vaccinationCenter = rs.getString("center_name");
          String vaccineType = rs.getString("vaccine_name");

          ServletContext ctx = getServletContext();
          byte[] pdfData =
              PDFGenerator.generateVaccinationConfirmation(
                  name,
                  appointmentDate,
                  vaccinationCenter,
                  vaccineType,
                  bookingId,
                  ctx.getInitParameter("baseurl"),
                  ctx.getInitParameter("webapp"));

          // PDF als Download anbieten
          response.setContentType("application/pdf");
          response.setHeader(
              "Content-Disposition", "attachment; filename=impftermin_" + bookingId + ".pdf");
          response.setContentLength(pdfData.length);

          ServletOutputStream outputStream = response.getOutputStream();
          outputStream.write(pdfData);
          outputStream.flush();
        } else {
          response.setContentType("text/html");
          PrintWriter out = response.getWriter();
          out.println("<!DOCTYPE html><html><body>");
          out.println("<h2>Fehler</h2>");
          out.println(
              "<p>Die Buchung konnte nicht gefunden werden oder gehört nicht zu Ihrem Account oder"
                  + " wurde storniert.</p>");
          out.println("<p><a href='appointments'>Zurück zu den Terminen</a></p>");
          out.println("</body></html>");
        }
      }
    } catch (Exception e) {
      response.setContentType("text/html");
      PrintWriter out = response.getWriter();
      out.println("<!DOCTYPE html><html><body>");
      out.println("<h2>Fehler</h2>");
      out.println("<p>Es ist ein Fehler aufgetreten: " + e.getMessage() + "</p>");
      out.println("<p><a href='appointments'>Zurück zu den Terminen</a></p>");
      out.println("</body></html>");
      e.printStackTrace(out);
    }
  }
}
