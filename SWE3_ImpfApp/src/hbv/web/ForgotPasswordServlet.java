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
import hbv.messaging.RedisEmailSender;
import redis.clients.jedis.Jedis;

public class ForgotPasswordServlet extends HttpServlet {

  private static final String SUCCESS_MESSAGE =
      "Falls ein Account mit dieser E-Mail-Adresse existiert, haben wir Ihnen einen Link zum Zurücksetzen Ihres Passworts gesendet.";

  @Override
  protected void doGet(HttpServletRequest request, HttpServletResponse response)
      throws IOException {
    response.sendRedirect(request.getContextPath() + "/#/forgot");
  }

  @Override
  protected void doPost(HttpServletRequest request, HttpServletResponse response)
      throws IOException, ServletException {
    request.setCharacterEncoding("UTF-8");
    boolean ajaxRequest = "XMLHttpRequest".equals(request.getHeader("X-Requested-With"));

    String email = request.getParameter("email");

    if (email == null || email.trim().isEmpty()) {
      if (ajaxRequest) {
        writeJson(response, false, "Bitte geben Sie eine E-Mail-Adresse ein.");
      } else {
        writeHtmlError(response, "Bitte geben Sie eine E-Mail-Adresse ein.");
      }
      return;
    }

    try {
      Context initCtx = new InitialContext();
      DataSource ds = (DataSource) initCtx.lookup("java:/comp/env/jdbc/mariadb");

      try (Connection connection = ds.getConnection()) {
        PreparedStatement ps = connection.prepareStatement(
            "SELECT a.id, p.first_name FROM account a JOIN person p ON a.person_id = p.id WHERE a.email = ?"
        );
        ps.setString(1, email);
        ResultSet rs = ps.executeQuery();

        if (rs.next()) {
          int accountId = rs.getInt("id");
          String firstName = rs.getString("first_name");

          String resetCode = generateResetCode();
          java.sql.Timestamp expiryTime = new java.sql.Timestamp(System.currentTimeMillis() + 3600 * 1000);

          Jedis jedis = JedisAdapter.getJedis();
          String key = "reset:" + resetCode;
          jedis.hset(key, "account_id", String.valueOf(accountId));
          jedis.hset(key, "email", email);
          jedis.hset(key, "expiry", String.valueOf(expiryTime.getTime()));
          jedis.expire(key, 3600);
          JedisAdapter.releaseJedis(jedis);

          sendResetEmail(request.getSession().getId(), email, firstName, resetCode);
        }

        if (ajaxRequest) {
          writeJson(response, true, SUCCESS_MESSAGE);
        } else {
          writeHtmlSuccess(response);
        }
      }
    } catch (Exception e) {
      if (ajaxRequest) {
        writeJson(response, false, "Es ist ein Fehler aufgetreten: " + e.getMessage());
      } else {
        writeHtmlError(response, "Es ist ein Fehler aufgetreten: " + e.getMessage());
      }
      e.printStackTrace();
    }
  }

  private void writeJson(HttpServletResponse response, boolean success, String message) throws IOException {
    response.setContentType("application/json; charset=UTF-8");
    PrintWriter out = response.getWriter();
    out.println("{\"success\":" + success + ",\"message\":\"" + escapeJson(message) + "\"}");
  }

  private void writeHtmlSuccess(HttpServletResponse response) throws IOException {
    response.setContentType("text/html; charset=UTF-8");
    PrintWriter out = response.getWriter();
    out.println("<!DOCTYPE html><html><body>");
    out.println("<h2>Link versendet</h2>");
    out.println("<p>" + SUCCESS_MESSAGE + "</p>");
    out.println("<p><a href='./'>Zurück zum Login</a></p>");
    out.println("</body></html>");
  }

  private void writeHtmlError(HttpServletResponse response, String message) throws IOException {
    response.setContentType("text/html; charset=UTF-8");
    PrintWriter out = response.getWriter();
    out.println("<!DOCTYPE html><html><body>");
    out.println("<h2>Fehler</h2>");
    out.println("<p>" + message + "</p>");
    out.println("<p><a href='./#/forgot'>Zurück</a></p>");
    out.println("</body></html>");
  }

  private String escapeJson(String value) {
    return value.replace("\\", "\\\\").replace("\"", "\\\"");
  }

  private String generateResetCode() {
    SecureRandom random = new SecureRandom();
    byte[] bytes = new byte[32];
    random.nextBytes(bytes);
    return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
  }

  private void sendResetEmail(String sessionId, String email, String firstName, String resetCode) {
    try {
      ServletContext ctx = getServletContext();
      String baseUrl = ctx.getInitParameter("baseurl");
      String webapp = ctx.getInitParameter("webapp");
      String resetUrl = baseUrl + "/" + webapp + "/reset-password?code=" + resetCode;

      RedisEmailSender.clearSession(sessionId);
      RedisEmailSender.send(EmailMessageFactory.passwordReset(email, firstName, resetUrl), sessionId);
    } catch (Exception e) {
      e.printStackTrace();
    }
  }
}
