package hbv.web;

import jakarta.servlet.*;
import jakarta.servlet.http.*;
import java.io.*;
import java.time.*;
import java.time.format.*;

public class LogoutServlet extends HttpServlet {

  protected void doGet(HttpServletRequest request, HttpServletResponse response)
      throws IOException, ServletException {

    String name = "";
    HttpSession session = request.getSession(false);
    if (session != null) {
      name = (String) session.getAttribute("user");
      session.invalidate();
    }

    response.setContentType("text/plain");
    PrintWriter out = response.getWriter();
    out.println("you are logged out ");
  }
}
