package hbv.service;

import hbv.utils.DbUtils;
import hbv.utils.PasswordUtils;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.HashMap;
import java.util.Map;

public class AccountService {
  public Map<String, Object> authenticate(String email, String password) throws Exception {
    Map<String, Object> result = new HashMap<>();

    try (Connection conn = DbUtils.getConnection()) {
      PreparedStatement ps =
          conn.prepareStatement(
              "SELECT a.id, a.password_hash, a.is_admin, p.first_name, p.last_name "
                  + "FROM account a "
                  + "LEFT JOIN person p ON a.person_id = p.id "
                  + "WHERE a.email = ?");
      ps.setString(1, email);
      ResultSet rs = ps.executeQuery();

      if (rs.next()) {
        int userId = rs.getInt("id");
        String storedHash = rs.getString("password_hash");
        boolean isAdmin = rs.getBoolean("is_admin");
        String firstName = rs.getString("first_name");
        String lastName = rs.getString("last_name");

        if (PasswordUtils.verifyPassword(password, storedHash)) {
          result.put("success", true);
          result.put("message", "Login erfolgreich");
          result.put("userId", userId);
          result.put("userRole", isAdmin ? "ADMIN" : "USER");
          result.put("userName", firstName + " " + lastName);
        } else {
          result.put("success", false);
          result.put("message", "Falsches Passwort");
        }
      } else {
        result.put("success", false);
        result.put("message", "E-Mail-Adresse nicht gefunden");
      }
    }

    return result;
  }
}
