package smarthome.arduino.impl;

import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import smarthome.arduino.utils.Constants;
import smarthome.arduino.utils.Logger;
import smarthome.db.DBManager;

import com.pi4j.io.serial.Serial;
import com.pi4j.io.serial.SerialDataEvent;
import com.pi4j.io.serial.SerialDataListener;

public class Controller implements SerialDataListener {

  private static final String TAG = "Controller";

  private Serial serial = null;

  private Map<String, Device> devices = new HashMap<String, Device>();

  public void init() {
    List<Device> devs = DBManager.getObjects("Devices.getAll", Device.class, null);
    synchronized (devices) {
      for (Device d : devs) {
        devices.put(d.getUid(), d);
      }
    }
    /*try {
      serial = SerialFactory.createInstance();
      if (serial.isOpen()) {
        serial.close();
      }
      serial.open(Serial.DEFAULT_COM_PORT, 115200);
      serial.addListener(this);
    } catch (Exception e) {
      Logger.error(TAG, "Serial error!", e);
    }*/
  }

  public void close() {
    if (serial != null) {
      try {
        serial.removeListener(this);
        serial.close();
        serial = null;
      } catch (Exception e) {
        Logger.error(TAG, "Serial error!", e);
      }
    }
    synchronized (devices) {
      for (Device device : devices.values()) {
        device.stopRunning();
      }
      devices.clear();
    }
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
      packet = new Packet(data.getBytes(Constants.CHARSET_NAME));
    } catch (UnsupportedEncodingException e) {
      Logger.error(TAG, "Error getting bytes!", e);
      return;
    }
    Logger.debug(TAG, "Packet recieved: " + packet);
    String uid = packet.getUid();
    Device device;
    synchronized (devices) {
      device = devices.get(uid);
      if (device == null) {
        if (packet.getType() == Packet.PACKET_TYPE_DEVICE_ADD) {
          device = new Device();
          device.setUid(uid);
          device.setController(this);
          device.startRunning();
          devices.put(uid, device);
        } else {
          Logger.debug(TAG, "Unknown device and packet is not DEVICE_ADD! Skip...");
          return;
        }
      }
    }
    device.newPacketRecieved(packet);
  }

}
