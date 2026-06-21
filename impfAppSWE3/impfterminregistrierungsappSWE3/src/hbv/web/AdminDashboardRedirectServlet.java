package hbv.web;

import jakarta.servlet.*;
import jakarta.servlet.http.*;
import java.io.IOException;

public class AdminDashboardRedirectServlet extends HttpServlet {
  @Override
  protected void doGet(HttpServletRequest request, HttpServletResponse response)
      throws ServletException, IOException {

    HttpSession session = request.getSession(false);
    String userRole = (session != null) ? (String) session.getAttribute("userRole") : null;

    if (session == null || !"ADMIN".equals(userRole)) {
      response.sendRedirect(request.getContextPath() + "/");
      return;
    }

    response.sendRedirect("admin/dashboard.html");
  }
}
