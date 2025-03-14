package hbv.web;

import jakarta.servlet.*;
import jakarta.servlet.http.*;
import java.io.*;

public class LoginServlet extends HttpServlet {

  protected void doGet(HttpServletRequest request, HttpServletResponse response)
      throws IOException, ServletException {

    String user = request.getParameter("user");
    String passwd = request.getParameter("passwd");

    // check if valid combination
    if (user != null && passwd != null) {
      HttpSession session = request.getSession();
      session.setAttribute("user", user);
      response.setStatus(HttpServletResponse.SC_TEMPORARY_REDIRECT);
      response.sendRedirect("protected");
    } else {
      response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
      response.sendRedirect("default.txt");
    }
  }
}
