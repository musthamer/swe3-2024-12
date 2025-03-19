package hbv.web.servlet;

import jakarta.servlet.*;
import jakarta.servlet.http.*;
import java.io.*;

public class AdminDashboardServlet extends HttpServlet {
    private String readTemplate(String filename, HttpServletRequest req) throws IOException {
         BufferedReader reader = new BufferedReader(
            new FileReader(req.getServletContext().getRealPath("/static/" + filename))
         );
         StringBuilder sb = new StringBuilder();
         String line;
         while((line = reader.readLine()) != null) {
             sb.append(line).append(System.lineSeparator());
         }
         reader.close();
         return sb.toString();
    }
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
         HttpSession session = req.getSession(false);
         if(session == null || !"admin".equalsIgnoreCase((String)session.getAttribute("userRole"))) {
             resp.sendRedirect("login");
             return;
         }
         String header = readTemplate("header.html", req).replace("<!-- Title placeholder -->", "Admin-Dashboard");
         String footer = readTemplate("footer.html", req);
         resp.setContentType("text/html;charset=UTF-8");
         PrintWriter out = resp.getWriter();
         out.println(header);
         out.println("<h2>Admin-Dashboard</h2>");
         out.println("<p><a href='center'>Neues Impfzentrum anlegen</a></p>");
         out.println(footer);
    }
}
