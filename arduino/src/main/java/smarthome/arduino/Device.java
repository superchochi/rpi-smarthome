package smarthome.arduino;

public interface Device {

  public String getUid();

  public Function[] getFunctions();

  public boolean isOnline() throws DeviceException;

  public void setFunctionValue(String functionUid, Object value) throws DeviceException;

  public Object getFunctionValue(String functionUid);

  public Object[] getFunctionStatisticValues(String functionUid, long from, long to);

  public void refresh() throws DeviceException;

  public boolean isInitialized();

}
