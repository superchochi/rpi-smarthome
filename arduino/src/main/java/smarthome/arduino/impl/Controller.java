package smarthome.arduino.impl;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import smarthome.arduino.utils.Logger;
import smarthome.db.DBManager;

import com.pi4j.io.serial.Serial;
import com.pi4j.io.serial.SerialDataEvent;
import com.pi4j.io.serial.SerialDataListener;
import com.pi4j.io.serial.SerialFactory;

public class Controller implements SerialDataListener, Runnable {

  private static final String TAG = "Controller";

  private Serial serial = null;

  private Map<String, Device> devices = new HashMap<String, Device>();

  private Thread thr = null;
  private volatile boolean running = false;
  private LinkedList<Byte> queuedBytes = new LinkedList<Byte>();

  public void init() {
    thr = new Thread(this, "ControllerThread");
    running = true;
    thr.start();

    DBManager.open();
    List<Device> devs = DBManager.getObjects("Devices.getAll", Device.class, null);
    synchronized (devices) {
      for (Device d : devs) {
        devices.put(d.getUid(), d);
        d.startRunning();
        Logger.info(TAG, "Device loaded: " + d.getUid());
      }
    }
    try {
      serial = SerialFactory.createInstance();
      if (serial.isOpen()) {
        serial.close();
      }
      serial.open(Serial.DEFAULT_COM_PORT, 115200);
      serial.addListener(this);
    } catch (Throwable e) {
      Logger.error(TAG, "Serial error!", e);
    }
  }

  public void close() {
    if (serial != null) {
      try {
        serial.removeListener(this);
        if (serial.isOpen()) {
          serial.close();
        }
        serial = null;
      } catch (Exception e) {
        Logger.error(TAG, "Serial error!", e);
      }
    }
    synchronized (devices) {
      for (Device d : devices.values()) {
        d.stopRunning();
        Logger.debug(TAG, "Device stopped: " + d.getUid());
      }
      devices.clear();
    }
    DBManager.close();
    running = false;
    synchronized (queuedBytes) {
      queuedBytes.notify();
    }
    try {
      thr.join();
    } catch (InterruptedException e) {
      Logger.error(TAG, "Error waiting thread to finish!", e);
    }
    thr = null;
  }

  public Device[] getDevices() {
    synchronized (devices) {
      return devices.values().toArray(new Device[0]);
    }
  }

  public Device getDevice(String uid) {
    synchronized (devices) {
      return devices.get(uid);
    }
  }

  public void removeDevice(String deviceUid) {
    // TODO Auto-generated method stub

  }

  public void dataReceived(SerialDataEvent event) {
    String data = event.getData();
    Logger.debug(TAG, "Data received: " + data.length());
    synchronized (queuedBytes) {
      boolean notify = queuedBytes.size() < Packet.PACKET_LENGTH;
      for (int i = 0; i < data.length(); i++) {
        queuedBytes.addLast((byte) data.charAt(i));
      }
      if (notify && queuedBytes.size() >= Packet.PACKET_LENGTH) {
        queuedBytes.notify();
      }
    }
  }

  public void run() {
    try {
      byte[] packetBytes;
      while (running) {
        Packet packet;
        synchronized (queuedBytes) {
          if (queuedBytes.size() < Packet.PACKET_LENGTH) {
            try {
              queuedBytes.wait();
            } catch (InterruptedException e) {
              Logger.error(TAG, "Error waiting on queuedBytes!", e);
            }
            if (!running) {
              break;
            }
          }
          packetBytes = new byte[Packet.PACKET_LENGTH];
          for (int i = 0; i < packetBytes.length; i++) {
            packetBytes[i] = queuedBytes.removeFirst();
          }
          try {
            packet = new Packet(packetBytes);
          } catch (Exception e) {
            Logger.error(TAG, "Error parsing packet! Remove bytes from buffer until valid byte for type occurs", e);
            while (!queuedBytes.isEmpty() && !Packet.isValidType(queuedBytes.getFirst())) {
              queuedBytes.removeFirst();
            }
            continue;
          }
        }

        Logger.debug(TAG, "New packet: " + packet);
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
              continue;
            }
          }
        }
        Logger.debug(TAG, "Packet added to device.");
        device.newPacketRecieved(packet);
      }
    } catch (Throwable t) {
      Logger.error(TAG, "Controller thread ended unexpected!", t);
    }
  }

}
