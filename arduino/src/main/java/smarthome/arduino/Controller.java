package smarthome.arduino;

public interface Controller {

  public Device[] getDevices();

  public void removeDevice(String deviceUid);

}
