package hbv.web;
import jakarta.servlet.*;
import jakarta.servlet.http.*;
import java.io.*;
import java.sql.*;

public class SQLServlet extends HttpServlet {
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
      throws ServletException, IOException {
        resp.setContentType("text/plain");
        PrintWriter out = resp.getWriter();
        long start = System.currentTimeMillis();
        try (Connection conn = hbv.service.Database.getConnection()) {
            PreparedStatement ps = conn.prepareStatement("INSERT INTO demo (name) VALUES (?)", Statement.RETURN_GENERATED_KEYS);
            ps.setString(1, "jdbc-demo");
            ps.executeUpdate();
            ResultSet rs = ps.getGeneratedKeys();
            if(rs.next()){
                out.println("Last inserted ID: " + rs.getLong(1));
            }
            ps.close();
        } catch(SQLException e){
            out.println("SQL Fehler: " + e.getMessage());
        }
        long end = System.currentTimeMillis();
        out.println("Dauer: " + (end-start) + "ms");
    }
}
