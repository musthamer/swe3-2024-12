package hbv.web;

import jakarta.servlet.*;
import jakarta.servlet.http.*;
import java.io.*;

public class ConfirmationServlet extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
      throws ServletException, IOException {
        String bookingId = req.getParameter("booking_id");
        resp.setContentType("application/pdf");
        PrintWriter out = resp.getWriter();
        out.println("%PDF-1.4");
        out.println("1 0 obj");
        out.println("<< /Type /Catalog /Pages 2 0 R >>");
        out.println("endobj");
        out.println("2 0 obj");
        out.println("<< /Type /Pages /Kids [3 0 R] /Count 1 >>");
        out.println("endobj");
        out.println("3 0 obj");
        out.println("<< /Type /Page /Parent 2 0 R /MediaBox [0 0 300 144] /Contents 4 0 R >>");
        out.println("endobj");
        out.println("4 0 obj");
        out.println("<< /Length 44 >>");
        out.println("stream");
        out.println("BT");
        out.println("/F1 12 Tf");
        out.println("100 100 Td");
        out.println("(Bestätigung für Buchung " + bookingId + " - QR Code Placeholder) Tj");
        out.println("ET");
        out.println("endstream");
        out.println("endobj");
        out.println("xref");
        out.println("0 5");
        out.println("0000000000 65535 f");
        out.println("0000000010 00000 n");
        out.println("0000000063 00000 n");
        out.println("0000000118 00000 n");
        out.println("0000000213 00000 n");
        out.println("trailer");
        out.println("<< /Size 5 /Root 1 0 R >>");
        out.println("startxref");
        out.println("300");
        out.println("%%EOF");
    }
}
