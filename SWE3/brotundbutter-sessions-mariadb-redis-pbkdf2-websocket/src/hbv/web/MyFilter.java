package hbv.web;

import jakarta.servlet.*;
import jakarta.servlet.http.*;

/** <strong>MyFilter</strong> extends HttpFilter. */
public class MyFilter extends HttpFilter {
  ServletContext ctx;

  public void init(FilterConfig config) throws ServletException {
    ctx = config.getServletContext();
  }

  void log(String msg) {
    ctx.log(msg);
  }

  public void doFilter(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
      throws java.io.IOException, ServletException {

    // MyLogger.info("in doFilter");
    // String forwardedFor = hsr.getHeader("X-Forwarded-For");
    // String requestURL = "" + hsr.getRequestURL();

    chain.doFilter(request, response);
  }

  public void destroy() {}
}
