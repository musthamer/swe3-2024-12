package hbv.web;

import hbv.service.*;
import jakarta.servlet.*;
import jakarta.servlet.annotation.MultipartConfig;
import jakarta.servlet.http.*;
import java.io.*;
import java.util.*;
import org.json.JSONObject;

@MultipartConfig
public class ApiServlet extends HttpServlet {

  private VaccinationCenterService centerService = new VaccinationCenterService();
  private BookingService bookingService = new BookingService();
  private TimeslotService timeslotService = new TimeslotService();
  private PersonService personService = new PersonService();

  private String toJson(Map<String, Object> jsonResponse) {
    return new JSONObject(jsonResponse).toString();
  }

  @Override
  protected void doGet(HttpServletRequest request, HttpServletResponse response)
      throws IOException, ServletException {

    String action = request.getPathInfo();
    if (action == null) action = "";

    response.setContentType("application/json");
    PrintWriter out = response.getWriter();
    Map<String, Object> jsonResponse = new HashMap<>();

    try {
      if ("/check-login".equals(action)) {
        checkLogin(request, jsonResponse);
      } else if ("/vaccination-centers".equals(action)) {
        getCenters(jsonResponse);
      } else if ("/vaccines".equals(action)) {
        getVaccines(request, jsonResponse);
      } else if ("/timeslots".equals(action)) {
        getTimeslots(request, jsonResponse);
      } else if ("/vaccine-inventory".equals(action)) {
        getVaccineInventory(request, jsonResponse);
      } else if ("/appointments".equals(action)) {
        getAppointments(request, jsonResponse);
      } else if ("/appointment-details".equals(action)) {
        getAppointmentDetails(request, response);
        return; // Antwort bereits gesendet
      } else {
        jsonResponse.put("success", false);
        jsonResponse.put("message", "Unbekannte API-Anfrage");
      }
    } catch (Exception e) {
      jsonResponse.put("success", false);
      jsonResponse.put("message", "Fehler: " + e.getMessage());
    }

    out.println(toJson(jsonResponse));
  }

  @Override
  protected void doPost(HttpServletRequest request, HttpServletResponse response)
      throws IOException, ServletException {

    request.setCharacterEncoding("UTF-8");
    String action = request.getPathInfo();
    if (action == null) action = "";

    response.setContentType("application/json");
    PrintWriter out = response.getWriter();
    Map<String, Object> jsonResponse = new HashMap<>();

    try {
      if ("/book-appointment".equals(action)) {
        bookAppointment(request, response, jsonResponse);
      } else if ("/appointments".equals(action)) {
        postAppointments(request, jsonResponse);
      } else {
        jsonResponse.put("success", false);
        jsonResponse.put("message", "Unbekannte API-Anfrage");
      }
    } catch (Exception e) {
      jsonResponse.put("success", false);
      jsonResponse.put("message", "Fehler: " + e.getMessage());
    }

    out.println(toJson(jsonResponse));
  }

  private void checkLogin(HttpServletRequest request, Map<String, Object> jsonResponse) {
    HttpSession session = request.getSession(false);
    Boolean isLoggedIn = (session != null) ? (Boolean) session.getAttribute("loggedin") : false;
    String userName = (session != null) ? (String) session.getAttribute("userName") : "";
    String userRole = (session != null) ? (String) session.getAttribute("userRole") : "";
    String email = (session != null) ? (String) session.getAttribute("email") : "";
    Integer userId = (session != null) ? (Integer) session.getAttribute("userId") : null;

    jsonResponse.put("loggedIn", isLoggedIn != null && isLoggedIn);
    jsonResponse.put("userName", userName != null ? userName : "");
    jsonResponse.put("userRole", userRole != null ? userRole : "");
    jsonResponse.put("email", email != null ? email : "");

    if (Boolean.TRUE.equals(isLoggedIn) && userId != null) {
      try {
        Map<String, Object> profile = personService.getAccountHolderProfile(userId);
        jsonResponse.put("firstName", profile.getOrDefault("firstName", ""));
        jsonResponse.put("lastName", profile.getOrDefault("lastName", ""));
        jsonResponse.put("dateOfBirth", profile.getOrDefault("dateOfBirth", ""));
        if (profile.get("email") != null) {
          jsonResponse.put("email", profile.get("email"));
        }
      } catch (Exception e) {
        jsonResponse.put("profileError", e.getMessage());
      }
    }
  }

  private void getCenters(Map<String, Object> jsonResponse) throws Exception {
    List<Map<String, Object>> centers = centerService.getAllCenters();
    jsonResponse.put("success", true);
    jsonResponse.put("centers", centers);
  }

  private void getVaccines(HttpServletRequest request, Map<String, Object> jsonResponse)
      throws Exception {
    String centerIdStr = request.getParameter("center_id");

    if (centerIdStr == null || centerIdStr.trim().isEmpty()) {
      jsonResponse.put("success", false);
      jsonResponse.put("message", "Keine Impfzentrum-ID angegeben.");
      return;
    }

    int centerId = Integer.parseInt(centerIdStr);
    List<Map<String, Object>> vaccines = centerService.getVaccinesForCenter(centerId);
    jsonResponse.put("success", true);
    jsonResponse.put("vaccines", vaccines);
  }

  private void getTimeslots(HttpServletRequest request, Map<String, Object> jsonResponse)
      throws Exception {
    String centerIdStr = request.getParameter("center_id");

    if (centerIdStr == null || centerIdStr.trim().isEmpty()) {
      jsonResponse.put("success", false);
      jsonResponse.put("message", "Keine Impfzentrum-ID angegeben.");
      return;
    }

    int centerId = Integer.parseInt(centerIdStr);
    List<Map<String, Object>> timeslots = timeslotService.getAvailableTimeslotsForCenter(centerId);
    jsonResponse.put("success", true);
    jsonResponse.put("timeslots", timeslots);
  }

  private void getVaccineInventory(HttpServletRequest request, Map<String, Object> jsonResponse)
      throws Exception {
    String centerIdStr = request.getParameter("center_id");
    if (centerIdStr != null) {
      int centerId = Integer.parseInt(centerIdStr);
      Map<String, Object> inventory = centerService.getVaccineInventory(centerId);
      jsonResponse.putAll(inventory);
      jsonResponse.put("success", true);
    }
  }

  private void getAppointments(HttpServletRequest request, Map<String, Object> jsonResponse)
      throws Exception {
    HttpSession session = request.getSession(false);
    Boolean isLoggedIn = (session != null) ? (Boolean) session.getAttribute("loggedin") : false;
    Integer userId = (session != null) ? (Integer) session.getAttribute("userId") : null;

    if (isLoggedIn != null && isLoggedIn && userId != null) {
      List<Map<String, Object>> appointments = bookingService.getAppointmentsForUser(userId);
      jsonResponse.put("success", true);
      jsonResponse.put("appointments", appointments);
    } else {
      jsonResponse.put("success", false);
      jsonResponse.put("message", "Nicht angemeldet");
    }
  }

  private void postAppointments(HttpServletRequest request, Map<String, Object> jsonResponse)
      throws Exception {
    HttpSession session = request.getSession(false);
    Boolean isLoggedIn = (session != null) ? (Boolean) session.getAttribute("loggedin") : false;
    Integer userId = (session != null) ? (Integer) session.getAttribute("userId") : null;

    if (isLoggedIn == null || !isLoggedIn || userId == null) {
      jsonResponse.put("success", false);
      jsonResponse.put("message", "Nicht angemeldet");
      return;
    }

    String action = request.getParameter("action");
    if ("cancel".equals(action)) {
      String idStr = request.getParameter("id");
      if (idStr == null || idStr.trim().isEmpty()) {
        jsonResponse.put("success", false);
        jsonResponse.put("message", "Keine Termin-ID angegeben");
        return;
      }

      int bookingId = Integer.parseInt(idStr);
      Map<String, Object> cancelResult = bookingService.cancelAppointment(userId, bookingId);
      jsonResponse.putAll(cancelResult);

      if (Boolean.TRUE.equals(cancelResult.get("success"))) {
        List<Map<String, Object>> appointments = bookingService.getAppointmentsForUser(userId);
        jsonResponse.put("appointments", appointments);
      }
    } else {
      jsonResponse.put("success", false);
      jsonResponse.put("message", "Unbekannte Aktion");
    }
  }

  private void getAppointmentDetails(HttpServletRequest request, HttpServletResponse response)
      throws IOException, ServletException {

    response.setContentType("application/json; charset=UTF-8");
    PrintWriter out = response.getWriter();
    Map<String, Object> jsonResponse = new HashMap<>();

    try {
      String bookingIdStr = request.getParameter("id");
      if (bookingIdStr == null || bookingIdStr.trim().isEmpty()) {
        jsonResponse.put("success", false);
        jsonResponse.put("message", "Keine Buchungs-ID angegeben");
        out.write(toJson(jsonResponse));
        return;
      }

      HttpSession session = request.getSession(false);
      String userRole = (session != null) ? (String) session.getAttribute("userRole") : null;

      if (session == null || !"ADMIN".equals(userRole)) {
        jsonResponse.put("success", false);
        jsonResponse.put("message", "Keine Berechtigung für diese Aktion");
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        out.write(toJson(jsonResponse));
        return;
      }

      int bookingId = Integer.parseInt(bookingIdStr);
      Map<String, Object> appointmentDetails = bookingService.getAppointmentById(bookingId);

      if (appointmentDetails != null) {
        jsonResponse.put("success", true);
        jsonResponse.put("appointment", appointmentDetails);
      } else {
        jsonResponse.put("success", false);
        jsonResponse.put("message", "Termin nicht gefunden");
      }
    } catch (Exception e) {
      jsonResponse.put("success", false);
      jsonResponse.put("message", "Fehler beim Abrufen der Termindaten: " + e.getMessage());
    }

    out.write(toJson(jsonResponse));
  }

  private void bookAppointment(
      HttpServletRequest request, HttpServletResponse response, Map<String, Object> jsonResponse)
      throws Exception {
    HttpSession session = request.getSession(false);
    Boolean isLoggedIn = (session != null) ? (Boolean) session.getAttribute("loggedin") : false;
    Integer userId = (session != null) ? (Integer) session.getAttribute("userId") : null;
    String userRole = (session != null) ? (String) session.getAttribute("userRole") : null;

    if (isLoggedIn == null || !isLoggedIn || userId == null) {
      jsonResponse.put("success", false);
      jsonResponse.put("message", "Sie müssen angemeldet sein, um einen Termin zu buchen.");
      return;
    }

    if ("ADMIN".equals(userRole)) {
      jsonResponse.put("success", false);
      jsonResponse.put("message", "Keine Berechtigung für diese Aktion");
      response.setStatus(HttpServletResponse.SC_FORBIDDEN);
      return;
    }

    String timeslotId = request.getParameter("timeslot_id");
    String vaccineId = request.getParameter("vaccine_id");
    String firstName = request.getParameter("first_name");
    String lastName = request.getParameter("last_name");
    String dateOfBirth = request.getParameter("date_of_birth");
    String bookingFor = request.getParameter("booking_for");
    boolean forSelf = !"other".equals(bookingFor);

    if (timeslotId != null
        && vaccineId != null
        && firstName != null
        && lastName != null
        && dateOfBirth != null) {

      int personId =
          personService.resolvePersonForBooking(userId, firstName, lastName, dateOfBirth, forSelf);

      ServletContext ctx = request.getServletContext();
      String baseUrl = ctx.getInitParameter("baseurl");
      String webapp = ctx.getInitParameter("webapp");

      Map<String, Object> bookingResult =
          bookingService.bookAppointment(
              userId,
              Integer.parseInt(timeslotId),
              Integer.parseInt(vaccineId),
              personId,
              baseUrl,
              webapp,
              session.getId());

      jsonResponse.putAll(bookingResult);
    } else {
      jsonResponse.put("success", false);
      jsonResponse.put("message", "Fehlende Parameter für die Buchung");
    }
  }
}
