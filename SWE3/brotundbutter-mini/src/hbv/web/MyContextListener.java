package hbv.web;

import jakarta.servlet.*;
import java.util.concurrent.*;

public class MyContextListener implements ServletContextListener, ServletRequestListener {
  ScheduledExecutorService executor;
  ServletContext ctx;
  MonitorStateLogger monitorStateLogger;

  public void contextInitialized(ServletContextEvent servletContextEvent) {
    ctx = servletContextEvent.getServletContext();
    ctx.log("contextInitialized");
    executor = new ScheduledThreadPoolExecutor(1);
    monitorStateLogger = new MonitorStateLogger(ctx);
    executor.scheduleAtFixedRate(monitorStateLogger, 0, 1, TimeUnit.SECONDS);
  }

  public void contextDestroyed(ServletContextEvent servletContextEvent) {
    executor.shutdownNow();
    ctx.log("contextDestroyed");
  }

  public void requestInitialized(ServletRequestEvent evt) {}

  // wieso muss diese Methode nicht implementiert werden?
  public void requestDestroyed(ServletRequestEvent evt) {}
}

class MonitorStateLogger implements Runnable {
  int count;
  ServletContext ctx;

  MonitorStateLogger(ServletContext ctx) {
    this.ctx = ctx;
  }

  public void run() {
    ctx.log("Info:" + (count++) + " java:" + System.getProperty("java.version"));
  }
}
