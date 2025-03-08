package hbv.web;
import jakarta.servlet.*;
import jakarta.servlet.http.*;
import java.util.logging.Logger;

public class MyContextListener implements ServletContextListener {
    @Override
    public void contextInitialized(ServletContextEvent sce) {
        ServletContext ctx = sce.getServletContext();
        String redisServer = ctx.getInitParameter("redisserver");
        String redisPass = ctx.getInitParameter("redispassword");
        JedisAdapter.init(redisServer, 6379, redisPass);
        Logger.getLogger("MyContextListener").info("JedisAdapter initialisiert.");
    }
    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        JedisAdapter.destroy();
    }
}
