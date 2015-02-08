package smarthome.arduino.impl;

import com.pi4j.io.serial.Serial;
import com.pi4j.io.serial.SerialDataEvent;
import com.pi4j.io.serial.SerialDataListener;
import com.pi4j.io.serial.SerialFactory;

import smarthome.arduino.Controller;
import smarthome.arduino.Device;
import smarthome.arduino.DeviceException;

public class ControllerImpl implements Controller, SerialDataListener {

  private Serial serial;

  protected void init() {
    serial = SerialFactory.createInstance();
    if (serial.isOpen()) {
      serial.close();
    }
    serial.open(Serial.DEFAULT_COM_PORT, 115200);
    serial.addListener(this);
  }

  protected void close() {
    serial.removeListener(this);
    serial.close();
    serial = null;
  }

  public Device[] getDevices() {
    // TODO Auto-generated method stub
    return null;
  }

  public void setDeviceFunctionValue(String deviceUid, String functionUid, Object value) throws DeviceException {
    // TODO Auto-generated method stub

  }

  public Object getDeviceFunctionValue(String deviceUid, String functionUid) throws DeviceException {
    // TODO Auto-generated method stub
    return null;
  }

  public boolean isDeviceOnline(String deviceUid) throws DeviceException {
    // TODO Auto-generated method stub
    return false;
  }

  public Object[] getDeviceFunctionStatisticValues(String deviceUid, String functionUid, long from, long to) {
    // TODO Auto-generated method stub
    return null;
  }

  public void removeDevice(String deviceUid) {
    // TODO Auto-generated method stub

  }

  public void dataReceived(SerialDataEvent event) {
    String data = event.getData();
    processData(data);
  }

  private void processData(String data) {

  }

}
