package smarthome.webapp.impl;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import smarthome.arduino.impl.Controller;
import smarthome.arduino.impl.Device;
import smarthome.arduino.impl.StatisticEntry;
import smarthome.db.DBManager;

@Path("/devices")
public class Devices {

  @GET
  @Produces({ MediaType.APPLICATION_JSON })
  public Device[] getDevices() {
    Controller controller = ServletContextListenerImpl.getController();
    if (controller != null) {
      Device[] devices = controller.getDevices();
      return devices;
    } else {
      return null;
    }
  }

  @GET
  @Produces({ MediaType.APPLICATION_JSON })
  @Path("/{deviceUid}")
  public Device getDevice(@PathParam("deviceUid") String deviceUid) {
    Controller controller = ServletContextListenerImpl.getController();
    if (controller != null) {
      Device[] devices = controller.getDevices();
      for (Device d : devices) {
        if (d.getUid().equals(deviceUid)) {
          return d;
        }
      }
    }
    return null;
  }

  @GET
  @Produces({ MediaType.APPLICATION_JSON })
  @Path("/{deviceUid}/{functionUid}")
  public StatisticEntry[] getStats(@PathParam("deviceUid") String deviceUid,
      @PathParam("functionUid") String functionUid) {
    Map<String, Object> params = new HashMap<String, Object>();
    params.put("functionUid", deviceUid + "_" + functionUid);
    List<StatisticEntry> entries = DBManager.getObjects("Stats.getByFunctionUid", StatisticEntry.class, params);
    return entries.toArray(new StatisticEntry[0]);
  }

}
