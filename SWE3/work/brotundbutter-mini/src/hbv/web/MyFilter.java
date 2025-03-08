package hbv.web;

import jakarta.servlet.*;
import jakarta.servlet.http.*;

public class MyFilter extends HttpFilter {
  ServletContext ctx;

  public void init(FilterConfig config) throws ServletException {
    ctx = config.getServletContext();
  }

  public void doFilter(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
      throws java.io.IOException, ServletException {

    // ctx.log("in doFilter");
    // String forwardedFor = hsr.getHeader("X-Forwarded-For");
    // String requestURL = "" + hsr.getRequestURL();

    chain.doFilter(request, response);
  }

  public void destroy() {}
}
