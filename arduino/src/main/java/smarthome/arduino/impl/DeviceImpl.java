package smarthome.arduino.impl;

import smarthome.arduino.Device;
import smarthome.arduino.DeviceException;
import smarthome.arduino.Function;

public class DeviceImpl implements Device {

	private ControllerImpl controller;
	private String uid;
	private Function[] functions;

	public String getUid() {
		return uid;
	}

	public Function[] getFunctions() {
		return functions;
	}

	public boolean isOnline() throws DeviceException {
		return controller.isDeviceOnline(uid);
	}

	public void setFunctionValue(String functionUid, Object value)
			throws DeviceException {
		controller.setDeviceFunctionValue(uid, functionUid, value);
	}

	public Object getFunctionValue(String functionUid) throws DeviceException {
		return controller.getDeviceFunctionValue(uid, functionUid);
	}

	public Object[] getFunctionStatisticValues(String functionUid, long from,
			long to) {
		return controller.getDeviceFunctionStatisticValues(uid, functionUid,
				from, to);
	}

}
