package smarthome.arduino.api;

public interface Controller {

  public void init();

  public void close();

  public Device getDevice(String deviceUid);

  public Device[] getDevices();

  public void removeDevice(String deviceUid);

}
