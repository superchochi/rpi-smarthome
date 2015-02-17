package smarthome.webapp.impl;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;

import smarthome.arduino.impl.Controller;

@WebListener
public class ServletContextListenerImpl implements ServletContextListener {

  private static Controller controller = null;

  public void contextDestroyed(ServletContextEvent e) {
    if (controller != null) {
      controller.close();
      controller = null;
    }
  }

  public void contextInitialized(ServletContextEvent e) {
    if (controller == null) {
      controller = new Controller();
      controller.init();
    }
  }

  public static Controller getController() {
    return controller;
  }

}
