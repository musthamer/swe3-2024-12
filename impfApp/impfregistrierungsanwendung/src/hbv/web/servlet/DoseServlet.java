package hbv.web.servlet;

import hbv.service.DoseService;
import jakarta.servlet.*;
import jakarta.servlet.http.*;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.SQLException;

public class DoseServlet extends HttpServlet {
    private DoseService doseService;

    @Override
    public void init() throws ServletException {
        doseService = new DoseService();
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        HttpSession session = req.getSession(false);
        if(session == null || !"center".equalsIgnoreCase((String)session.getAttribute("userRole"))) {
            resp.sendRedirect("login");
            return;
        }
        String centerName = (String) session.getAttribute("center");
        String vaccine = req.getParameter("vaccine");
        String quantityStr = req.getParameter("quantity");
        int quantity = 0;
        try {
            quantity = Integer.parseInt(quantityStr);
        } catch(NumberFormatException e) {
            quantity = 0;
        }
        resp.setContentType("text/html;charset=UTF-8");
        PrintWriter out = resp.getWriter();
        try {
            doseService.addDoses(vaccine, quantity, centerName);
            int currentStock = doseService.getStockForVaccine(centerName, vaccine);
            out.println("<html><head><title>Dosen hinzugefügt</title></head><body>");
            out.println("<h2>Dosen erfolgreich hinzugefügt.</h2>");
            out.println("<p>Aktueller Bestand für " + vaccine + ": " + currentStock + "</p>");
            if(currentStock < 10) {
                out.println("<p style='color:red;'>Warnung: Der Impfstoffbestand ist niedrig!</p>");
            }
            out.println("<p><a href='centerDashboard'>Zurück zum Dashboard</a></p>");
            out.println("</body></html>");
        } catch(SQLException e) {
            out.println("<html><body>");
            out.println("<h2>Fehler beim Hinzufügen der Dosen: " + e.getMessage() + "</h2>");
            out.println("<p><a href='centerDashboard'>Zurück</a></p>");
            out.println("</body></html>");
        }
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        resp.sendRedirect("centerDashboard");
    }
}
