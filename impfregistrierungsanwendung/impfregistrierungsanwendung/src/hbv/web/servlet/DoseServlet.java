package hbv.web.servlet;

import hbv.service.DoseService;
import jakarta.servlet.*;
import jakarta.servlet.http.*;
import java.io.*;
import java.sql.SQLException;

public class DoseServlet extends HttpServlet {
  private DoseService doseService = new DoseService();

  @Override
  protected void doGet(HttpServletRequest req, HttpServletResponse resp)
      throws ServletException, IOException {
    resp.setContentType("text/html;charset=UTF-8");
    PrintWriter out = resp.getWriter();
    out.println("<h2>Impfdosen einpflegen</h2>");
    out.println("<form method='post' action='dose'>");
    out.println("Impfstoff: <select name='vaccine'>");
    out.println("<option value='Biontech'>Biontech</option>");
    out.println("<option value='Moderna'>Moderna</option>");
    out.println("</select><br>");
    out.println("Menge: <input type='number' name='quantity' required/><br>");
    out.println("Impfzentrum: <select name='centerName'>");
    for (int i = 1; i <= 30; i++) {
      out.println("<option value='Zentrum " + i + "'>Zentrum " + i + "</option>");
    }
    out.println("</select><br>");
    out.println("<input type='submit' value='Einpflegen'/>");
    out.println("</form>");
  }

  @Override
  protected void doPost(HttpServletRequest req, HttpServletResponse resp)
      throws ServletException, IOException {
    String vaccine = req.getParameter("vaccine");
    int quantity = Integer.parseInt(req.getParameter("quantity"));
    String centerName = req.getParameter("centerName");
    try {
      doseService.addDoses(vaccine, quantity, centerName);
      resp.getWriter().println("Impfdosen erfolgreich eingepflegt.");
    } catch (SQLException e) {
      throw new ServletException("Fehler beim Einpflegen der Dosen: " + e.getMessage(), e);
    }
  }
}
