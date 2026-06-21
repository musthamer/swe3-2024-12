package hbv.web;

import hbv.messaging.EmailMessageFactory;
import hbv.messaging.EmailService;
import hbv.utils.PasswordUtils;
import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.security.SecureRandom;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.Base64;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.sql.DataSource;

public class RegisterServlet extends HttpServlet {

  @Override
  protected void doGet(HttpServletRequest request, HttpServletResponse response)
      throws IOException {
    showRegisterForm(response);
  }

  @Override
  protected void doPost(HttpServletRequest request, HttpServletResponse response)
      throws IOException, ServletException {

    request.setCharacterEncoding("UTF-8");

    boolean ajaxRequest = isAjaxRequest(request);

    RegistrationData data = readRegistrationData(request);
    String validationError = validateRegistrationData(data);

    if (validationError != null) {
      if (ajaxRequest) {
        writeJsonResponse(response, false, validationError, null);
      } else {
        showErrorPage(response, "Registrierung fehlgeschlagen", validationError);
      }
      return;
    }

    try {
      registerUser(data);

      String successMessage =
          "Registrierung erfolgreich! Bitte prüfen Sie Ihre E-Mails, um Ihren Account zu"
              + " aktivieren.";

      if (ajaxRequest) {
        writeJsonResponse(response, true, successMessage, "emails");
      } else {
        showSuccessPage(response);
      }

    } catch (IllegalArgumentException e) {
      if (ajaxRequest) {
        writeJsonResponse(response, false, e.getMessage(), null);
      } else {
        showErrorPage(response, "Registrierung fehlgeschlagen", e.getMessage());
      }
    } catch (Exception e) {
      e.printStackTrace();

      String errorMessage = "Bei der Registrierung ist ein interner Fehler aufgetreten.";

      if (ajaxRequest) {
        writeJsonResponse(response, false, errorMessage, null);
      } else {
        showErrorPage(response, "Fehler bei der Registrierung", errorMessage);
      }
    }
  }

  private boolean isAjaxRequest(HttpServletRequest request) {
    return "XMLHttpRequest".equals(request.getHeader("X-Requested-With"));
  }

  private RegistrationData readRegistrationData(HttpServletRequest request) {
    RegistrationData data = new RegistrationData();
    data.firstName = trim(request.getParameter("firstName"));
    data.lastName = trim(request.getParameter("lastName"));
    data.dateOfBirth = trim(request.getParameter("dateOfBirth"));
    data.email = trim(request.getParameter("email"));
    data.password = request.getParameter("password");
    data.passwordConfirm = request.getParameter("passwordConfirm");
    return data;
  }

  private String validateRegistrationData(RegistrationData data) {
    if (isBlank(data.firstName)
        || isBlank(data.lastName)
        || isBlank(data.dateOfBirth)
        || isBlank(data.email)
        || isBlank(data.password)
        || isBlank(data.passwordConfirm)) {
      return "Bitte füllen Sie alle Felder aus.";
    }

    if (!data.password.equals(data.passwordConfirm)) {
      return "Die Passwörter stimmen nicht überein.";
    }

    return null;
  }

  private void registerUser(RegistrationData data) throws Exception {
    DataSource ds = getDataSource();

    try (Connection connection = ds.getConnection()) {
      connection.setAutoCommit(false);

      try {
        if (emailExists(connection, data.email)) {
          throw new IllegalArgumentException("Diese E-Mail-Adresse wird bereits verwendet.");
        }

        int personId = insertPerson(connection, data);
        String passwordHash = PasswordUtils.hashPassword(data.password);
        int accountId = insertAccount(connection, personId, data.email, passwordHash);

        String activationCode = generateActivationCode();
        insertActivation(connection, accountId, activationCode);

        sendActivationEmail(data.email, data.firstName, activationCode);

        connection.commit();
      } catch (Exception e) {
        connection.rollback();
        throw e;
      } finally {
        connection.setAutoCommit(true);
      }
    }
  }

  private DataSource getDataSource() throws Exception {
    Context initCtx = new InitialContext();
    return (DataSource) initCtx.lookup("java:/comp/env/jdbc/mariadb");
  }

  private boolean emailExists(Connection connection, String email) throws Exception {
    String sql = "SELECT COUNT(*) FROM account WHERE email = ?";

    try (PreparedStatement stmt = connection.prepareStatement(sql)) {
      stmt.setString(1, email);

      try (ResultSet rs = stmt.executeQuery()) {
        rs.next();
        return rs.getInt(1) > 0;
      }
    }
  }

  private int insertPerson(Connection connection, RegistrationData data) throws Exception {
    String sql =
        "INSERT INTO person (first_name, last_name, date_of_birth, email) VALUES (?, ?, ?, ?)";

    try (PreparedStatement stmt =
        connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
      stmt.setString(1, data.firstName);
      stmt.setString(2, data.lastName);
      stmt.setDate(3, Date.valueOf(data.dateOfBirth));
      stmt.setString(4, data.email);

      stmt.executeUpdate();

      try (ResultSet rs = stmt.getGeneratedKeys()) {
        if (rs.next()) {
          return rs.getInt(1);
        }
      }
    }

    throw new Exception("Person konnte nicht angelegt werden.");
  }

  private int insertAccount(Connection connection, int personId, String email, String passwordHash)
      throws Exception {
    String sql = "INSERT INTO account (person_id, email, password_hash) VALUES (?, ?, ?)";

    try (PreparedStatement stmt =
        connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
      stmt.setInt(1, personId);
      stmt.setString(2, email);
      stmt.setString(3, passwordHash);

      stmt.executeUpdate();

      try (ResultSet rs = stmt.getGeneratedKeys()) {
        if (rs.next()) {
          return rs.getInt(1);
        }
      }
    }

    throw new Exception("Account konnte nicht angelegt werden.");
  }

  private void insertActivation(Connection connection, int accountId, String activationCode)
      throws Exception {
    String sql =
        "INSERT INTO account_activation (account_id, activation_code, expiry_datetime) VALUES (?,"
            + " ?, ?)";

    Timestamp expiry = new Timestamp(System.currentTimeMillis() + 24L * 60L * 60L * 1000L);

    try (PreparedStatement stmt = connection.prepareStatement(sql)) {
      stmt.setInt(1, accountId);
      stmt.setString(2, activationCode);
      stmt.setTimestamp(3, expiry);
      stmt.executeUpdate();
    }
  }

  private String generateActivationCode() {
    SecureRandom random = new SecureRandom();
    byte[] bytes = new byte[32];
    random.nextBytes(bytes);
    return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
  }

  private void sendActivationEmail(String email, String firstName, String activationCode) {
    try {
      ServletContext ctx = getServletContext();
      String baseUrl = ctx.getInitParameter("baseurl");
      String webapp = ctx.getInitParameter("webapp");
      String activationUrl = baseUrl + "/" + webapp + "/activate?code=" + activationCode;

      EmailService.send(EmailMessageFactory.registration(email, firstName, activationUrl));
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  private void showRegisterForm(HttpServletResponse response) throws IOException {
    response.setContentType("text/html; charset=UTF-8");

    PrintWriter out = response.getWriter();
    out.println("<!DOCTYPE html>");
    out.println("<html>");
    out.println("<head><title>Registrierung</title></head>");
    out.println("<body>");
    out.println("<h2>Neuen Account erstellen</h2>");
    out.println("<form method='POST'>");
    out.println("Vorname: <input type='text' name='firstName' required /><br/>");
    out.println("Nachname: <input type='text' name='lastName' required /><br/>");
    out.println("Geburtsdatum: <input type='date' name='dateOfBirth' required /><br/>");
    out.println("E-Mail: <input type='email' name='email' required /><br/>");
    out.println("Passwort: <input type='password' name='password' required /><br/>");
    out.println(
        "Passwort wiederholen: <input type='password' name='passwordConfirm' required /><br/>");
    out.println("<input type='submit' value='Registrieren' />");
    out.println("</form>");
    out.println("<p><a href='login'>Zurück zum Login</a></p>");
    out.println("</body>");
    out.println("</html>");
  }

  private void showErrorPage(HttpServletResponse response, String title, String message)
      throws IOException {
    response.setContentType("text/html; charset=UTF-8");

    PrintWriter out = response.getWriter();
    out.println("<!DOCTYPE html>");
    out.println("<html><body>");
    out.println("<h2>" + escapeHtml(title) + "</h2>");
    out.println("<p>" + escapeHtml(message) + "</p>");
    out.println("<p><a href='register'>Zurück zum Registrierungsformular</a></p>");
    out.println("</body></html>");
  }

  private void showSuccessPage(HttpServletResponse response) throws IOException {
    response.setContentType("text/html; charset=UTF-8");

    PrintWriter out = response.getWriter();
    out.println("<!DOCTYPE html>");
    out.println("<html><body>");
    out.println("<h2>Registrierung erfolgreich</h2>");
    out.println(
        "<p>Ihr Account wurde erfolgreich erstellt. Bitte prüfen Sie Ihre E-Mails, um Ihren Account"
            + " zu aktivieren.</p>");
    out.println("<p><a href='emails'>E-Mail anzeigen</a></p>");
    out.println("<p><a href='login'>Zum Login</a></p>");
    out.println("</body></html>");
  }

  private void writeJsonResponse(
      HttpServletResponse response, boolean success, String message, String emailsUrl)
      throws IOException {
    response.setContentType("application/json; charset=UTF-8");

    PrintWriter out = response.getWriter();

    StringBuilder json = new StringBuilder();
    json.append("{");
    json.append("\"success\":").append(success).append(",");
    json.append("\"message\":\"").append(escapeJson(message)).append("\"");

    if (emailsUrl != null) {
      json.append(",\"emailsUrl\":\"").append(escapeJson(emailsUrl)).append("\"");
    }

    json.append("}");

    out.println(json.toString());
  }

  private String trim(String value) {
    return value == null ? null : value.trim();
  }

  private boolean isBlank(String value) {
    return value == null || value.trim().isEmpty();
  }

  private String escapeJson(String value) {
    if (value == null) {
      return "";
    }

    return value
        .replace("\\", "\\\\")
        .replace("\"", "\\\"")
        .replace("\n", "\\n")
        .replace("\r", "\\r");
  }

  private String escapeHtml(String value) {
    if (value == null) {
      return "";
    }

    return value
        .replace("&", "&amp;")
        .replace("<", "&lt;")
        .replace(">", "&gt;")
        .replace("\"", "&quot;");
  }

  private static class RegistrationData {
    String firstName;
    String lastName;
    String dateOfBirth;
    String email;
    String password;
    String passwordConfirm;
  }
}
