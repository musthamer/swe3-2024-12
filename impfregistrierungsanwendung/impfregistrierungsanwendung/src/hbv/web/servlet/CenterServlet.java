package hbv.web.servlet;

import hbv.service.CenterService;
import jakarta.servlet.*;
import jakarta.servlet.http.*;
import java.io.*;
import java.sql.SQLException;

public class CenterServlet extends HttpServlet {
  private CenterService centerService = new CenterService();

  @Override
  protected void doGet(HttpServletRequest req, HttpServletResponse resp)
      throws ServletException, IOException {
    resp.setContentType("text/html;charset=UTF-8");
    PrintWriter out = resp.getWriter();
    out.println("<h2>Impfzentrum anlegen</h2>");
    out.println("<form method='post' action='center'>");
    out.println("Zentrum Name: <input type='text' name='centerName' required/><br>");
    out.println("Email: <input type='email' name='centerEmail' required/><br>");
    out.println("Passwort: <input type='password' name='centerPassword' required/><br>");
    out.println("<input type='submit' value='Anlegen'/>");
    out.println("</form>");
  }

  @Override
  protected void doPost(HttpServletRequest req, HttpServletResponse resp)
      throws ServletException, IOException {
    String centerName = req.getParameter("centerName");
    String centerEmail = req.getParameter("centerEmail");
    String centerPassword = req.getParameter("centerPassword");
    try {
      centerService.createCenter(centerName, centerEmail, centerPassword);
      resp.getWriter().println("Impfzentrum erfolgreich angelegt.");
    } catch (SQLException e) {
      throw new ServletException("Fehler beim Anlegen des Impfzentrums: " + e.getMessage(), e);
    }
  }
}
