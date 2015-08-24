package smarthome.webapp.impl;

import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;

import smarthome.arduino.api.Controller;
import smarthome.arduino.api.Device;
import smarthome.arduino.api.StatisticEntry;
import smarthome.db.DBManager;

@Path("/devices")
public class Devices {

  @GET
  @Produces({ MediaType.APPLICATION_JSON })
  public DeviceBean[] getDevices() {
    Controller controller = ServletContextListenerImpl.getController();
    if (controller != null) {
      Device[] devices = controller.getDevices();
      DeviceBean[] beans = new DeviceBean[devices.length];
      for (int i = 0; i < devices.length; i++) {
        beans[i] = new DeviceBean(devices[i]);
      }
      return beans;
    } else {
      return null;
    }
  }

  @GET
  @Produces({ MediaType.APPLICATION_JSON })
  @Path("/{deviceUid}")
  public DeviceBean getDevice(@PathParam("deviceUid") String deviceUid) {
    Controller controller = ServletContextListenerImpl.getController();
    if (controller != null) {
      Device[] devices = controller.getDevices();
      for (Device d : devices) {
        if (d.getUid().equals(deviceUid)) {
          return new DeviceBean(d);
        }
      }
    }
    return null;
  }

  @GET
  @Produces({ MediaType.APPLICATION_JSON })
  @Path("/{deviceUid}/{functionUid}")
  public StatisticBean[] getStats(@PathParam("deviceUid") String deviceUid,
      @PathParam("functionUid") String functionUid, @QueryParam("from") long from, @QueryParam("to") long to) {
    String queryName = "Stats.uid";
    Map<String, Object> params = new HashMap<String, Object>();
    params.put("functionUid", deviceUid + "_" + functionUid);
    if (from > 0 || to > 0) {
      params.put("from", from > 0 ? from : 0);
      params.put("to", to > 0 && to >= from ? to : Long.MAX_VALUE);
      queryName += "Time";
    }
    List<StatisticEntry> entries = DBManager.getObjects(queryName, StatisticEntry.class, params);
    StatisticBean[] beans = new StatisticBean[entries.size()];
    for (int i = 0; i < entries.size(); i++) {
      beans[i] = new StatisticBean(entries.get(i));
    }
    return beans;
  }

  @GET
  @Produces({ MediaType.APPLICATION_JSON })
  @Path("/{deviceUid}/{functionUid}/{criteria}")
  public Double getStatsCriteria(@PathParam("deviceUid") String deviceUid,
      @PathParam("functionUid") String functionUid, @PathParam("criteria") String criteria,
      @QueryParam("from") long from, @QueryParam("to") long to) {
    String queryName = "Stats.uid";
    Map<String, Object> params = new HashMap<String, Object>();
    params.put("functionUid", deviceUid + "_" + functionUid);
    if (from > 0 || to > 0) {
      params.put("from", from > 0 ? from : 0);
      params.put("to", to > 0 && to >= from ? to : Long.MAX_VALUE);
      queryName += "Time";
    }
    Double result;
    if ("max".equalsIgnoreCase(criteria)) {
      queryName += "Max";
    } else if ("min".equalsIgnoreCase(criteria)) {
      queryName += "Min";
    } else if ("avg".equalsIgnoreCase(criteria)) {
      queryName += "Avg";
    }
    result = DBManager.getValue(queryName, params);
    return result;
  }

  @GET
  @Produces({ MediaType.APPLICATION_JSON })
  @Path("/{deviceUid}/{functionUid}/{interval}/{timestamp}")
  public Map<Long, Double> getStatsDay(@PathParam("deviceUid") String deviceUid,
      @PathParam("functionUid") String functionUid, @PathParam("timestamp") long timestamp,
      @PathParam("interval") String interval) {
    String queryName = "Stats.uidTimeAvg";
    Map<String, Object> params = new HashMap<String, Object>();
    params.put("functionUid", deviceUid + "_" + functionUid);
    int step;
    Map<Long, Double> result = new HashMap<Long, Double>();
    Calendar cal = Calendar.getInstance();
    cal.setTimeInMillis(timestamp);
    long finalTime;
    if (interval.equalsIgnoreCase("day")) {
      step = Calendar.HOUR_OF_DAY;
      cal.add(Calendar.DAY_OF_MONTH, 1);
      finalTime = cal.getTimeInMillis();
      cal.add(Calendar.DAY_OF_MONTH, -1);
    } else if (interval.equalsIgnoreCase("month")) {
      step = Calendar.DAY_OF_MONTH;
      cal.add(Calendar.MONTH, 1);
      finalTime = cal.getTimeInMillis();
      cal.add(Calendar.MONTH, -1);
    } else if (interval.equalsIgnoreCase("year")) {
      step = Calendar.MONTH;
      cal.add(Calendar.YEAR, 1);
      finalTime = cal.getTimeInMillis();
      cal.add(Calendar.YEAR, -1);
    } else {
      return result;
    }
    long from = cal.getTimeInMillis();
    long to;
    while (from < finalTime) {
      cal.add(step, 1);
      to = cal.getTimeInMillis();
      params.put("from", from);
      params.put("to", to);
      Double value = DBManager.getValue(queryName, params);
      result.put(from, value);
      from = to;
    }
    return result;
  }

  @PUT
  @Path("/{deviceUid}/setName/{name}")
  public boolean setDeviceName(@PathParam("deviceUid") String deviceUid, @PathParam("name") String name) {
    Controller controller = ServletContextListenerImpl.getController();
    if (controller != null) {
      Device d = controller.getDevice(deviceUid);
      if (d != null) {
        d.setName(name);
        return true;
      }
    }
    return false;
  }

}
