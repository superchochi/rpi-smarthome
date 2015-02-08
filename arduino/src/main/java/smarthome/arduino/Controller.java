package smarthome.arduino;

public interface Controller {

	public Device[] getDevices();

	public void setDeviceFunctionValue(String deviceUid, String functionUid,
			Object value) throws DeviceException;

	public Object getDeviceFunctionValue(String deviceUid, String functionUid)
			throws DeviceException;

	public boolean isDeviceOnline(String deviceUid) throws DeviceException;

	public Object[] getDeviceFunctionStatisticValues(String deviceUid,
			String functionUid, long from, long to);

	public void removeDevice(String deviceUid);

}
