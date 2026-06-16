package hbv.web;
import java.io.*;
import java.time.*;
import java.time.format.*;
import jakarta.servlet.*;
import jakarta.servlet.http.*;
import java.util.*;

public class LogoutServlet extends HttpServlet {

  protected void doGet(HttpServletRequest request,
      HttpServletResponse response)
      throws IOException, ServletException {
      
      // Session holen und invalidieren
      HttpSession session = request.getSession(false);
      if(session != null){
        session.invalidate();
      }

      // Weiterleitung zur Startseite
      response.sendRedirect(request.getContextPath() + "/");
  }
  
  @Override
  protected void doPost(HttpServletRequest request, 
      HttpServletResponse response)
      throws IOException, ServletException {
      
      // Session holen und invalidieren
      HttpSession session = request.getSession(false);
      if(session != null){
        session.invalidate();
      }

      // JSON-Antwort direkt erstellen ohne JsonObject-Klasse
      response.setContentType("application/json");
      PrintWriter out = response.getWriter();
      out.println("{\"success\":true,\"message\":\"Erfolgreich abgemeldet\"}");
  }
}
