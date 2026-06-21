package hbv.utils;

import java.sql.Connection;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.sql.DataSource;

public class DbUtils {

  public static Connection getConnection() throws Exception {
    Context initCtx = new InitialContext();
    DataSource ds = (DataSource) initCtx.lookup("java:/comp/env/jdbc/mariadb");
    return ds.getConnection();
  }
}
