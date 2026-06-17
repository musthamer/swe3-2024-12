package hbv.web;

import hbv.service.AdminService;
import hbv.service.BookingService;
import jakarta.servlet.*;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import java.io.*;
import org.json.*;

@WebServlet("/api/admin")
public class AdminServlet extends HttpServlet {

  private AdminService adminService = new AdminService();

  @Override
  protected void doGet(HttpServletRequest request, HttpServletResponse response)
      throws IOException, ServletException {

    HttpSession session = request.getSession(false);
    String userRole = (session != null) ? (String) session.getAttribute("userRole") : null;

    if (session == null || !"ADMIN".equals(userRole)) {
      response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
      response.getWriter().println("{\"success\": false, \"message\": \"Keine Berechtigung\"}");
      return;
    }

    String action = request.getParameter("action");
    response.setContentType("application/json");
    PrintWriter out = response.getWriter();

    try {
      if ("get-centers".equals(action)) {
        getCenters(out);
      } else if ("get-vaccines".equals(action)) {
        getVaccines(request, out);
      } else {
        out.println("{\"success\": false, \"message\": \"Unbekannte Aktion\"}");
      }
    } catch (Exception e) {
      response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
      out.println("{\"success\": false, \"message\": \"" + e.getMessage() + "\"}");
      e.printStackTrace();
    }
  }

  @Override
  protected void doPost(HttpServletRequest request, HttpServletResponse response)
      throws IOException, ServletException {
    request.setCharacterEncoding("UTF-8");

    HttpSession session = request.getSession(false);
    String userRole = (session != null) ? (String) session.getAttribute("userRole") : null;

    String action = request.getParameter("action");
    if (session == null || !"ADMIN".equals(userRole)) {
      response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
      response.getWriter().println("{\"success\": false, \"message\": \"Keine Berechtigung\"}");
      return;
    }

    response.setContentType("application/json");
    JSONObject jsonResponse = new JSONObject();

    try {
      if ("create-center".equals(action)) {
        createCenter(request, jsonResponse);
      } else if ("update-inventory".equals(action)) {
        updateInventory(request, jsonResponse);
      } else if ("complete-appointment".equals(action)) {
        completeAppointment(request, jsonResponse);
      } else {
        jsonResponse.put("success", false);
        jsonResponse.put("message", "Unbekannte Aktion");
      }
    } catch (Exception e) {
      jsonResponse.put("success", false);
      jsonResponse.put("message", "Fehler: " + e.getMessage());
      e.printStackTrace();
    }

    PrintWriter out = response.getWriter();
    out.println(jsonResponse.toString());
  }

  private void getCenters(PrintWriter out) throws Exception {
    JSONObject response = new JSONObject();
    JSONArray centers = adminService.getAllCenters();

    response.put("success", true);
    response.put("centers", centers);
    out.println(response.toString());
  }

  private void getVaccines(HttpServletRequest request, PrintWriter out) throws Exception {
    String centerIdStr = request.getParameter("center_id");
    if (centerIdStr == null || centerIdStr.isEmpty()) {
      out.println("{\"success\": false, \"message\": \"center_id is required\"}");
      return;
    }

    // Debug-Ausgabe
    System.out.println("AdminServlet: Fetching vaccines for center ID: " + centerIdStr);

    JSONObject response = new JSONObject();
    JSONArray vaccines = adminService.getVaccinesForCenter(Integer.parseInt(centerIdStr));

    response.put("success", true);
    response.put("vaccines", vaccines);
    out.println(response.toString());
  }

  private void createCenter(HttpServletRequest request, JSONObject jsonResponse) throws Exception {
    String name = request.getParameter("name");
    String address = request.getParameter("address");

    if (name == null || name.trim().isEmpty() || address == null || address.trim().isEmpty()) {
      jsonResponse.put("success", false);
      jsonResponse.put("message", "Name und Adresse sind erforderlich");
      return;
    }

    boolean success = adminService.createCenter(name, address);
    jsonResponse.put("success", success);
    jsonResponse.put(
        "message",
        success
            ? "Impfzentrum wurde erfolgreich erstellt"
            : "Fehler beim Erstellen des Impfzentrums");
  }

  private void updateInventory(HttpServletRequest request, JSONObject jsonResponse)
      throws Exception {
    String centerIdStr = request.getParameter("center_id");
    String vaccineIdStr = request.getParameter("vaccine_id");
    String dosesStr = request.getParameter("doses");

    if (centerIdStr == null || vaccineIdStr == null || dosesStr == null) {
      jsonResponse.put("success", false);
      jsonResponse.put("message", "Alle Felder sind erforderlich");
      return;
    }

    int centerId = Integer.parseInt(centerIdStr);
    int vaccineId = Integer.parseInt(vaccineIdStr);
    int dosesToAdd = Integer.parseInt(dosesStr);

    boolean success = adminService.updateInventory(centerId, vaccineId, dosesToAdd);
    jsonResponse.put("success", success);
    jsonResponse.put(
        "message",
        success
            ? "Impfbestand wurde erfolgreich aktualisiert"
            : "Fehler beim Aktualisieren des Impfbestands");
  }

  // Neue Methode zum Abschließen eines Termins
  private void completeAppointment(HttpServletRequest request, JSONObject jsonResponse)
      throws Exception {
    String bookingIdStr = request.getParameter("booking_id");

    if (bookingIdStr == null || bookingIdStr.trim().isEmpty()) {
      jsonResponse.put("success", false);
      jsonResponse.put("message", "Keine Buchungs-ID angegeben");
      return;
    }

    int bookingId = Integer.parseInt(bookingIdStr);
    BookingService bookingService = new BookingService();
    boolean success = bookingService.updateAppointmentStatus(bookingId, "COMPLETED");

    jsonResponse.put("success", success);
    jsonResponse.put(
        "message",
        success
            ? "Termin erfolgreich als durchgeführt markiert"
            : "Termin konnte nicht aktualisiert werden");
  }
}
