package smarthome.webapp.impl;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import smarthome.arduino.impl.Controller;
import smarthome.arduino.impl.Device;
import smarthome.arduino.utils.Constants;

@Path("/devices")
public class Devices {

  @GET
  @Produces({ MediaType.TEXT_HTML })
  public String getDevices() {
    Controller controller = ServletContextListenerImpl.getController();
    if (controller != null) {
      Device[] devices = controller.getDevices();
      StringBuffer buff = new StringBuffer();
      for (Device d : devices) {
        buff.append(d.toString()).append(Constants.LINE_SEPARATOR);
      }
      return buff.toString();
    } else {
      return null;
    }
  }

}
