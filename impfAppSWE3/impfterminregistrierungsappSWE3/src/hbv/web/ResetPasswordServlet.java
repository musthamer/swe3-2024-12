package hbv.web;

import hbv.utils.PasswordUtils;
import jakarta.servlet.*;
import jakarta.servlet.http.*;
import java.io.*;
import java.sql.*;
import java.util.*;
import javax.naming.*;
import javax.sql.*;
import redis.clients.jedis.Jedis;

public class ResetPasswordServlet extends HttpServlet {

  @Override
  protected void doGet(HttpServletRequest request, HttpServletResponse response)
      throws IOException, ServletException {
    String resetCode = request.getParameter("code");

    if (resetCode == null || resetCode.isEmpty()) {
      response.sendRedirect(request.getContextPath() + "/");
      return;
    }

    response.setContentType("text/html");
    PrintWriter out = response.getWriter();

    // Prüfen, ob der Reset-Code gültig ist
    Jedis jedis = JedisAdapter.getJedis();
    String key = "reset:" + resetCode;

    if (!jedis.exists(key)) {
      out.println("<!DOCTYPE html><html><body>");
      out.println("<h2>Ungültiger oder abgelaufener Link</h2>");
      out.println("<p>Der Link zum Zurücksetzen Ihres Passworts ist ungültig oder abgelaufen.</p>");
      out.println("<p><a href='forgot-password'>Neuen Link anfordern</a></p>");
      out.println("</body></html>");
      JedisAdapter.releaseJedis(jedis);
      return;
    }

    String email = jedis.hget(key, "email");
    JedisAdapter.releaseJedis(jedis);

    // Formular zum Eingeben des neuen Passworts anzeigen
    out.println("<!DOCTYPE html>");
    out.println("<html><head><title>Neues Passwort festlegen</title></head><body>");
    out.println("<h2>Neues Passwort festlegen</h2>");
    out.println("<p>Bitte geben Sie Ihr neues Passwort ein.</p>");
    out.println("<form method='POST'>");
    out.println("<input type='hidden' name='resetCode' value='" + resetCode + "' />");
    out.println("Neues Passwort: <input type='password' name='password' required /><br/>");
    out.println(
        "Passwort wiederholen: <input type='password' name='passwordConfirm' required /><br/>");
    out.println("<input type='submit' value='Passwort ändern'/>");
    out.println("</form>");
    out.println("</body></html>");
  }

  @Override
  protected void doPost(HttpServletRequest request, HttpServletResponse response)
      throws IOException, ServletException {
    request.setCharacterEncoding("UTF-8");
    response.setContentType("text/html");
    PrintWriter out = response.getWriter();

    String resetCode = request.getParameter("resetCode");
    String password = request.getParameter("password");
    String passwordConfirm = request.getParameter("passwordConfirm");

    if (resetCode == null
        || password == null
        || passwordConfirm == null
        || resetCode.isEmpty()
        || password.isEmpty()
        || passwordConfirm.isEmpty()) {
      out.println("<!DOCTYPE html><html><body>");
      out.println("<h2>Fehler</h2>");
      out.println("<p>Bitte füllen Sie alle Felder aus.</p>");
      out.println("<p><a href='reset-password?code=" + resetCode + "'>Zurück</a></p>");
      out.println("</body></html>");
      return;
    }

    if (!password.equals(passwordConfirm)) {
      out.println("<!DOCTYPE html><html><body>");
      out.println("<h2>Fehler</h2>");
      out.println("<p>Die Passwörter stimmen nicht überein.</p>");
      out.println("<p><a href='reset-password?code=" + resetCode + "'>Zurück</a></p>");
      out.println("</body></html>");
      return;
    }

    try {
      // Prüfen, ob der Reset-Code gültig ist
      Jedis jedis = JedisAdapter.getJedis();
      String key = "reset:" + resetCode;

      if (!jedis.exists(key)) {
        out.println("<!DOCTYPE html><html><body>");
        out.println("<h2>Ungültiger oder abgelaufener Link</h2>");
        out.println(
            "<p>Der Link zum Zurücksetzen Ihres Passworts ist ungültig oder abgelaufen.</p>");
        out.println("<p><a href='forgot-password'>Neuen Link anfordern</a></p>");
        out.println("</body></html>");
        JedisAdapter.releaseJedis(jedis);
        return;
      }

      String accountId = jedis.hget(key, "account_id");

      // Reset-Code aus Redis löschen (Einmalverwendung)
      jedis.del(key);
      JedisAdapter.releaseJedis(jedis);

      // Passwort in der Datenbank aktualisieren
      Context initCtx = new InitialContext();
      DataSource ds = (DataSource) initCtx.lookup("java:/comp/env/jdbc/mariadb");

      try (Connection connection = ds.getConnection()) {
        String passwordHash = PasswordUtils.hashPassword(password);

        // Passwort aktualisieren
        PreparedStatement ps =
            connection.prepareStatement("UPDATE account SET password_hash = ? WHERE id = ?");
        ps.setString(1, passwordHash);
        ps.setInt(2, Integer.parseInt(accountId));
        int rowsAffected = ps.executeUpdate();

        if (rowsAffected > 0) {
          out.println("<!DOCTYPE html><html><body>");
          out.println("<h2>Passwort geändert</h2>");
          out.println(
              "<p>Ihr Passwort wurde erfolgreich geändert. Sie können sich jetzt mit Ihrem neuen"
                  + " Passwort anmelden.</p>");
          out.println("<p><a href='./'>Zum Login</a></p>");
          out.println("</body></html>");
        } else {
          out.println("<!DOCTYPE html><html><body>");
          out.println("<h2>Fehler</h2>");
          out.println("<p>Das Passwort konnte nicht geändert werden.</p>");
          out.println("<p><a href='forgot-password'>Erneut versuchen</a></p>");
          out.println("</body></html>");
        }
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
}
