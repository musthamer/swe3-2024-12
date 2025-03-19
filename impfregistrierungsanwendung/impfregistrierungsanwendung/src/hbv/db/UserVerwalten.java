package hbv.db;

import hbv.model.User;
import java.sql.*;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;

public class UserVerwalten {
  private DataSource dataSource;
  private static UserVerwalten instance;

  private UserVerwalten() {
    try {
      InitialContext ctx = new InitialContext();
      dataSource = (DataSource) ctx.lookup("java:/comp/env/jdbc/mariadb");
    } catch (NamingException e) {
      throw new RuntimeException("Fehler beim Abrufen der Datenquelle", e);
    }
  }

  public static synchronized UserVerwalten getInstance() {
    if (instance == null) instance = new UserVerwalten();
    return instance;
  }

  public boolean existiertUser(String email) throws SQLException {
    String query = "SELECT COUNT(*) FROM user_account WHERE email = ?";
    try (Connection conn = dataSource.getConnection();
        PreparedStatement stmt = conn.prepareStatement(query)) {
      stmt.setString(1, email);
      try (ResultSet rs = stmt.executeQuery()) {
        return rs.next() && rs.getInt(1) > 0;
      }
    }
  }

  public boolean speichereUser(User user) throws SQLException {
    String insertQuery =
        "INSERT INTO user_account (email, vorname, nachname, password_hash, role, assigned_center)"
            + " VALUES (?,?,?,?,?,?)";
    try (Connection conn = dataSource.getConnection();
        PreparedStatement stmt =
            conn.prepareStatement(insertQuery, Statement.RETURN_GENERATED_KEYS)) {
      stmt.setString(1, user.getEmail());
      stmt.setString(2, user.getVorname());
      stmt.setString(3, user.getNachname());
      stmt.setString(4, user.getPasswordHash());
      stmt.setString(5, user.getRole());
      stmt.setString(6, null);
      int affected = stmt.executeUpdate();
      if (affected > 0) {
        try (ResultSet rs = stmt.getGeneratedKeys()) {
          if (rs.next()) user.setId(rs.getInt(1));
        }
        return true;
      }
      return false;
    }
  }

  public User findeUser(String email) throws SQLException {
    String query =
        "SELECT user_id, email, password_hash, vorname, nachname, role, geburtsdatum FROM"
            + " user_account WHERE email = ?";
    try (Connection conn = dataSource.getConnection();
        PreparedStatement stmt = conn.prepareStatement(query)) {
      stmt.setString(1, email);
      try (ResultSet rs = stmt.executeQuery()) {
        if (rs.next()) {
          User user =
              new User(
                  rs.getInt("user_id"),
                  rs.getString("email"),
                  rs.getString("password_hash"),
                  rs.getString("vorname"),
                  rs.getString("nachname"),
                  rs.getString("role"));
          user.setGeburtsdatum(rs.getDate("geburtsdatum"));
          return user;
        }
      }
    }
    return null;
  }
}
