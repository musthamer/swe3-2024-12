package hbv.web;

import jakarta.servlet.*;

public class MyContextListener implements ServletContextListener {
  ServletContext ctx;

  public void contextInitialized(ServletContextEvent servletContextEvent) {
    ctx = servletContextEvent.getServletContext();

    String redispassword = ctx.getInitParameter("redispassword");
    String redisserver = ctx.getInitParameter("redisserver");

    JedisAdapter.init(redisserver, 6379, redispassword);
  }

  public void contextDestroyed(ServletContextEvent servletContextEvent) {
    JedisAdapter.destroy();
  }
}
