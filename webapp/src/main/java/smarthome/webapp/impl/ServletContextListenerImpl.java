package smarthome.webapp.impl;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;

import smarthome.arduino.api.Controller;
import smarthome.arduino.impl.ControllerImpl;
import smarthome.arduino.utils.Logger;

@WebListener
public class ServletContextListenerImpl implements ServletContextListener {

  private static Controller controller = null;

  public void contextDestroyed(ServletContextEvent e) {
    if (controller != null) {
      controller.close();
      controller = null;
    }
    Logger.close();
  }

  public void contextInitialized(ServletContextEvent e) {
    Logger.open();
    if (controller == null) {
      controller = new ControllerImpl();
      // controller = new ControllerWinImpl();
      controller.init();
    }
  }

  public static Controller getController() {
    return controller;
  }

}
