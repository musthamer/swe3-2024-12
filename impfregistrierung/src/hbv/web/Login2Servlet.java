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

public class Login2Servlet extends HttpServlet {
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
        String email = req.getParameter("user");
        String pw = req.getParameter("passwd");
        if(email == null || pw == null){
            resp.setContentType("text/html;charset=UTF-8");
            PrintWriter out = resp.getWriter();
            String nav = "<a href='" + req.getContextPath() + "/login2'>Login</a>";
            String header = readTemplate("header.html", req)
                              .replace("<!-- Title placeholder -->", "Login2")
                              .replace("<!--NAVIGATION-->", nav);
            out.println(header);
            out.println("<h2>Login2</h2>");
            out.println("<form method='get' action='login2'>");
            out.println("E-Mail: <input type='text' name='user'><br>");
            out.println("Passwort: <input type='password' name='passwd'><br>");
            out.println("<button type='submit'>Login</button>");
            out.println("</form>");
            String footer = readTemplate("footer.html", req);
            out.println(footer);
            return;
        }
        try(Connection conn = ds.getConnection()){
            String sql = "SELECT user_id, password_hash, role, vorname, nachname FROM user_account WHERE email=?";
            try(PreparedStatement ps = conn.prepareStatement(sql)){
                ps.setString(1, email);
                try(ResultSet rs = ps.executeQuery()){
                    if(rs.next()){
                        long userId = rs.getLong("user_id");
                        String storedHash = rs.getString("password_hash");
                        String role = rs.getString("role");
                        String vorname = rs.getString("vorname");
                        String nachname = rs.getString("nachname");
                        String providedHash = sha256(pw);
                        if(storedHash.equals(providedHash)){
                            req.getSession(true).setAttribute("userId", userId);
                            req.getSession().setAttribute("userEmail", email);
                            req.getSession().setAttribute("userRole", role);
                            req.getSession().setAttribute("vorname", vorname);
                            req.getSession().setAttribute("nachname", nachname);
                            resp.sendRedirect("appointments");
                            return;
                        }
                    }
                }
            }
        } catch(SQLException | NoSuchAlgorithmException e){
            throw new ServletException("Fehler beim Login2", e);
        }
        resp.setContentType("text/html;charset=UTF-8");
        PrintWriter out = resp.getWriter();
        String nav = "<a href='" + req.getContextPath() + "/login2'>Login</a>";
        String header = readTemplate("header.html", req)
                          .replace("<!-- Title placeholder -->", "Login2 fehlgeschlagen")
                          .replace("<!--NAVIGATION-->", nav);
        out.println(header);
        out.println("<h2>Login fehlgeschlagen</h2>");
        out.println("<p><a href='login2'>Erneut versuchen</a></p>");
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
