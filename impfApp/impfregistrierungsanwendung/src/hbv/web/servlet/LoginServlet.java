package hbv.web.servlet;

import hbv.model.User;
import hbv.service.IUserService;
import hbv.service.LoggingUserServiceDecorator;
import hbv.service.UserService;
import jakarta.servlet.*;
import jakarta.servlet.http.*;
import java.io.*;
import java.sql.SQLException;

public class LoginServlet extends HttpServlet {
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
        String header = readTemplate("header.html", req).replace("<!-- Title placeholder -->", "Login");
        String footer = readTemplate("footer.html", req);

        out.println(header);
        out.println("<h2>Login</h2>");
        out.println("<form class='form-control' method='post' action='login'>");
        out.println("<label for='email'>E-Mail:</label>");
        out.println("<input type='text' name='email' id='email' required>");
        out.println("<label for='password'>Passwort:</label>");
        out.println("<input type='password' name='password' id='password' required>");
        out.println("<input type='submit' value='Anmelden'>");
        out.println("</form>");
        out.println(footer);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        String email = req.getParameter("email");
        String password = req.getParameter("password");
        resp.setContentType("text/html;charset=UTF-8");
        PrintWriter out = resp.getWriter();
        String header = readTemplate("header.html", req).replace("<!-- Title placeholder -->", "Login");
        String footer = readTemplate("footer.html", req);

        try {
            boolean authenticated = userService.authenticateUser(email, password);
            if(authenticated) {
                User user = userService.getUser(email);
                HttpSession session = req.getSession(true);
                session.setAttribute("userId", user.getId());
                session.setAttribute("userEmail", user.getEmail());
                session.setAttribute("userRole", user.getRole());
                session.setAttribute("vorname", user.getVorname());
                session.setAttribute("nachname", user.getNachname());
                session.setAttribute("center", user.getAssignedCenter());
                if(user.getRole().equalsIgnoreCase("admin")) {
                    resp.sendRedirect("adminDashboard");
                } else if(user.getRole().equalsIgnoreCase("center")) {
                    resp.sendRedirect("centerDashboard");
                } else {
                    resp.sendRedirect("dashboard");
                }
            } else {
                out.println(header);
                out.println("<h2>Login fehlgeschlagen</h2>");
                out.println("<p>Ungültige E-Mail oder Passwort.</p>");
                out.println("<p><a href='login'>Zurück</a></p>");
                out.println(footer);
            }
        } catch (SQLException e) {
            out.println(header);
            out.println("<h2>Login fehlgeschlagen</h2>");
            out.println("<p>Fehler: " + e.getMessage() + "</p>");
            out.println("<p><a href='login'>Zurück</a></p>");
            out.println(footer);
        }
    }
}
