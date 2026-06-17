package hbv.service;

import hbv.utils.DbUtils;
import java.sql.*;
import java.util.*;

public class PersonService {

  /** Liefert die Profildaten des Account-Inhabers (für Formular-Vorausfüllung). */
  public Map<String, Object> getAccountHolderProfile(int userId) throws Exception {
    Map<String, Object> profile = new HashMap<>();
    try (Connection connection = DbUtils.getConnection();
        PreparedStatement ps =
            connection.prepareStatement(
                "SELECT p.first_name, p.last_name, p.date_of_birth, a.email "
                    + "FROM account a "
                    + "LEFT JOIN person p ON a.person_id = p.id "
                    + "WHERE a.id = ?")) {
      ps.setInt(1, userId);
      ResultSet rs = ps.executeQuery();
      if (rs.next()) {
        profile.put("firstName", rs.getString("first_name"));
        profile.put("lastName", rs.getString("last_name"));
        java.sql.Date dob = rs.getDate("date_of_birth");
        profile.put("dateOfBirth", dob != null ? dob.toString() : "");
        profile.put("email", rs.getString("email"));
      }
    }
    return profile;
  }

  /**
   * Ermittelt oder legt die Person für eine Buchung an. forSelf=true: Account-Inhaber;
   * forSelf=false: Drittperson (Familienmitglied).
   */
  public int resolvePersonForBooking(
      int userId, String firstName, String lastName, String dateOfBirth, boolean forSelf)
      throws Exception {
    try (Connection connection = DbUtils.getConnection()) {
      connection.setAutoCommit(false);
      try {
        int personId;
        if (forSelf) {
          personId = getAccountHolderPersonId(connection, userId);
          if (personId <= 0) {
            personId =
                createAccountHolderPerson(connection, userId, firstName, lastName, dateOfBirth);
          } else {
            updatePerson(connection, personId, firstName, lastName, dateOfBirth);
          }
        } else {
          personId = findDependent(connection, userId, firstName, lastName, dateOfBirth);
          if (personId <= 0) {
            personId = createDependent(connection, userId, firstName, lastName, dateOfBirth);
          }
        }
        connection.commit();
        return personId;
      } catch (Exception e) {
        connection.rollback();
        throw e;
      }
    }
  }

  private int getAccountHolderPersonId(Connection connection, int userId) throws SQLException {
    try (PreparedStatement ps =
        connection.prepareStatement("SELECT person_id FROM account WHERE id = ?")) {
      ps.setInt(1, userId);
      ResultSet rs = ps.executeQuery();
      if (rs.next()) {
        int personId = rs.getInt("person_id");
        return rs.wasNull() ? -1 : personId;
      }
    }
    return -1;
  }

  private int findDependent(
      Connection connection, int userId, String firstName, String lastName, String dateOfBirth)
      throws SQLException {
    try (PreparedStatement ps =
        connection.prepareStatement(
            "SELECT id FROM person WHERE account_id = ? AND first_name = ? AND last_name = ? AND"
                + " date_of_birth = ?")) {
      ps.setInt(1, userId);
      ps.setString(2, firstName.trim());
      ps.setString(3, lastName.trim());
      ps.setDate(4, java.sql.Date.valueOf(dateOfBirth));
      ResultSet rs = ps.executeQuery();
      return rs.next() ? rs.getInt("id") : -1;
    }
  }

  private void updatePerson(
      Connection connection, int personId, String firstName, String lastName, String dateOfBirth)
      throws SQLException {
    try (PreparedStatement ps =
        connection.prepareStatement(
            "UPDATE person SET first_name = ?, last_name = ?, date_of_birth = ? WHERE id = ?")) {
      ps.setString(1, firstName.trim());
      ps.setString(2, lastName.trim());
      ps.setDate(3, java.sql.Date.valueOf(dateOfBirth));
      ps.setInt(4, personId);
      ps.executeUpdate();
    }
  }

  private int createAccountHolderPerson(
      Connection connection, int userId, String firstName, String lastName, String dateOfBirth)
      throws SQLException {
    String email = getUserEmail(connection, userId);
    try (PreparedStatement ps =
        connection.prepareStatement(
            "INSERT INTO person (first_name, last_name, date_of_birth, email) VALUES (?, ?, ?, ?)",
            Statement.RETURN_GENERATED_KEYS)) {
      ps.setString(1, firstName.trim());
      ps.setString(2, lastName.trim());
      ps.setDate(3, java.sql.Date.valueOf(dateOfBirth));
      ps.setString(4, email);
      ps.executeUpdate();

      ResultSet keys = ps.getGeneratedKeys();
      if (!keys.next()) {
        throw new SQLException("Person erstellen fehlgeschlagen, keine ID erhalten.");
      }
      int personId = keys.getInt(1);

      try (PreparedStatement linkPs =
          connection.prepareStatement("UPDATE account SET person_id = ? WHERE id = ?")) {
        linkPs.setInt(1, personId);
        linkPs.setInt(2, userId);
        linkPs.executeUpdate();
      }
      return personId;
    }
  }

  private int createDependent(
      Connection connection, int userId, String firstName, String lastName, String dateOfBirth)
      throws SQLException {
    try (PreparedStatement ps =
        connection.prepareStatement(
            "INSERT INTO person (first_name, last_name, date_of_birth, email, account_id) "
                + "VALUES (?, ?, ?, NULL, ?)",
            Statement.RETURN_GENERATED_KEYS)) {
      ps.setString(1, firstName.trim());
      ps.setString(2, lastName.trim());
      ps.setDate(3, java.sql.Date.valueOf(dateOfBirth));
      ps.setInt(4, userId);
      ps.executeUpdate();

      ResultSet keys = ps.getGeneratedKeys();
      if (keys.next()) {
        return keys.getInt(1);
      }
      throw new SQLException("Drittperson erstellen fehlgeschlagen, keine ID erhalten.");
    }
  }

  private String getUserEmail(Connection connection, int userId) throws SQLException {
    try (PreparedStatement ps =
        connection.prepareStatement("SELECT email FROM account WHERE id = ?")) {
      ps.setInt(1, userId);
      ResultSet rs = ps.executeQuery();
      if (!rs.next()) {
        throw new SQLException("Keine E-Mail-Adresse für Benutzer-ID " + userId + " gefunden");
      }
      return rs.getString("email");
    }
  }
}
