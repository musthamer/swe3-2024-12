package hbv.web;

import jakarta.servlet.*;
import jakarta.servlet.http.*;
import java.io.*;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.util.*;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;

public class PBKDF2Servlet extends HttpServlet {

  @Override
  protected void doGet(HttpServletRequest request, HttpServletResponse response)
      throws IOException, ServletException {
    response.setContentType("text/plain");
    PrintWriter out = response.getWriter();
    // for the purpose of testing only
    String password = "password";
    byte[] salt = HexFormat.of().parseHex("6161616161616161");
    int work = 60000;
    String workParam = request.getParameter("work");
    if (workParam != null) {
      try {
        work = Integer.parseInt(workParam);
      } catch (NumberFormatException nfe) {
      }
    }

    try {
      byte[] hash = hashPassword(password, salt, work);
      out.println("hex-salt:" + HexFormat.of().formatHex(salt));
      out.println("hex-hash:" + HexFormat.of().formatHex(hash));
    } catch (Exception e) {
    }
  }

  byte[] hashPassword(String password, byte[] salt, int work)
      throws NoSuchAlgorithmException, InvalidKeySpecException {
    SecretKeyFactory secretKeyFactory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA512");
    PBEKeySpec spec = new PBEKeySpec(password.toCharArray(), salt, work, 512);
    SecretKey key = secretKeyFactory.generateSecret(spec);
    spec.clearPassword();
    return key.getEncoded();
  }
}
