package smarthome.webapp;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;

@WebListener
public class ServletContextListenerImpl implements ServletContextListener {

	public void contextDestroyed(ServletContextEvent e) {
	}

	public void contextInitialized(ServletContextEvent e) {
	}

}
