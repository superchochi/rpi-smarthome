package smarthome.webapp.impl;

import java.util.LinkedList;
import java.util.List;

import smarthome.arduino.api.DeviceException;
import smarthome.arduino.api.Device;
import smarthome.arduino.api.Function;

public class DeviceBean {

  private String uid;
  private String name;
  private boolean online;
  private boolean initialized;
  private List<FunctionBean> functions;

  public DeviceBean() {
  }

  public DeviceBean(Device device) {
    uid = device.getUid();
    name = device.getName();
    try {
      online = device.isOnline();
    } catch (DeviceException e) {
      online = false;
    }
    initialized = device.isInitialized();
    Function[] functions = device.getFunctions();
    this.functions = new LinkedList<FunctionBean>();
    for (Function f : functions) {
      this.functions.add(new FunctionBean(f));
    }
  }

  public String getUid() {
    return uid;
  }

  public void setUid(String uid) {
    this.uid = uid;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public boolean isOnline() {
    return online;
  }

  public void setOnline(boolean online) {
    this.online = online;
  }

  public boolean isInitialized() {
    return initialized;
  }

  public void setInitialized(boolean initialized) {
    this.initialized = initialized;
  }

  public List<FunctionBean> getFunctions() {
    return functions;
  }

  public void setFunctions(List<FunctionBean> functions) {
    this.functions = functions;
  }

}
