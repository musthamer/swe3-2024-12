package hbv.web;

import jakarta.servlet.*;
import jakarta.servlet.http.*;
import java.io.*;
import java.sql.*;
import java.util.*;
import javax.naming.*;
import javax.sql.*;

public class SQLServlet extends HttpServlet {

  protected void doGet(HttpServletRequest request, HttpServletResponse response)
      throws IOException, ServletException {

    response.setContentType("text/plain");
    PrintWriter out = response.getWriter();
    long start = System.currentTimeMillis();
    try {
      // Naming Context
      Context initCtx = new InitialContext();
      DataSource ds = (DataSource) initCtx.lookup("java:/comp/env/jdbc/mariadb");

      // get connection ...
      Connection connection = ds.getConnection();

      // preparedstatement to prevent sql-injection
      PreparedStatement ps =
          connection.prepareStatement(
              "insert into demo (name) values(?)", Statement.RETURN_GENERATED_KEYS);
      ps.setString(1, "whatevercomes...");
      ps.executeUpdate();

      // get last generated Key
      ResultSet mrs = ps.getGeneratedKeys();
      if (mrs.next()) {
        long id = mrs.getLong(1);
        out.println("lastid:" + id);
      }
      ps.close();

      /*

             // simple select statement is ok
             Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery("select * from demo");
             int rows=0;

             // cursor pattern
             while(rs.next()){
               long id = rs.getLong("id");
               String name = rs.getString("name");
               rows++;
             }
             out.format("%3d\n",rows);
             rs.close();
             stmt.close();
      */

      connection.close();

    } catch (Exception e) {
      out.println(e);
      e.printStackTrace(out);
      throw new RuntimeException(e);
    }
    long ende = System.currentTimeMillis();
    out.println((ende - start) + "ms");

    ServletContext context = getServletContext();
  }
}
