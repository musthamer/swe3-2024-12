package hbv.web;
import java.io.*;
import jakarta.servlet.*;
import jakarta.servlet.http.*;
import java.sql.*;
import javax.sql.*;
import javax.naming.*;
import java.util.*;
import java.security.*;
import java.util.Base64;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import hbv.service.AccountService;
import org.json.JSONObject;

public class LoginServlet extends HttpServlet {

  private AccountService accountService = new AccountService();

  @Override
  protected void doGet(HttpServletRequest request, HttpServletResponse response)
      throws IOException, ServletException {
    
    // Direkte GET-Anfragen an das Servlet leiten zum Login-Formular weiter
    response.sendRedirect("login.html");
  }

  @Override
  protected void doPost(HttpServletRequest request, HttpServletResponse response)
      throws IOException, ServletException {
    request.setCharacterEncoding("UTF-8");
    String email = request.getParameter("email");
    String password = request.getParameter("password");
    String redirect = request.getParameter("redirect");
    
    response.setContentType("application/json");
    PrintWriter out = response.getWriter();
    Map<String, Object> jsonResponse = new HashMap<>();
    
    try {
      Map<String, Object> authResult = accountService.authenticate(email, password);
      
      if ((Boolean) authResult.get("success")) {
        HttpSession session = request.getSession(true);
        session.setAttribute("loggedin", true);
        session.setAttribute("email", email);
        session.setAttribute("userId", authResult.get("userId"));
        session.setAttribute("userRole", authResult.get("userRole"));
        session.setAttribute("userName", authResult.get("userName"));
        
        // Weiterleitung basierend auf der Benutzerrolle
        String redirectUrl;
        if ("ADMIN".equals(authResult.get("userRole"))) {
          redirectUrl = "admin";
        } else {
          redirectUrl = redirect != null ? redirect : "booking.html";
        }
        
        jsonResponse.put("success", true);
        jsonResponse.put("message", "Login erfolgreich");
        jsonResponse.put("redirectUrl", redirectUrl);
      } else {
        jsonResponse.put("success", false);
        jsonResponse.put("message", authResult.get("message"));
      }
    } catch (Exception e) {
      jsonResponse.put("success", false);
      jsonResponse.put("message", "Fehler bei der Anmeldung: " + e.getMessage());
      e.printStackTrace(out);
    }
    
    out.println(new JSONObject(jsonResponse).toString());
  }
}
