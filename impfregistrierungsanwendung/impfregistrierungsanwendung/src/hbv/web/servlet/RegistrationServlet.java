package hbv.web.servlet;
import hbv.service.IUserService;
import hbv.service.LoggingUserServiceDecorator;
import hbv.service.UserService;
import jakarta.servlet.*;
import jakarta.servlet.http.*;
import java.io.*;
import java.sql.SQLException;
public class RegistrationServlet extends HttpServlet {
    private IUserService userService;
    @Override
    public void init() throws ServletException {
        userService = new LoggingUserServiceDecorator(new UserService());
    }
    private String readTemplate(String filename, HttpServletRequest req) throws IOException {
        BufferedReader reader = new BufferedReader(new FileReader(req.getServletContext().getRealPath("/static/" + filename)));
        StringBuilder sb = new StringBuilder();
        String line;
        while((line = reader.readLine()) != null) { sb.append(line).append(System.lineSeparator()); }
        reader.close();
        return sb.toString();
    }
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        resp.setContentType("text/html;charset=UTF-8");
        PrintWriter out = resp.getWriter();
        // Ersetzen des Title-Platzhalters (z.B. "Registrierung")
        String header = readTemplate("header.html", req).replace("<!-- Title placeholder -->", "Registrierung");
        String footer = readTemplate("footer.html", req);
        out.println(header);
        out.println("<h2>Registrierung</h2>");
        out.println("<form method='post' action='register'>");
        out.println("E-Mail: <input type='email' name='email' required><br>");
        out.println("Vorname: <input type='text' name='vorname' required><br>");
        out.println("Nachname: <input type='text' name='nachname' required><br>");
        out.println("Telefon: <input type='text' name='telefon'><br>");
        out.println("Geburtsdatum: <input type='date' name='geburtsdatum'><br>");
        out.println("Passwort: <input type='password' name='password' required><br>");
        out.println("Passwort bestätigen: <input type='password' name='passwordConfirm' required><br>");
        out.println("Rolle: <select name='role'>");
        out.println("<option value='user'>Nutzer</option>");
        out.println("<option value='admin'>Admin</option>");
        out.println("<option value='center'>Impfzentrum</option>");
        out.println("</select><br>");
        out.println("<div id='regcode' style='display:none;'>");
        out.println("Registrierungscode: <input type='text' name='regcode'><br>");
        out.println("</div>");
        out.println("<input type='submit' value='Registrieren'>");
        out.println("</form>");
        out.println(footer);
    }
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        resp.setContentType("text/html;charset=UTF-8");
        PrintWriter out = resp.getWriter();
        String header = readTemplate("header.html", req).replace("<!-- Title placeholder -->", "Registrierung");
        String footer = readTemplate("footer.html", req);
        String email = req.getParameter("email");
        String password = req.getParameter("password");
        String passwordConfirm = req.getParameter("passwordConfirm");
        String vorname = req.getParameter("vorname");
        String nachname = req.getParameter("nachname");
        String role = req.getParameter("role");
        String regcode = req.getParameter("regcode");
        if(email == null || password == null || passwordConfirm == null || vorname == null || nachname == null || !password.equals(passwordConfirm)) {
            out.println(header);
            out.println("<h2>Registrierung fehlgeschlagen</h2>");
            out.println("<p>Ungültige Eingaben oder Passwörter stimmen nicht überein.</p>");
            out.println("<p><a href='register'>Zurück</a></p>");
            out.println(footer);
            return;
        }
        try {
            boolean success = userService.registerUser(email, password, vorname, nachname, role, (regcode == null ? "" : regcode));
            if(success) {
                out.println(header);
                out.println("<h2>Registrierung erfolgreich</h2>");
                out.println("<p>Bitte <a href='login'>melden Sie sich an</a>.</p>");
                out.println(footer);
            } else {
                out.println(header);
                out.println("<h2>Registrierung fehlgeschlagen</h2>");
                out.println("<p>Entweder ist die E-Mail bereits registriert oder der Registrierungscode war falsch.</p>");
                out.println("<p><a href='register'>Zurück</a></p>");
                out.println(footer);
            }
        } catch (SQLException e) {
            out.println(header);
            out.println("<h2>Registrierung fehlgeschlagen</h2>");
            out.println("<p>Fehler: " + e.getMessage() + "</p>");
            out.println("<p><a href='register'>Zurück</a></p>");
            out.println(footer);
        }
    }
}
