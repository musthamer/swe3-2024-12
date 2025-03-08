package hbv.web;

import jakarta.servlet.*;
import jakarta.servlet.http.*;
import java.io.*;
import java.sql.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.charset.StandardCharsets;

public class AppointmentServlet extends HttpServlet {
    // Prüft, ob der Nutzer bereits einen Termin gebucht hat
    private boolean hasExistingBooking(Connection conn, int userId) throws SQLException {
        PreparedStatement ps = conn.prepareStatement("SELECT COUNT(*) AS count FROM booking WHERE user_id=?");
        ps.setInt(1, userId);
        ResultSet rs = ps.executeQuery();
        int count = 0;
        if(rs.next()){
            count = rs.getInt("count");
        }
        rs.close();
        ps.close();
        return count > 0;
    }
    
    private String readTemplate(String filename, HttpServletRequest req) throws IOException {
        String realPath = req.getServletContext().getRealPath("/static/" + filename);
        return new String(Files.readAllBytes(Paths.get(realPath)), StandardCharsets.UTF_8);
    }
    
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
      throws ServletException, IOException {
        HttpSession session = req.getSession(false);
        if(session == null || session.getAttribute("userId") == null) {
            resp.sendRedirect("login");
            return;
        }
        int userId = ((Number) session.getAttribute("userId")).intValue();
        // Begrüßung jetzt mit Vor- und Nachname
        String vorname = (String) session.getAttribute("vorname");
        String nachname = (String) session.getAttribute("nachname");
        
        resp.setContentType("text/html;charset=UTF-8");
        PrintWriter out = resp.getWriter();
        
        // Dashboard-Navigation: keine Links zu Registrieren/Anmelden, stattdessen Dashboard und Logout
        String nav = "<a href='" + req.getContextPath() + "/appointments'>Dashboard</a> " +
                     "<a href='" + req.getContextPath() + "/logout'>Logout</a>";
        String header = readTemplate("header.html", req)
                          .replace("<!-- Title placeholder -->", "Termine")
                          .replace("<!--NAVIGATION-->", nav);
        out.println(header);
        
        out.println("<h2>Guten Tag, " + vorname + " " + nachname + "!</h2>");
        String userRole = (String) session.getAttribute("userRole");
        if("admin".equalsIgnoreCase(userRole)){
            out.println("<h3>Admin-Zusatzfunktionen: [Admin-Dashboard, erweiterte Terminverwaltung etc.]</h3>");
        }
        
        // Impfstoff- und Standortfilter: Dropdowns
        String vaccineFilter = req.getParameter("vaccine_filter");
        String locationFilter = req.getParameter("location_filter");
        out.println("<form method='get' action='appointments'>");
        out.println("Filter nach Impfstoff: <select name='vaccine_filter'>");
        PreparedStatement psVaccine = null;
        PreparedStatement psLocation = null;
        try (Connection conn = hbv.service.Database.getConnection()) {
            psVaccine = conn.prepareStatement("SELECT DISTINCT vaccine FROM appointment");
            ResultSet rsVaccine = psVaccine.executeQuery();
            while(rsVaccine.next()){
                String vac = rsVaccine.getString("vaccine");
                if(vaccineFilter != null && vaccineFilter.equals(vac)){
                     out.println("<option value='" + vac + "' selected>" + vac + "</option>");
                } else {
                     out.println("<option value='" + vac + "'>" + vac + "</option>");
                }
            }
            rsVaccine.close();
            
            out.println("</select>");
            out.println(" Filter nach Standort: <select name='location_filter'>");
            psLocation = conn.prepareStatement("SELECT DISTINCT location FROM appointment");
            ResultSet rsLocation = psLocation.executeQuery();
            while(rsLocation.next()){
                String loc = rsLocation.getString("location");
                if(locationFilter != null && locationFilter.equals(loc)){
                     out.println("<option value='" + loc + "' selected>" + loc + "</option>");
                } else {
                     out.println("<option value='" + loc + "'>" + loc + "</option>");
                }
            }
            rsLocation.close();
            out.println("</select>");
            out.println("<input type='submit' value='Filtern'/>");
            out.println("</form>");
            
            // Termine abfragen
            PreparedStatement ps;
            if(vaccineFilter != null && !vaccineFilter.isEmpty() && locationFilter != null && !locationFilter.isEmpty()){
                ps = conn.prepareStatement("SELECT appointment_id, date_slot, time_slot, vaccine, remaining_capacity, location, provider FROM appointment WHERE vaccine=? AND location=?");
                ps.setString(1, vaccineFilter);
                ps.setString(2, locationFilter);
            } else if(vaccineFilter != null && !vaccineFilter.isEmpty()){
                ps = conn.prepareStatement("SELECT appointment_id, date_slot, time_slot, vaccine, remaining_capacity, location, provider FROM appointment WHERE vaccine=?");
                ps.setString(1, vaccineFilter);
            } else if(locationFilter != null && !locationFilter.isEmpty()){
                ps = conn.prepareStatement("SELECT appointment_id, date_slot, time_slot, vaccine, remaining_capacity, location, provider FROM appointment WHERE location=?");
                ps.setString(1, locationFilter);
            } else {
                ps = conn.prepareStatement("SELECT appointment_id, date_slot, time_slot, vaccine, remaining_capacity, location, provider FROM appointment");
            }
            ResultSet rs = ps.executeQuery();
            out.println("<h2>Verfügbare Termine</h2>");
            out.println("<table border='1'><tr><th>ID</th><th>Datum</th><th>Zeit</th><th>Vaccine</th><th>Standort</th><th>Anbieter</th><th>Verfügbar</th><th>Aktion</th></tr>");
            boolean alreadyBooked = hasExistingBooking(conn, userId);
            while(rs.next()){
                int id = rs.getInt("appointment_id");
                Date date = rs.getDate("date_slot");
                String time = rs.getString("time_slot");
                String vaccine = rs.getString("vaccine");
                String location = rs.getString("location");
                String provider = rs.getString("provider");
                int remaining = rs.getInt("remaining_capacity");
                out.println("<tr>");
                out.println("<td>" + id + "</td><td>" + date + "</td><td>" + time + "</td><td>" + vaccine + "</td><td>" + location + "</td><td>" + provider + "</td><td>" + remaining + "</td>");
                if(remaining > 0 && !alreadyBooked){
                    out.println("<td><form method='post' action='appointments'>");
                    out.println("<input type='hidden' name='appointment_id' value='" + id + "'/>");
                    out.println("Für: <input type='text' name='booking_name' placeholder='Optional'/> ");
                    out.println("<input type='submit' value='Buchen'/>");
                    out.println("</form></td>");
                } else {
                    out.println("<td>" + (alreadyBooked ? "Bereits gebucht" : "Ausgebucht") + "</td>");
                }
                out.println("</tr>");
            }
            out.println("</table>");
            ps.close();
            
            // Eigene Buchungen anzeigen
            PreparedStatement psUser = conn.prepareStatement(
              "SELECT b.booking_id, a.date_slot, a.time_slot, a.vaccine, a.location, a.provider, b.booking_name FROM booking b JOIN appointment a ON b.appointment_id = a.appointment_id WHERE b.user_id=?");
            psUser.setInt(1, userId);
            ResultSet rsUser = psUser.executeQuery();
            out.println("<h2>Deine gebuchten Termine</h2>");
            out.println("<table border='1'><tr><th>ID</th><th>Datum</th><th>Zeit</th><th>Vaccine</th><th>Standort</th><th>Anbieter</th><th>Name</th><th>Aktion</th></tr>");
            while(rsUser.next()){
                int bid = rsUser.getInt("booking_id");
                Date date = rsUser.getDate("date_slot");
                String time = rsUser.getString("time_slot");
                String vaccine = rsUser.getString("vaccine");
                String location = rsUser.getString("location");
                String provider = rsUser.getString("provider");
                String bookingName = rsUser.getString("booking_name");
                out.println("<tr>");
                out.println("<td>" + bid + "</td><td>" + date + "</td><td>" + time + "</td><td>" + vaccine + "</td><td>" + location + "</td><td>" + provider + "</td><td>" + (bookingName != null ? bookingName : (vorname + " " + nachname)) + "</td>");
                out.println("<td><form method='post' action='cancel'>");
                out.println("<input type='hidden' name='booking_id' value='" + bid + "'/>");
                out.println("<input type='submit' value='Stornieren'/>");
                out.println("</form></td>");
                out.println("</tr>");
            }
            out.println("</table>");
            psUser.close();
            
            if(alreadyBooked){
                out.println("<p>Fehler: Sie haben bereits einen Termin gebucht.</p>");
            }
            
            if("admin".equalsIgnoreCase(userRole)){
                out.println("<h2>Alle Buchungen (Admin-Dashboard)</h2>");
                Statement stmtAll = conn.createStatement();
                ResultSet rsAll = stmtAll.executeQuery("SELECT b.booking_id, ua.email, a.date_slot, a.time_slot, a.vaccine, a.location, a.provider FROM booking b JOIN user_account ua ON b.user_id = ua.user_id JOIN appointment a ON b.appointment_id = a.appointment_id");
                out.println("<table border='1'><tr><th>ID</th><th>Nutzer</th><th>Datum</th><th>Zeit</th><th>Vaccine</th><th>Standort</th><th>Anbieter</th></tr>");
                while(rsAll.next()){
                    int bid = rsAll.getInt("booking_id");
                    String emailAll = rsAll.getString("email");
                    Date date = rsAll.getDate("date_slot");
                    String time = rsAll.getString("time_slot");
                    String vaccine = rsAll.getString("vaccine");
                    String location = rsAll.getString("location");
                    String provider = rsAll.getString("provider");
                    out.println("<tr>");
                    out.println("<td>" + bid + "</td><td>" + emailAll + "</td><td>" + date + "</td><td>" + time + "</td><td>" + vaccine + "</td><td>" + location + "</td><td>" + provider + "</td>");
                    out.println("</tr>");
                }
                out.println("</table>");
                stmtAll.close();
            }
            
            out.println("<button onclick=\"window.location.href='appointments'\">Zurück</button>");
            out.println("<p><a href='logout'>Logout</a></p>");
        } catch(SQLException e) {
            out.println("<p>Fehler: " + e.getMessage() + "</p>");
            out.println("<button onclick=\"window.location.href='appointments'\">Zurück</button>");
        }
        
        String footer = readTemplate("footer.html", req);
        out.println(footer);
    }
    
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
      throws ServletException, IOException {
        int appointmentId = Integer.parseInt(req.getParameter("appointment_id"));
        int userId = ((Number) req.getSession(false).getAttribute("userId")).intValue();
        String bookingName = req.getParameter("booking_name");
        try (Connection conn = hbv.service.Database.getConnection()) {
            PreparedStatement psUserCheck = conn.prepareStatement("SELECT COUNT(*) AS count FROM booking WHERE user_id=?");
            psUserCheck.setInt(1, userId);
            ResultSet rsUserCheck = psUserCheck.executeQuery();
            if(rsUserCheck.next()){
                int count = rsUserCheck.getInt("count");
                if(count > 0){
                    resp.setContentType("text/html;charset=UTF-8");
                    PrintWriter out = resp.getWriter();
                    String nav = "<a href='" + req.getContextPath() + "/appointments'>Dashboard</a> " +
                                 "<a href='" + req.getContextPath() + "/logout'>Logout</a>";
                    String header = readTemplate("header.html", req).replace("<!-- Title placeholder -->", "Buchungsfehler").replace("<!--NAVIGATION-->", nav);
                    out.println(header);
                    out.println("<p>Fehler: Sie haben bereits einen Termin gebucht.</p>");
                    out.println("<button onclick=\"window.location.href='appointments'\">Zurück</button>");
                    String footer = readTemplate("footer.html", req);
                    out.println(footer);
                    return;
                }
            }
            PreparedStatement psCheck = conn.prepareStatement("SELECT remaining_capacity FROM appointment WHERE appointment_id=?");
            psCheck.setInt(1, appointmentId);
            ResultSet rs = psCheck.executeQuery();
            if(rs.next()){
                int remaining = rs.getInt("remaining_capacity");
                if(remaining > 0){
                    PreparedStatement psInsert = conn.prepareStatement("INSERT INTO booking (user_id, appointment_id, booking_name) VALUES (?,?,?)", Statement.RETURN_GENERATED_KEYS);
                    psInsert.setInt(1, userId);
                    psInsert.setInt(2, appointmentId);
                    psInsert.setString(3, (bookingName != null && !bookingName.trim().isEmpty()) ? bookingName : null);
                    psInsert.executeUpdate();
                    ResultSet generatedKeys = psInsert.getGeneratedKeys();
                    int bookingId = -1;
                    if(generatedKeys.next()){
                        bookingId = generatedKeys.getInt(1);
                    }
                    PreparedStatement psUpdate = conn.prepareStatement("UPDATE appointment SET remaining_capacity = remaining_capacity - 1 WHERE appointment_id=?");
                    psUpdate.setInt(1, appointmentId);
                    psUpdate.executeUpdate();
                    resp.setContentType("text/html;charset=UTF-8");
                    PrintWriter out = resp.getWriter();
                    String nav = "<a href='" + req.getContextPath() + "/appointments'>Dashboard</a> " +
                                 "<a href='" + req.getContextPath() + "/logout'>Logout</a>";
                    String header = readTemplate("header.html", req).replace("<!-- Title placeholder -->", "Buchung erfolgreich").replace("<!--NAVIGATION-->", nav);
                    out.println(header);
                    out.println("<p>Termin erfolgreich gebucht.</p>");
                    out.println("<p><a href='confirmation?booking_id=" + bookingId + "' target='_blank'>Bestätigung (PDF) anzeigen</a></p>");
                    out.println("<button onclick=\"window.location.href='appointments'\">Zurück</button>");
                    String footer = readTemplate("footer.html", req);
                    out.println(footer);
                    return;
                } else {
                    resp.setContentType("text/html;charset=UTF-8");
                    PrintWriter out = resp.getWriter();
                    String nav = "<a href='" + req.getContextPath() + "/appointments'>Dashboard</a> " +
                                 "<a href='" + req.getContextPath() + "/logout'>Logout</a>";
                    String header = readTemplate("header.html", req).replace("<!-- Title placeholder -->", "Buchungsfehler").replace("<!--NAVIGATION-->", nav);
                    out.println(header);
                    out.println("<p>Fehler: Termin ausgebucht.</p>");
                    out.println("<button onclick=\"window.location.href='appointments'\">Zurück</button>");
                    String footer = readTemplate("footer.html", req);
                    out.println(footer);
                    return;
                }
            }
        } catch(SQLException e) {
            resp.setContentType("text/html;charset=UTF-8");
            PrintWriter out = resp.getWriter();
            String nav = "<a href='" + req.getContextPath() + "/appointments'>Dashboard</a> " +
                         "<a href='" + req.getContextPath() + "/logout'>Logout</a>";
            String header = readTemplate("header.html", req).replace("<!-- Title placeholder -->", "Buchungsfehler").replace("<!--NAVIGATION-->", nav);
            out.println(header);
            out.println("<p>Fehler: " + e.getMessage() + "</p>");
            out.println("<button onclick=\"window.location.href='appointments'\">Zurück</button>");
            String footer = readTemplate("footer.html", req);
            out.println(footer);
        }
    }
}
