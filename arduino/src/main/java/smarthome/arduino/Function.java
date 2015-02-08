package smarthome.arduino;

public interface Function {

	public String getUid();

	public byte getType();

	public Object getValue() throws DeviceException;

	public void setValue(Object value) throws DeviceException;

	public Object[] getStatisticValues(long from, long to);

}
