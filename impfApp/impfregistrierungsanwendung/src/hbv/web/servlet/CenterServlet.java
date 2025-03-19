package hbv.web.servlet;

import hbv.service.CenterService;
import jakarta.servlet.*;
import jakarta.servlet.http.*;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.SQLException;

public class CenterServlet extends HttpServlet {
    private CenterService centerService;

    @Override
    public void init() throws ServletException {
        centerService = new CenterService();
    }

    private String readTemplate(String filename, HttpServletRequest req) throws IOException {
        java.io.BufferedReader reader = new java.io.BufferedReader(
            new java.io.FileReader(req.getServletContext().getRealPath("/static/" + filename))
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
        if(session == null || !"admin".equalsIgnoreCase((String)session.getAttribute("userRole"))) {
            resp.sendRedirect("login");
            return;
        }
        resp.setContentType("text/html;charset=UTF-8");
        PrintWriter out = resp.getWriter();
        String header = readTemplate("header.html", req).replace("<!-- Title placeholder -->", "Neues Impfzentrum anlegen");
        String footer = readTemplate("footer.html", req);
        out.println(header);
        out.println("<h2>Neues Impfzentrum anlegen</h2>");
        out.println("<form method='post' action='center'>");
        out.println("<label for='centerName'>Name des Impfzentrums:</label>");
        out.println("<input type='text' name='centerName' id='centerName' required><br>");
        out.println("<label for='centerEmail'>E-Mail des Impfzentrums:</label>");
        out.println("<input type='email' name='centerEmail' id='centerEmail' required><br>");
        out.println("<label for='centerPassword'>Passwort für Impfzentrum:</label>");
        out.println("<input type='password' name='centerPassword' id='centerPassword' required><br>");
        out.println("<input type='submit' value='Impfzentrum anlegen'>");
        out.println("</form>");
        out.println(footer);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        HttpSession session = req.getSession(false);
        if(session == null || !"admin".equalsIgnoreCase((String)session.getAttribute("userRole"))) {
            resp.sendRedirect("login");
            return;
        }
        String centerName = req.getParameter("centerName");
        String centerEmail = req.getParameter("centerEmail");
        String centerPassword = req.getParameter("centerPassword");
        resp.setContentType("text/html;charset=UTF-8");
        PrintWriter out = resp.getWriter();
        if(centerName == null || centerEmail == null || centerPassword == null) {
            out.println("<html><body>");
            out.println("<h2>Fehlende Angaben.</h2>");
            out.println("<p><a href='center'>Zurück</a></p>");
            out.println("</body></html>");
            return;
        }
        try {
            centerService.createCenter(centerName, centerEmail, centerPassword);
            out.println("<html><body>");
            out.println("<h2>Impfzentrum erfolgreich angelegt.</h2>");
            out.println("<p>Das Impfzentrum kann sich nun mit folgenden Credentials anmelden:</p>");
            out.println("<p>E-Mail: " + centerEmail + "<br>Passwort: " + centerPassword + "</p>");
            out.println("<p><a href='adminDashboard'>Zurück zum Admin-Dashboard</a></p>");
            out.println("</body></html>");
        } catch (SQLException e) {
            out.println("<html><body>");
            out.println("<h2>Fehler beim Anlegen des Impfzentrums: " + e.getMessage() + "</h2>");
            out.println("<p><a href='center'>Zurück</a></p>");
            out.println("</body></html>");
        }
    }
}
