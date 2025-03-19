package hbv.web.servlet;

import hbv.model.Booking;
import hbv.service.BookingService;
import hbv.service.BookingServiceImpl;
import hbv.service.LoggingBookingServiceDecorator;
import hbv.service.DoseService;
import jakarta.servlet.*;
import jakarta.servlet.http.*;
import java.io.*;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

public class CenterDashboardServlet extends HttpServlet {
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
        if(!"center".equalsIgnoreCase(userRole)) {
            if("admin".equalsIgnoreCase(userRole)) {
                resp.sendRedirect("adminDashboard");
            } else {
                resp.sendRedirect("dashboard");
            }
            return;
        }
        String centerName = (String) session.getAttribute("center");
        if(centerName == null) { centerName = ""; }
        String vorname = (String) session.getAttribute("vorname");
        String header = readTemplate("header.html", req).replace("<!-- Title placeholder -->", "Center-Dashboard");
        String footer = readTemplate("footer.html", req);
        resp.setContentType("text/html;charset=UTF-8");
        PrintWriter out = resp.getWriter();
        out.println(header);
        out.println("<h2>Willkommen, " + vorname + " (Impfzentrum-Dashboard)</h2>");
        out.println("<h3>Meine Impfstoffbestände</h3>");
        DoseService doseService = new DoseService();
        try {
            ResultSet rs = doseService.getAllStocksForCenter(centerName);
            out.println("<table>");
            out.println("<tr><th>Impfstoff</th><th>Bestand</th></tr>");
            while(rs.next()){
                out.println("<tr><td>" + rs.getString("vaccine") + "</td><td>" + rs.getInt("stock") + "</td></tr>");
            }
            out.println("</table>");
            rs.getStatement().close();
            rs.close();
        } catch(SQLException e) {
            out.println("<p>Fehler beim Laden der Bestände: " + e.getMessage() + "</p>");
        }
        // Formular zum Hinzufügen neuer Dosen
        out.println("<h3>Neue Dosen hinzufügen</h3>");
        out.println("<form method='post' action='dose'>");
        out.println("<label for='vaccine'>Impfstoff:</label>");
        out.println("<select name='vaccine' id='vaccine'>");
        out.println("<option value='Biontech'>Biontech</option>");
        out.println("<option value='Moderna'>Moderna</option>");
        out.println("</select><br>");
        out.println("<label for='quantity'>Anzahl:</label>");
        out.println("<input type='number' name='quantity' id='quantity' min='1' required><br>");
        out.println("<input type='submit' value='Dosen hinzufügen'>");
        out.println("</form>");
        // Buchungsübersicht
        out.println("<h3>Buchungsübersicht</h3>");
        try {
            BookingService bs = new LoggingBookingServiceDecorator(new BookingServiceImpl());
            List<Booking> bookings = bs.getBookingsForCenter(centerName);
            if(bookings.isEmpty()){
                out.println("<p>Keine Buchungen vorhanden.</p>");
            } else {
                out.println("<table>");
                out.println("<tr><th>Buchungs-ID</th><th>Gebucht am</th></tr>");
                for(Booking b : bookings){
                    out.println("<tr>");
                    out.println("<td>" + b.getBookingId() + "</td>");
                    out.println("<td>" + b.getBookingTime() + "</td>");
                    out.println("</tr>");
                }
                out.println("</table>");
            }
        } catch(SQLException e) {
            out.println("<p>Fehler beim Laden der Buchungen: " + e.getMessage() + "</p>");
        }
        out.println(footer);
    }
}
