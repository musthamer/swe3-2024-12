package hbv.utils;

import java.sql.Connection;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.sql.DataSource;

public class DbUtils {

  /** Stellt eine Datenbankverbindung her */
  public static Connection getConnection() throws Exception {
    try {
      Context initCtx = new InitialContext();
      DataSource ds = (DataSource) initCtx.lookup("java:/comp/env/jdbc/mariadb");
      Connection conn = ds.getConnection();
      System.out.println("Datenbankverbindung erfolgreich hergestellt");
      return conn;
    } catch (Exception e) {
      System.err.println("Fehler beim Verbinden zur Datenbank: " + e.getMessage());
      e.printStackTrace();
      throw e;
    }
  }
}
