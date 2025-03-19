package hbv.web.servlet;
import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;
import java.util.logging.Logger;
public class MyContextListener implements ServletContextListener {
    @Override
    public void contextInitialized(ServletContextEvent sce) {
        Logger.getLogger("MyContextListener").info("Anwendung gestartet – Ressourcen initialisiert.");
    }
    @Override
    public void contextDestroyed(ServletContextEvent sce) { }
}
