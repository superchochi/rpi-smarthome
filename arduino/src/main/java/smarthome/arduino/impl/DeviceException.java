package smarthome.arduino.impl;

public class DeviceException extends Exception {

	private static final long serialVersionUID = 1L;

	public DeviceException(String msg) {
		super(msg);
	}

	public DeviceException(Throwable t) {
		super(t);
	}

}
