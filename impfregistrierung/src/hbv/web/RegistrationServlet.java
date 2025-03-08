package hbv.web;

import jakarta.servlet.*;
import jakarta.servlet.http.*;
import java.io.*;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.*;
import javax.sql.DataSource;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.charset.StandardCharsets;

public class RegistrationServlet extends HttpServlet {
    private DataSource ds;
    @Override
    public void init() throws ServletException {
        try {
            InitialContext ctx = new InitialContext();
            ds = (DataSource) ctx.lookup("java:/comp/env/jdbc/mariadb");
        } catch(NamingException e){
            throw new ServletException("DataSource nicht gefunden", e);
        }
    }
    
    private String readTemplate(String filename, HttpServletRequest req) throws IOException {
        String realPath = req.getServletContext().getRealPath("/static/" + filename);
        return new String(Files.readAllBytes(Paths.get(realPath)), StandardCharsets.UTF_8);
    }
    
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
      throws ServletException, IOException {
        resp.setContentType("text/html;charset=UTF-8");
        PrintWriter out = resp.getWriter();
        
        // Nicht eingeloggte Navigation
        String nav = "<a href='" + req.getContextPath() + "/register'>Registrieren</a> " +
                     "<a href='" + req.getContextPath() + "/login'>Anmelden</a>";
        String header = readTemplate("header.html", req)
                          .replace("<!-- Title placeholder -->", "Registrierung")
                          .replace("<!--NAVIGATION-->", nav);
        out.println(header);
        
        out.println("<h2>Registrierung</h2>");
        out.println("<form method='post' action='register'>");
        out.println("E-Mail: <input type='email' name='email' required><br>");
        out.println("Vorname: <input type='text' name='vorname' required><br>");
        out.println("Nachname: <input type='text' name='nachname' required><br>");
        out.println("Telefonnummer: <input type='text' name='telefon'><br>");
        out.println("Geburtsdatum: <input type='date' name='geburtsdatum'><br>");
        out.println("Passwort: <input type='password' name='password' required><br>");
        out.println("Rolle: <select name='role'>");
        out.println("<option value='user'>Nutzer</option>");
        out.println("<option value='admin'>Admin</option>");
        out.println("</select><br>");
        out.println("<button type='submit'>Registrieren</button>");
        out.println("</form>");
        
        String footer = readTemplate("footer.html", req);
        out.println(footer);
    }
    
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
      throws ServletException, IOException {
        String email = req.getParameter("email");
        String vorname = req.getParameter("vorname");
        String nachname = req.getParameter("nachname");
        String telefon = req.getParameter("telefon");
        String geburtsdatum = req.getParameter("geburtsdatum");
        String pw = req.getParameter("password");
        String role = req.getParameter("role");
        if(role == null || role.isEmpty()){
            role = "user";
        }
        if(email == null || pw == null || email.isEmpty() || pw.isEmpty()
           || vorname == null || vorname.isEmpty()
           || nachname == null || nachname.isEmpty()){
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "E-Mail, Vorname, Nachname und Passwort erforderlich");
            return;
        }
        String pwHash;
        try {
            pwHash = sha256(pw);
        } catch(NoSuchAlgorithmException e){
            throw new ServletException("Fehler beim Hashen", e);
        }
        try(Connection conn = ds.getConnection()){
            String sql = "INSERT INTO user_account (email, vorname, nachname, telefon, geburtsdatum, password_hash, role, created_at) VALUES (?,?,?,?,?,?,?,NOW())";
            try(PreparedStatement ps = conn.prepareStatement(sql)){
                ps.setString(1, email);
                ps.setString(2, vorname);
                ps.setString(3, nachname);
                ps.setString(4, telefon);
                if(geburtsdatum != null && !geburtsdatum.isEmpty()){
                    ps.setDate(5, java.sql.Date.valueOf(geburtsdatum));
                } else {
                    ps.setNull(5, java.sql.Types.DATE);
                }
                ps.setString(6, pwHash);
                ps.setString(7, role);
                ps.executeUpdate();
            }
        } catch(SQLException e){
            resp.setContentType("text/html;charset=UTF-8");
            PrintWriter out = resp.getWriter();
            String nav = "<a href='" + req.getContextPath() + "/register'>Registrieren</a> " +
                         "<a href='" + req.getContextPath() + "/login'>Anmelden</a>";
            String header = readTemplate("header.html", req)
                              .replace("<!-- Title placeholder -->", "Registrierung - Fehler")
                              .replace("<!--NAVIGATION-->", nav);
            out.println(header);
            if(e.getMessage().contains("Duplicate entry")){
                out.println("<p>Fehler: Diese E-Mail existiert bereits. Bitte verwenden Sie eine andere E-Mail oder melden Sie sich an.</p>");
            } else {
                out.println("<p>Fehler bei Registrierung: " + e.getMessage() + "</p>");
            }
            out.println("<button onclick=\"window.location.href='register'\">Zurück</button>");
            String footer = readTemplate("footer.html", req);
            out.println(footer);
            return;
        }
        resp.setContentType("text/html;charset=UTF-8");
        PrintWriter out = resp.getWriter();
        String nav = "<a href='" + req.getContextPath() + "/login2'>Zum Login</a>";
        String header = readTemplate("header.html", req)
                          .replace("<!-- Title placeholder -->", "Registrierung erfolgreich")
                          .replace("<!--NAVIGATION-->", nav);
        out.println(header);
        out.println("<h2>Registrierung erfolgreich</h2>");
        out.println("<p><a href='login2'>Zum Login</a></p>");
        out.println("<button onclick=\"window.location.href='login2'\">Zurück</button>");
        String footer = readTemplate("footer.html", req);
        out.println(footer);
    }
    
    private String sha256(String raw) throws NoSuchAlgorithmException {
        MessageDigest md = MessageDigest.getInstance("SHA-256");
        byte[] hashed = md.digest(raw.getBytes());
        StringBuilder sb = new StringBuilder();
        for(byte b: hashed){
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }
}
