package smarthome.arduino.impl;

import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;

import smarthome.arduino.Controller;
import smarthome.arduino.Device;
import smarthome.arduino.utils.Logger;

import com.pi4j.io.serial.Serial;
import com.pi4j.io.serial.SerialDataEvent;
import com.pi4j.io.serial.SerialDataListener;
import com.pi4j.io.serial.SerialFactory;

public class ControllerImpl implements Controller, SerialDataListener {

  private static final String TAG = "Controller";

  private Serial serial;

  private Map<String, DeviceImpl> devices = new HashMap<String, DeviceImpl>();

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
    synchronized (devices) {
      for (DeviceImpl device : devices.values()) {
        device.stopRunning();
      }
      devices.clear();
    }
    serial.close();
    serial = null;
  }

  public Device[] getDevices() {
    synchronized (devices) {
      return devices.values().toArray(new Device[0]);
    }
  }

  public void removeDevice(String deviceUid) {
    // TODO Auto-generated method stub

  }

  public void dataReceived(SerialDataEvent event) {
    String data = event.getData();
    Packet packet;
    try {
      packet = new Packet(data.getBytes("Cp1252"));
    } catch (UnsupportedEncodingException e) {
      Logger.error(TAG, "Error getting bytes!", e);
      return;
    }
    Logger.debug(TAG, "Packet recieved: " + packet);
    String uid = packet.getUid();
    DeviceImpl device;
    synchronized (devices) {
      device = devices.get(uid);
      if (device == null) {
        if (packet.getType() == Packet.PACKET_TYPE_DEVICE_ADD) {
          device = new DeviceImpl();
          devices.put(uid, device);
        } else {
          Logger.debug(TAG, "Unknown device and packet is not DEVICE_ADD! Skip...");
        }
      }
    }
    device.setUid(uid);
    device.setController(this);
    device.startRunning();
    device.newPacketRecieved(packet);
  }

}
