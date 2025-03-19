package hbv.web.servlet;

import hbv.service.AppointmentService;
import hbv.service.BookingService;
import hbv.service.BookingServiceImpl;
import hbv.service.LoggingBookingServiceDecorator;
import hbv.model.Appointment;
import hbv.model.Booking;
import jakarta.servlet.*;
import jakarta.servlet.http.*;
import java.io.*;
import java.sql.SQLException;
import java.util.List;

public class DashboardServlet extends HttpServlet {
    private String readTemplate(String filename, HttpServletRequest req) throws IOException {
        BufferedReader reader = new BufferedReader(
            new FileReader(req.getServletContext().getRealPath("/static/" + filename))
        );
        StringBuilder sb = new StringBuilder();
        String line;
        while((line = reader.readLine()) != null) {
            sb.append(line).append(System.lineSeparator());
        }
        reader.close();
        return sb.toString();
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        HttpSession session = req.getSession(false);
        if(session == null || session.getAttribute("userId") == null) {
            resp.sendRedirect("login");
            return;
        }
        String userRole = (String) session.getAttribute("userRole");
        if("admin".equalsIgnoreCase(userRole)) {
            resp.sendRedirect("adminDashboard");
            return;
        } else if("center".equalsIgnoreCase(userRole)) {
            resp.sendRedirect("centerDashboard");
            return;
        }
        resp.setContentType("text/html;charset=UTF-8");
        PrintWriter out = resp.getWriter();
        String vorname = (String) session.getAttribute("vorname");
        String nachname = (String) session.getAttribute("nachname");
        String header = readTemplate("header.html", req).replace("<!-- Title placeholder -->", "User-Dashboard");
        String footer = readTemplate("footer.html", req);
        out.println(header);
        out.println("<h2>Willkommen, " + vorname + " " + nachname + " (Nutzer-Dashboard)</h2>");
        String filterVaccine = req.getParameter("vaccine_filter");
        String filterCenter = req.getParameter("center_filter");
        out.println("<form method='get' action='dashboard'>");
        out.println("Impfstoff: <select name='vaccine_filter'>");
        out.println("<option value=''>Alle</option>");
        out.println("<option value='Biontech' " + ("Biontech".equals(filterVaccine) ? "selected" : "") + ">Biontech</option>");
        out.println("<option value='Moderna' " + ("Moderna".equals(filterVaccine) ? "selected" : "") + ">Moderna</option>");
        out.println("</select> ");
        out.println("Impfzentrum: <select name='center_filter'>");
        out.println("<option value=''>Alle</option>");
        for(int i=1; i<=30; i++){
            String centerOption = "Zentrum " + i;
            boolean isSelected = (centerOption.equals(filterCenter));
            out.println("<option value='"+centerOption+"' " + (isSelected ? "selected" : "") + ">"+centerOption+"</option>");
        }
        out.println("</select> ");
        out.println("<input type='submit' value='Filtern'>");
        out.println("</form>");
        AppointmentService apServ = new AppointmentService();
        try {
            List<Appointment> appts = apServ.getFilteredAppointments(filterVaccine, filterCenter);
            if(appts.isEmpty()){
                out.println("<p>Keine Termine verfügbar.</p>");
            } else {
                out.println("<table>");
                out.println("<tr><th>ID</th><th>Datum</th><th>Zeit</th><th>Impfstoff</th><th>Standort</th><th>Anbieter</th><th>Buchen</th></tr>");
                for(Appointment ap : appts){
                    out.println("<tr>");
                    out.println("<td>"+ ap.getAppointmentId() +"</td>");
                    out.println("<td>"+ ap.getDateSlot() +"</td>");
                    out.println("<td>"+ ap.getTimeSlot() +"</td>");
                    out.println("<td>"+ ap.getVaccine() +"</td>");
                    out.println("<td>"+ ap.getLocation() +"</td>");
                    out.println("<td>"+ ap.getProvider() +"</td>");
                    out.println("<td>");
                    out.println("<form method='post' action='appointments'>");
                    out.println("<input type='hidden' name='appointment_id' value='"+ ap.getAppointmentId() +"'>");
                    out.println("Name: <input type='text' name='booking_name'>");
                    out.println("<input type='submit' value='Buchen'>");
                    out.println("</form>");
                    out.println("</td>");
                    out.println("</tr>");
                }
                out.println("</table>");
            }
        } catch (SQLException e) {
            out.println("<p>Fehler beim Laden der Termine: "+ e.getMessage() +"</p>");
        }
        out.println("<h3>Meine Buchungen</h3>");
        try {
            BookingService bs = new LoggingBookingServiceDecorator(new BookingServiceImpl());
            Integer userId = (Integer) session.getAttribute("userId");
            java.util.List<Booking> bookings = bs.getBookingsForUser(userId);
            if(bookings.isEmpty()){
                out.println("<p>Keine Buchungen vorhanden.</p>");
            } else {
                out.println("<table>");
                out.println("<tr><th>Buchungs-ID</th><th>Gebucht am</th><th>Aktion</th></tr>");
                for(Booking b : bookings){
                    out.println("<tr>");
                    out.println("<td>"+ b.getBookingId() +"</td>");
                    out.println("<td>"+ b.getBookingTime() +"</td>");
                    out.println("<td>");
                    out.println("<form method='post' action='cancel'>");
                    out.println("<input type='hidden' name='booking_id' value='"+ b.getBookingId() +"'>");
                    out.println("<input type='submit' value='Stornieren'>");
                    out.println("</form>");
                    out.println("</td>");
                    out.println("</tr>");
                }
                out.println("</table>");
            }
        } catch (SQLException e) {
            out.println("<p>Fehler beim Laden der Buchungen: "+ e.getMessage() +"</p>");
        }
        out.println(footer);
    }
}
