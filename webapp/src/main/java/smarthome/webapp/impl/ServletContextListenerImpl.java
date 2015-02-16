package smarthome.webapp.impl;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;

@WebListener
public class ServletContextListenerImpl implements ServletContextListener {

	public void contextDestroyed(ServletContextEvent e) {
	  System.out.println("@@@@@@@@@@@@@@@@@@@");
	}

	public void contextInitialized(ServletContextEvent e) {
	  System.out.println("###################");
	}

}
