package hbv.web;

import jakarta.servlet.*;
import jakarta.servlet.http.*;
import java.io.*;

public class ProtectedServlet extends HttpServlet {

  protected void doGet(HttpServletRequest request, HttpServletResponse response)
      throws IOException, ServletException {

    response.setContentType("text/plain");
    PrintWriter out = response.getWriter();

    HttpSession session = request.getSession(false);
    if (session != null) {
      out.println("you are logged in as " + session.getAttribute("user") + " at protected");
    } else {
      response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
      response.sendRedirect("default.txt");
    }
  }
}
