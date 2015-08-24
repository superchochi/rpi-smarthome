package smarthome.arduino.api;

public interface Device {

  public String getUid();

  public Function[] getFunctions();

  public boolean isOnline() throws DeviceException;

  public boolean isInitialized();

  public String getName();

  public void setName(String name);

  public double getFunctionValue(String functionUid);

  public void setFunctionValue(String functionUid, double value) throws DeviceException;

}
