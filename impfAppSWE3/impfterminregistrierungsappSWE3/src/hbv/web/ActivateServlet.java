package hbv.web;

import hbv.messaging.EmailMessageFactory;
import hbv.messaging.RedisEmailSender;
import jakarta.servlet.*;
import jakarta.servlet.http.*;
import java.io.*;
import java.sql.*;
import javax.naming.*;
import javax.sql.*;

public class ActivateServlet extends HttpServlet {

  @Override
  protected void doGet(HttpServletRequest request, HttpServletResponse response)
      throws IOException, ServletException {

    String activationCode = request.getParameter("code");

    if (activationCode == null || activationCode.trim().isEmpty()) {
      response.sendRedirect(request.getContextPath() + "/");
      return;
    }

    response.setContentType("text/html");
    PrintWriter out = response.getWriter();

    try {
      Context initCtx = new InitialContext();
      DataSource ds = (DataSource) initCtx.lookup("java:/comp/env/jdbc/mariadb");

      try (Connection connection = ds.getConnection()) {
        // Aktivierungscode in der Datenbank suchen
        PreparedStatement ps =
            connection.prepareStatement(
                "SELECT a.account_id, a.expiry_datetime, p.first_name, ac.email "
                    + "FROM account_activation a "
                    + "JOIN account ac ON a.account_id = ac.id "
                    + "JOIN person p ON ac.person_id = p.id "
                    + "WHERE a.activation_code = ?");
        ps.setString(1, activationCode);
        ResultSet rs = ps.executeQuery();

        if (rs.next()) {
          int accountId = rs.getInt("account_id");
          java.sql.Timestamp expiryTime = rs.getTimestamp("expiry_datetime");
          String firstName = rs.getString("first_name");
          String email = rs.getString("email");

          // Prüfen, ob der Code abgelaufen ist
          if (expiryTime.before(new java.sql.Timestamp(System.currentTimeMillis()))) {
            out.println("<!DOCTYPE html><html><body>");
            out.println("<h2>Aktivierungscode abgelaufen</h2>");
            out.println(
                "<p>Ihr Aktivierungscode ist leider abgelaufen. Bitte registrieren Sie sich"
                    + " erneut.</p>");
            out.println("<p><a href='register'>Zur Registrierung</a></p>");
            out.println("</body></html>");
            return;
          }

          // Account aktivieren und Aktivierungscode löschen
          connection.setAutoCommit(false);

          try {
            // Aktivierungscode löschen
            PreparedStatement deleteActivation =
                connection.prepareStatement(
                    "DELETE FROM account_activation WHERE activation_code = ?");
            deleteActivation.setString(1, activationCode);
            deleteActivation.executeUpdate();

            // Transaktion abschließen
            connection.commit();

            // Erfolgsseite anzeigen
            out.println("<!DOCTYPE html><html><body>");
            out.println("<h2>Account aktiviert</h2>");
            out.println(
                "<p>Hallo "
                    + firstName
                    + ", Ihr Account wurde erfolgreich aktiviert. Sie können sich jetzt"
                    + " anmelden.</p>");
            out.println("<p><a href='./'>Zum Login</a></p>");
            out.println("</body></html>");

            // Bestätigungs-E-Mail "senden" (in Redis speichern)
            sendActivationConfirmationEmail(request.getSession().getId(), email, firstName);

          } catch (Exception e) {
            // Transaktion zurückrollen bei Fehler
            connection.rollback();
            throw e;
          } finally {
            // Auto-Commit wiederherstellen
            connection.setAutoCommit(true);
          }
        } else {
          out.println("<!DOCTYPE html><html><body>");
          out.println("<h2>Ungültiger Aktivierungscode</h2>");
          out.println("<p>Der von Ihnen angegebene Aktivierungscode ist ungültig.</p>");
          out.println("<p><a href='register'>Zur Registrierung</a></p>");
          out.println("</body></html>");
        }
      }
    } catch (Exception e) {
      out.println("<!DOCTYPE html><html><body>");
      out.println("<h2>Fehler bei der Aktivierung</h2>");
      out.println("<p>Es ist ein Fehler aufgetreten: " + e.getMessage() + "</p>");
      out.println("<p><a href='register'>Zur Registrierung</a></p>");
      out.println("</body></html>");
      e.printStackTrace(out);
    }
  }

  private void sendActivationConfirmationEmail(String sessionId, String email, String firstName) {
    try {
      RedisEmailSender.send(EmailMessageFactory.activation(email, firstName), sessionId);
    } catch (Exception e) {
      e.printStackTrace();
    }
  }
}
