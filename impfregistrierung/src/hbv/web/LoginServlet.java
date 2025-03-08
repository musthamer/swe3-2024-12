package hbv.web;

import jakarta.servlet.*;
import jakarta.servlet.http.*;
import java.io.*;
import java.sql.*;
import javax.sql.DataSource;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class LoginServlet extends HttpServlet {
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
        
        String nav = "<a href='" + req.getContextPath() + "/register'>Registrieren</a> " +
                     "<a href='" + req.getContextPath() + "/login'>Anmelden</a>";
        String header = readTemplate("header.html", req)
                          .replace("<!-- Title placeholder -->", "Login")
                          .replace("<!--NAVIGATION-->", nav);
        out.println(header);
        
        out.println("<h2>Login</h2>");
        out.println("<form method='post' action='login'>");
        out.println("E-Mail: <input type='text' name='email'/><br>");
        out.println("Passwort: <input type='password' name='password'/><br>");
        out.println("<input type='submit' value='Anmelden'/>");
        out.println("</form>");
        out.println("<p><a href='register'>Registrieren</a></p>");
        
        String footer = readTemplate("footer.html", req);
        out.println(footer);
    }
    
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
      throws ServletException, IOException {
        String email = req.getParameter("email");
        String password = req.getParameter("password");
        try(Connection conn = ds.getConnection()){
            // Abfrage inklusive vorname und nachname
            PreparedStatement ps = conn.prepareStatement("SELECT user_id, password_hash, role, vorname, nachname FROM user_account WHERE email=?");
            ps.setString(1, email);
            ResultSet rs = ps.executeQuery();
            if(rs.next()){
                int userId = rs.getInt("user_id");
                String storedHash = rs.getString("password_hash");
                String role = rs.getString("role");
                String vorname = rs.getString("vorname");
                String nachname = rs.getString("nachname");
                String providedHash = sha256(password);
                if(storedHash.equals(providedHash)){
                    HttpSession session = req.getSession(true);
                    session.setAttribute("userId", userId);
                    session.setAttribute("userEmail", email);
                    session.setAttribute("userRole", role);
                    session.setAttribute("vorname", vorname);
                    session.setAttribute("nachname", nachname);
                    resp.sendRedirect("appointments");
                    return;
                }
            }
        } catch(SQLException | NoSuchAlgorithmException e){
            throw new ServletException("Fehler bei Login", e);
        }
        resp.setContentType("text/html;charset=UTF-8");
        PrintWriter out = resp.getWriter();
        String nav = "<a href='" + req.getContextPath() + "/register'>Registrieren</a> " +
                     "<a href='" + req.getContextPath() + "/login'>Anmelden</a>";
        String header = readTemplate("header.html", req)
                          .replace("<!-- Title placeholder -->", "Login fehlgeschlagen")
                          .replace("<!--NAVIGATION-->", nav);
        out.println(header);
        out.println("<h2>Anmeldung fehlgeschlagen</h2>");
        out.println("<p><a href='login'>Nochmal versuchen</a></p>");
        String footer = readTemplate("footer.html", req);
        out.println(footer);
    }
    
    private String sha256(String raw) throws NoSuchAlgorithmException {
        MessageDigest md = MessageDigest.getInstance("SHA-256");
        byte[] bytes = md.digest(raw.getBytes());
        StringBuilder sb = new StringBuilder();
        for(byte b: bytes){
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }
}
