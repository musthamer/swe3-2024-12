package hbv.web;

import jakarta.servlet.*;
import jakarta.servlet.http.*;
import java.io.*;
import java.security.SecureRandom;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import java.util.HexFormat;

public class PBKDF2Servlet extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
      throws ServletException, IOException {
        resp.setContentType("text/plain");
        PrintWriter out = resp.getWriter();
        String password = "password";
        byte[] salt = HexFormat.of().parseHex("6161616161616161");
        int work = 60000;
        String workParam = req.getParameter("work");
        if(workParam != null){
            try {
                work = Integer.parseInt(workParam);
            } catch(NumberFormatException nfe){}
        }
        try {
            byte[] hash = hashPassword(password, salt, work);
            out.println("hex-salt:" + HexFormat.of().formatHex(salt));
            out.println("hex-hash:" + HexFormat.of().formatHex(hash));
        } catch(Exception e) {
            out.println("Fehler: " + e.getMessage());
        }
    }
    byte[] hashPassword(String password, byte[] salt, int work)
      throws NoSuchAlgorithmException, InvalidKeySpecException {
        SecretKeyFactory skf = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA512");
        PBEKeySpec spec = new PBEKeySpec(password.toCharArray(), salt, work, 512);
        SecretKey key = skf.generateSecret(spec);
        spec.clearPassword();
        return key.getEncoded();
    }
}
