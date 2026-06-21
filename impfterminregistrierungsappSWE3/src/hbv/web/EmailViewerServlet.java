package hbv.web;

import jakarta.servlet.*;
import jakarta.servlet.http.*;
import java.io.*;
import redis.clients.jedis.*;
import java.util.List;
import org.json.JSONObject;
import java.util.Base64;

import hbv.messaging.RedisEmailSender;

public class EmailViewerServlet extends HttpServlet {
  
  protected void doGet(HttpServletRequest request, HttpServletResponse response)
      throws IOException, ServletException {
      
      // PDF-Anhang direkt anzeigen, wenn ein PDF-Parameter übergeben wurde
      String pdfId = request.getParameter("pdf");
      if (pdfId != null && !pdfId.isEmpty()) {
          showPdfAttachment(request, response, pdfId);
          return;
      }
      
      response.setContentType("text/html");
      PrintWriter out = response.getWriter();
      
      out.println("<!DOCTYPE html>");
      out.println("<html><head><title>Gespeicherte E-Mails</title>");
      out.println("<style>body{font-family:Arial,sans-serif;margin:20px} .email{border:1px solid #ccc;padding:10px;margin-bottom:20px;} pre{background:#f5f5f5;padding:10px;} .attachment{margin-top:10px;padding:5px;background-color:#e0f7fa;}</style>");
      out.println("</head><body>");
      out.println("<h1>Gespeicherte E-Mails</h1>");

      try {
          Jedis jedis = JedisAdapter.getJedis();
          if (jedis != null) {
              String sessionId = request.getSession().getId();
              List<String> emails = jedis.lrange(RedisEmailSender.emailsKey(sessionId), 0, -1);
              
              if (emails.isEmpty()) {
                  out.println("<p>Keine E-Mails gefunden.</p>");
              } else {
                  int emailIndex = 0;
                  for (String emailJson : emails) {
                      out.println("<div class='email'>");
                      
                      // Versuchen, das JSON zu parsen
                      try {
                          JSONObject emailData = new JSONObject(emailJson);
                          out.println("<h3>An: " + emailData.optString("to") + "</h3>");
                          out.println("<h4>Betreff: " + emailData.optString("subject") + "</h4>");
                          out.println("<pre>" + emailData.optString("body") + "</pre>");
                          
                          // Prüfen auf Anhang
                          if (emailData.has("attachment")) {
                              JSONObject attachment = emailData.getJSONObject("attachment");
                              if (attachment.has("filename") && attachment.has("content")) {
                                  out.println("<div class='attachment'>");
                                  out.println("<p><strong>Anhang:</strong> " + attachment.optString("filename") + "</p>");
                                  out.println("<p><a href='?pdf=" + emailIndex + "' target='_blank'>Anhang anzeigen/herunterladen</a></p>");
                                  out.println("</div>");
                              }
                          }
                      } catch (Exception e) {
                          out.println("<pre>" + emailJson + "</pre>");
                      }
                      
                      out.println("</div>");
                      emailIndex++;
                  }
              }
              
              JedisAdapter.releaseJedis(jedis);
          } else {
              out.println("<p>Redis ist derzeit nicht verfügbar.</p>");
          }
      } catch (Exception e) {
          out.println("<p>Fehler beim Abrufen der E-Mails: " + e.getMessage() + "</p>");
          e.printStackTrace(out);
      }
      
      out.println("<p><a href='./'>Zurück zur Startseite</a></p>");
      out.println("</body></html>");
  }
  
  private void showPdfAttachment(HttpServletRequest request, HttpServletResponse response, String pdfIdStr)
      throws IOException, ServletException {
      
      try {
          int pdfId = Integer.parseInt(pdfIdStr);
          
          Jedis jedis = JedisAdapter.getJedis();
          if (jedis != null) {
              String sessionId = request.getSession().getId();
              List<String> emails = jedis.lrange(RedisEmailSender.emailsKey(sessionId), 0, -1);
              
              if (pdfId >= 0 && pdfId < emails.size()) {
                  String emailJson = emails.get(pdfId);
              JSONObject emailData = new JSONObject(emailJson);
                  
              if (emailData.has("attachment")) {
                  JSONObject attachment = emailData.getJSONObject("attachment");
                  if (attachment.has("content")) {
                      String base64Content = attachment.getString("content");
                          byte[] pdfData = Base64.getDecoder().decode(base64Content);
                          
                          // PDF-Daten ausgeben
                          response.setContentType("application/pdf");
                          response.setHeader("Content-Disposition", "inline; filename=impftermin.pdf");
                          response.setContentLength(pdfData.length);
                          
                          ServletOutputStream outputStream = response.getOutputStream();
                          outputStream.write(pdfData);
                          outputStream.flush();
                          return;
                      }
                  }
              }
              
              JedisAdapter.releaseJedis(jedis);
          }
      } catch (Exception e) {
          e.printStackTrace();
      }
      
      // Im Fehlerfall: Fehlermeldung anzeigen
      response.setContentType("text/html");
      PrintWriter out = response.getWriter();
      out.println("<!DOCTYPE html><html><body>");
      out.println("<h2>Fehler</h2>");
      out.println("<p>Der angeforderte PDF-Anhang konnte nicht gefunden werden.</p>");
      out.println("<p><a href='emails'>Zurück zur E-Mail-Übersicht</a></p>");
      out.println("</body></html>");
  }
} 