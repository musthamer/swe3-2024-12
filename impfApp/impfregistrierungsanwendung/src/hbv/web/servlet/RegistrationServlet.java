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
        resp.setContentType("text/html;charset=UTF-8");
        PrintWriter out = resp.getWriter();
        String header = readTemplate("header.html", req).replace("<!-- Title placeholder -->", "Registrierung");
        String footer = readTemplate("footer.html", req);

        out.println(header);
        out.println("<h2>Registrierung</h2>");
        out.println("<form class='form-control' method='post' action='register'>");
        out.println("<label for='email'>E-Mail:</label>");
        out.println("<input type='email' name='email' id='email' required>");
        out.println("<label for='vorname'>Vorname:</label>");
        out.println("<input type='text' name='vorname' id='vorname' required>");
        out.println("<label for='nachname'>Nachname:</label>");
        out.println("<input type='text' name='nachname' id='nachname' required>");
        out.println("<label for='telefon'>Telefon:</label>");
        out.println("<input type='text' name='telefon' id='telefon'>");
        out.println("<label for='geburtsdatum'>Geburtsdatum:</label>");
        out.println("<input type='date' name='geburtsdatum' id='geburtsdatum'>");
        out.println("<label for='password'>Passwort:</label>");
        out.println("<input type='password' name='password' id='password' required>");
        out.println("<label for='passwordConfirm'>Passwort bestätigen:</label>");
        out.println("<input type='password' name='passwordConfirm' id='passwordConfirm' required>");
        out.println("<label for='role'>Rolle:</label>");
        out.println("<select name='role' id='role'>");
        out.println("  <option value='user'>Nutzer</option>");
        out.println("  <option value='admin'>Admin</option>");
        // Impfzentren dürfen sich nicht über dieses Formular registrieren.
        out.println("</select>");
        out.println("<input type='submit' value='Registrieren'>");
        out.println("</form>");
        out.println(footer);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
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

        out.println(header);
        if(email == null || password == null || passwordConfirm == null ||
           vorname == null || nachname == null || !password.equals(passwordConfirm)) {
            out.println("<h2>Registrierung fehlgeschlagen</h2>");
            out.println("<p>Ungültige Eingaben oder Passwörter stimmen nicht überein.</p>");
            out.println("<p><a href='register'>Zurück</a></p>");
            out.println(footer);
            return;
        }
        // Verhindere Registrierung als Impfzentrum über diese Seite.
        if("center".equalsIgnoreCase(role)) {
            out.println("<h2>Registrierung fehlgeschlagen</h2>");
            out.println("<p>Impfzentren dürfen sich nicht über diese Seite registrieren.</p>");
            out.println("<p><a href='register'>Zurück</a></p>");
            out.println(footer);
            return;
        }

        try {
            boolean success = userService.registerUser(email, password, vorname, nachname, role, "");
            if(success) {
                out.println("<h2>Registrierung erfolgreich</h2>");
                out.println("<p>Bitte <a href='login'>melden Sie sich an</a>.</p>");
            } else {
                out.println("<h2>Registrierung fehlgeschlagen</h2>");
                out.println("<p>E-Mail bereits registriert.</p>");
                out.println("<p><a href='register'>Zurück</a></p>");
            }
        } catch (SQLException e) {
            out.println("<h2>Registrierung fehlgeschlagen</h2>");
            out.println("<p>Fehler: " + e.getMessage() + "</p>");
            out.println("<p><a href='register'>Zurück</a></p>");
        }
        out.println(footer);
    }
}
