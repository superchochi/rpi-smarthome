package smarthome.db;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

@Entity
public class LogEntity {

	private static final String LINE_SEPARATOR = System
			.getProperty("line.separator");

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private int id;

	private byte sensorType;
	private String deviceUid;
	private double value;

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public byte getSensorType() {
		return sensorType;
	}

	public void setSensorType(byte sensorType) {
		this.sensorType = sensorType;
	}

	public String getDeviceUid() {
		return deviceUid;
	}

	public void setDeviceUid(String deviceUid) {
		this.deviceUid = deviceUid;
	}

	public double getValue() {
		return value;
	}

	public void setValue(double value) {
		this.value = value;
	}

	@Override
	public String toString() {
		return ("sensorType: " + sensorType + LINE_SEPARATOR + "deviceUid: "
				+ deviceUid + LINE_SEPARATOR + "value: " + value);
	}

}
