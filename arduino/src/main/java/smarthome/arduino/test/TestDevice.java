package smarthome.arduino.test;

import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Random;

import smarthome.arduino.impl.Controller;
import smarthome.arduino.impl.Function;
import smarthome.arduino.impl.Packet;
import smarthome.arduino.utils.Logger;

import com.pi4j.io.serial.SerialDataEvent;

public class TestDevice implements Runnable {

  private static final String TAG = "TestDevice";

  private Random rand = new Random(System.currentTimeMillis());
  private Controller controller = null;
  private byte[] uid = null;
  private Map<byte[], Byte> functions = new HashMap<byte[], Byte>();

  private Thread thr;
  private volatile boolean running = false;

  public TestDevice(Controller controller, String uid) {
    this.controller = controller;
    try {
      this.uid = uid.getBytes();
    } catch (Exception e) {
      Logger.error(TAG, "Charset name error!", e);
    }
  }

  public void startRunning() {
    running = true;
    thr = new Thread(this, "TestDeviceThread");
    thr.start();
  }

  public void stopRunning(boolean interrupt) {
    running = false;
    try {
      if (interrupt) {
        thr.interrupt();
      }
      thr.join();
    } catch (Exception e) {
      Logger.error(TAG, "Error joining test device thread!", e);
    }
  }

  public void addFunction(byte functionType, String functionUid) {
    try {
      byte[] bytes = new byte[functionUid.length()];
      for (int i = 0; i < bytes.length; i++) {
        bytes[i] = (byte) functionUid.charAt(i);
      }
      functions.put(bytes, functionType);
    } catch (Exception e) {
      Logger.error(TAG, "Charset name error!", e);
    }
  }

  public void run() {
    addDevice();
    while (running) {
      try {
        Thread.sleep(500);
      } catch (Exception e) {
        Logger.error(TAG, "Error sleeping!", e);
        break;
      }
      updateFunctionsValue();
      //break;
    }
  }

  private void addDevice() {
    List<byte[]> packets = new LinkedList<byte[]>();
    byte[] packet = new byte[Packet.PACKET_LENGTH];
    for (int i = 0; i < Packet.PACKET_UID_LENGTH; i++) {
      packet[i] = uid[i];
    }
    packet[Packet.PACKET_TYPE_INDEX] = Packet.PACKET_TYPE_DEVICE_ADD;
    packet[Packet.PACKET_IS_LAST_INDEX] = 0;
    List<Byte> functionsData = new LinkedList<Byte>();
    for (Iterator<byte[]> it = functions.keySet().iterator(); it.hasNext();) {
      byte[] fUid = it.next();
      byte fType = functions.get(fUid);
      functionsData.add(Packet.PACKET_FUNCTION_DATA);
      functionsData.add(fType);
      functionsData.add((byte) fUid.length);
      for (byte b : fUid) {
        functionsData.add(b);
      }
      switch (fType) {
      case Function.FUNCTION_TYPE_TEMPERATURE:
        functionsData.add(Function.VALUE_TYPE_BYTE);
        functionsData.add((byte) 10);
        break;
      case Function.FUNCTION_TYPE_HUMIDITY:
        functionsData.add(Function.VALUE_TYPE_BYTE);
        functionsData.add((byte) 60);
        break;
      case Function.FUNCTION_TYPE_BATTERY:
        functionsData.add(Function.VALUE_TYPE_BYTE);
        functionsData.add((byte) 100);
        break;
      }
    }
    int j = 0;
    for (int i = 0; i < functionsData.size(); i++) {
      byte b = functionsData.get(i);
      packet[Packet.PACKET_DATA_START_INDEX + j++] = b;
      if (j == Packet.PACKET_DATA_LENGTH) {
        j = 0;
        packets.add(packet);
        if (i == functionsData.size() - 1) {
          packet[Packet.PACKET_IS_LAST_INDEX] = 1;
        } else {
          packet[Packet.PACKET_IS_LAST_INDEX] = 0;
          packet = new byte[Packet.PACKET_LENGTH];
          for (int k = 0; k < Packet.PACKET_UID_LENGTH; k++) {
            packet[k] = uid[k];
          }
          packet[Packet.PACKET_TYPE_INDEX] = Packet.PACKET_TYPE_SERIAL;
          packet[Packet.PACKET_IS_LAST_INDEX] = 0;
        }
      }
    }
    if (packet[Packet.PACKET_IS_LAST_INDEX] == 0) {
      for (int i = j; i < Packet.PACKET_DATA_LENGTH; i++) {
        packet[Packet.PACKET_DATA_START_INDEX + i] = 0;
      }
      packet[Packet.PACKET_IS_LAST_INDEX] = 1;
      packets.add(packet);
    }

    for (byte[] b : packets) {
      try {
        char[] chars = new char[b.length];
        for (int i = 0; i < chars.length; i++) {
          chars[i] = (char) b[i];
        }
        controller.dataReceived(new SerialDataEvent(new Object(), new String(chars)));
      } catch (Exception e) {
        Logger.error(TAG, "Charset name error!", e);
      }
    }
  }

  private void updateFunctionsValue() {
    for (Iterator<byte[]> it = functions.keySet().iterator(); it.hasNext();) {
      byte[] fUid = it.next();
      byte fType = functions.get(fUid);
      byte[] packet = new byte[Packet.PACKET_LENGTH];
      for (int i = 0; i < Packet.PACKET_UID_LENGTH; i++) {
        packet[i] = uid[i];
      }
      packet[Packet.PACKET_TYPE_INDEX] = Packet.PACKET_TYPE_FUNCTION_VALUE;
      packet[Packet.PACKET_IS_LAST_INDEX] = 1;
      int i = Packet.PACKET_DATA_START_INDEX;
      packet[i++] = fType;
      packet[i++] = (byte) fUid.length;
      for (int j = 0; j < fUid.length; j++) {
        packet[i++] = fUid[j];
      }
      switch (fType) {
      case Function.FUNCTION_TYPE_TEMPERATURE: {
        byte val = (byte) (rand.nextInt(100) - 50);
        packet[i++] = val;
        break;
      }
      case Function.FUNCTION_TYPE_HUMIDITY: {
        byte val = (byte) (rand.nextInt(100) - 50);
        packet[i++] = val;
        break;
      }
      case Function.FUNCTION_TYPE_BATTERY: {
        byte val = (byte) rand.nextInt(100);
        packet[i++] = val;
        break;
      }
      }
      while (i < Packet.PACKET_DATA_LENGTH) {
        packet[i++] = 0;
      }
      try {
        char[] chars = new char[packet.length];
        for (i = 0; i < chars.length; i++) {
          chars[i] = (char) packet[i];
        }
        controller.dataReceived(new SerialDataEvent(new Object(), new String(chars)));
      } catch (Exception e) {
        Logger.error(TAG, "Charset name error!", e);
      }
    }
  }

  public static void main(String[] args) {
    System.out.println((byte) 20);
  }
}
