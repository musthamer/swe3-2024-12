package hbv.web;

import hbv.service.BookingService;
import jakarta.servlet.*;
import jakarta.servlet.http.*;
import java.io.*;
import java.util.Map;

public class BookingPDFServlet extends HttpServlet {

  private final BookingService bookingService = new BookingService();

  @Override
  protected void doGet(HttpServletRequest request, HttpServletResponse response)
      throws IOException, ServletException {

    HttpSession session = request.getSession(false);
    Boolean isLoggedIn = (session != null) ? (Boolean) session.getAttribute("loggedin") : false;
    Integer userId = (session != null) ? (Integer) session.getAttribute("userId") : null;

    if (isLoggedIn == null || !isLoggedIn || userId == null) {
      response.sendRedirect(request.getContextPath() + "/");
      return;
    }

    String bookingIdStr = request.getParameter("id");

    if (bookingIdStr == null || bookingIdStr.trim().isEmpty()) {
      showError(response, "Keine Buchungs-ID angegeben.");
      return;
    }

    int bookingId;
    try {
      bookingId = Integer.parseInt(bookingIdStr);
    } catch (NumberFormatException e) {
      showError(response, "Ungültige Buchungs-ID.");
      return;
    }

    try {
      Map<String, Object> appointment =
          bookingService.getConfirmedAppointmentForAccount(bookingId, userId);

      if (appointment != null) {
        ServletContext ctx = getServletContext();
        byte[] pdfData =
            PDFGenerator.generateVaccinationConfirmation(
                (String) appointment.get("personName"),
                (java.util.Date) appointment.get("startTime"),
                (String) appointment.get("centerName"),
                (String) appointment.get("vaccineName"),
                bookingId,
                ctx.getInitParameter("baseurl"),
                ctx.getInitParameter("webapp"));

        response.setContentType("application/pdf");
        response.setHeader(
            "Content-Disposition", "attachment; filename=impftermin_" + bookingId + ".pdf");
        response.setContentLength(pdfData.length);

        ServletOutputStream outputStream = response.getOutputStream();
        outputStream.write(pdfData);
        outputStream.flush();
      } else {
        showError(
            response,
            "Die Buchung konnte nicht gefunden werden oder gehört nicht zu Ihrem Account oder wurde"
                + " storniert.");
      }
    } catch (Exception e) {
      showError(response, "Es ist ein Fehler aufgetreten: " + e.getMessage());
      e.printStackTrace();
    }
  }

  private void showError(HttpServletResponse response, String message) throws IOException {
    response.setContentType("text/html");
    PrintWriter out = response.getWriter();
    out.println("<!DOCTYPE html><html><body>");
    out.println("<h2>Fehler</h2>");
    out.println("<p>" + message + "</p>");
    out.println("<p><a href='./#/booking'>Zurück zu den Terminen</a></p>");
    out.println("</body></html>");
  }
}
