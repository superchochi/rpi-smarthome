package smarthome.arduino.impl;

import smarthome.arduino.DeviceException;
import smarthome.arduino.Function;

public class FunctionImpl implements Function {

	private DeviceImpl device;
	private String uid;
	private byte type;

	public String getUid() {
		return uid;
	}

	public byte getType() {
		return type;
	}

	public Object getValue() throws DeviceException {
		return device.getFunctionValue(uid);
	}

	public void setValue(Object value) throws DeviceException {
		device.setFunctionValue(uid, value);
	}

	public Object[] getStatisticValues(long from, long to) {
		return device.getFunctionStatisticValues(uid, from, to);
	}

	protected void setDevice(DeviceImpl device) {
		this.device = device;
	}

}
