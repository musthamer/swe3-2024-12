package hbv.web;

import jakarta.servlet.*;
import java.util.concurrent.*;

public class MyContextListener implements ServletContextListener, ServletRequestListener {
  ScheduledExecutorService executor;
  ServletContext ctx;

  public void contextInitialized(ServletContextEvent servletContextEvent) {
    ctx = servletContextEvent.getServletContext();
    executor = new ScheduledThreadPoolExecutor(1);
    executor.scheduleAtFixedRate(new MonitorStateLogger(), 0, 10, TimeUnit.SECONDS);

    String redispassword = ctx.getInitParameter("redispassword");
    String redisserver = ctx.getInitParameter("redisserver");

    JedisAdapter.init(redisserver, 6379, redispassword);
  }

  public void contextDestroyed(ServletContextEvent servletContextEvent) {
    executor.shutdownNow();
    JedisAdapter.destroy();
  }

  public void requestInitialized(ServletRequestEvent evt) {}

  public void requestDestroyed(ServletRequestEvent evt) {}
}

class MonitorStateLogger implements Runnable {
  int count;

  public void run() {}
}
