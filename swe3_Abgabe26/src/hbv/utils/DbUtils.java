package hbv.utils;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.sql.DataSource;
import java.sql.Connection;

public class DbUtils {
    
    /**
     * Stellt eine Datenbankverbindung her
     */
    public static Connection getConnection() throws Exception {
        try {
            Context initCtx = new InitialContext();
            DataSource ds = (DataSource)initCtx.lookup("java:/comp/env/jdbc/mariadb");
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