package listener;

import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;
import jakarta.servlet.annotation.WebListener;

@WebListener
public class AppContextListener implements ServletContextListener {
    @Override public void contextInitialized(ServletContextEvent e) {
        System.out.println("[ELRS] Application started.");
    }
    @Override public void contextDestroyed(ServletContextEvent e) {
        System.out.println("[ELRS] Application stopped.");
    }
}