package hbv.web;

import jakarta.servlet.*;
import jakarta.servlet.http.*;
import java.io.*;
import java.sql.*;
import javax.sql.*;
import javax.naming.*;
import java.security.SecureRandom;
import java.util.*;

import hbv.messaging.EmailMessageFactory;
import hbv.messaging.EmailService;
import redis.clients.jedis.Jedis;

public class ForgotPasswordServlet extends HttpServlet {

  @Override
  protected void doGet(HttpServletRequest request, HttpServletResponse response)
      throws IOException, ServletException {
    // Formular zum Anfordern eines Reset-Links anzeigen
    response.setContentType("text/html");
    PrintWriter out = response.getWriter();
    out.println("<!DOCTYPE html>");
    out.println("<html><head><title>Passwort vergessen</title></head><body>");
    out.println("<h2>Passwort zurücksetzen</h2>");
    out.println("<p>Bitte geben Sie Ihre E-Mail-Adresse ein. Wir senden Ihnen einen Link zum Zurücksetzen Ihres Passworts.</p>");
    out.println("<form method='POST'>");
    out.println("E-Mail: <input type='email' name='email' required /><br/>");
    out.println("<input type='submit' value='Link anfordern'/>");
    out.println("</form>");
    out.println("<p><a href='login'>Zurück zum Login</a></p>");
    out.println("</body></html>");
  }

  @Override
  protected void doPost(HttpServletRequest request, HttpServletResponse response)
      throws IOException, ServletException {
    request.setCharacterEncoding("UTF-8");
    response.setContentType("text/html");
    PrintWriter out = response.getWriter();

    String email = request.getParameter("email");
    
    if (email == null || email.trim().isEmpty()) {
      out.println("<!DOCTYPE html><html><body>");
      out.println("<h2>Fehler</h2>");
      out.println("<p>Bitte geben Sie eine E-Mail-Adresse ein.</p>");
      out.println("<p><a href='forgot-password'>Zurück</a></p>");
      out.println("</body></html>");
      return;
    }

    try {
      Context initCtx = new InitialContext();
      DataSource ds = (DataSource)initCtx.lookup("java:/comp/env/jdbc/mariadb");
      
      try (Connection connection = ds.getConnection()) {
        // Prüfen, ob die E-Mail existiert und Benutzerdaten abrufen
        PreparedStatement ps = connection.prepareStatement(
            "SELECT a.id, p.first_name FROM account a JOIN person p ON a.person_id = p.id WHERE a.email = ?"
        );
        ps.setString(1, email);
        ResultSet rs = ps.executeQuery();
        
        if (rs.next()) {
          int accountId = rs.getInt("id");
          String firstName = rs.getString("first_name");
          
          // Reset-Code generieren
          String resetCode = generateResetCode();
          java.sql.Timestamp expiryTime = new java.sql.Timestamp(System.currentTimeMillis() + 3600 * 1000); // 1 Stunde gültig
          
          // Code in Redis speichern
          Jedis jedis = JedisAdapter.getJedis();
          String key = "reset:" + resetCode;
          jedis.hset(key, "account_id", String.valueOf(accountId));
          jedis.hset(key, "email", email);
          jedis.hset(key, "expiry", String.valueOf(expiryTime.getTime()));
          jedis.expire(key, 3600); // 1 Stunde TTL
          JedisAdapter.releaseJedis(jedis);
          
          // Reset-E-Mail "senden" (in Redis speichern)
          sendResetEmail(email, firstName, resetCode);
        }
        
        // Unabhängig davon, ob die E-Mail existiert oder nicht, zeigen wir dieselbe Nachricht an
        // Das verhindert, dass Angreifer herausfinden können, welche E-Mail-Adressen registriert sind
        out.println("<!DOCTYPE html><html><body>");
        out.println("<h2>Link versendet</h2>");
        out.println("<p>Falls ein Account mit dieser E-Mail-Adresse existiert, haben wir Ihnen einen Link zum Zurücksetzen Ihres Passworts gesendet.</p>");
        out.println("<p>Bitte überprüfen Sie Ihren Posteingang (und ggf. den Spam-Ordner).</p>");
        out.println("<p><a href='login'>Zurück zum Login</a></p>");
        out.println("</body></html>");
      }
    } catch (Exception e) {
      out.println("<!DOCTYPE html><html><body>");
      out.println("<h2>Fehler</h2>");
      out.println("<p>Es ist ein Fehler aufgetreten: " + e.getMessage() + "</p>");
      out.println("<p><a href='forgot-password'>Zurück</a></p>");
      out.println("</body></html>");
      e.printStackTrace(out);
    }
  }
  
  private String generateResetCode() {
    SecureRandom random = new SecureRandom();
    byte[] bytes = new byte[32];
    random.nextBytes(bytes);
    return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
  }
  
  private void sendResetEmail(String email, String firstName, String resetCode) {
    try {
      ServletContext ctx = getServletContext();
      String baseUrl = ctx.getInitParameter("baseurl");
      String webapp = ctx.getInitParameter("webapp");
      String resetUrl = baseUrl + "/" + webapp + "/reset-password?code=" + resetCode;

      EmailService.send(EmailMessageFactory.passwordReset(email, firstName, resetUrl));
    } catch (Exception e) {
      e.printStackTrace();
    }
  }
} 