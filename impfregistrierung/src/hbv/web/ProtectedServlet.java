package hbv.web;
import jakarta.servlet.*;
import jakarta.servlet.http.*;
import java.io.*;

public class ProtectedServlet extends HttpServlet {
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
      throws ServletException, IOException {
        HttpSession session = req.getSession(false);
        resp.setContentType("text/plain");
        PrintWriter out = resp.getWriter();
        if(session != null && session.getAttribute("userId") != null){
            out.println("Du bist angemeldet als: " + session.getAttribute("userEmail"));
        } else {
            resp.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            out.println("Nicht autorisiert. Bitte melde Dich an.");
        }
    }
}
