package smarthome.arduino.impl;

import java.util.LinkedList;
import java.util.List;

import smarthome.arduino.Device;
import smarthome.arduino.DeviceException;
import smarthome.arduino.Function;
import smarthome.arduino.utils.Constants;
import smarthome.arduino.utils.Logger;

public class DeviceImpl implements Device, Runnable {

  private static final String TAG = "Device";

  private ControllerImpl controller;
  private String uid;
  private List<FunctionImpl> functions = new LinkedList<FunctionImpl>();
  private boolean online;

  private Thread thr;
  private volatile boolean running;

  private LinkedList<Packet> packets = new LinkedList<Packet>();
  private Object lockPackets = new Object();

  protected void startRunning() {
    thr = new Thread(this, "DeviceThread");
    running = true;
    thr.start();
  }

  protected void stopRunning() {
    running = false;
    synchronized (lockPackets) {
      lockPackets.notify();
    }
  }

  public String getUid() {
    return uid;
  }

  public Function[] getFunctions() {
    return functions.toArray(new Function[0]);
  }

  public boolean isOnline() throws DeviceException {
    return online;
  }

  public void setFunctionValue(String functionUid, Object value) throws DeviceException {

  }

  public Object getFunctionValue(String functionUid) {
    for (Function f : functions) {
      if (f.getUid().equals(functionUid)) {
        return f.getValue();
      }
    }
    return null;
  }

  public Object[] getFunctionStatisticValues(String functionUid, long from, long to) {
    for (Function f : functions) {
      if (f.getUid().equals(functionUid)) {
        return f.getStatisticValues(from, to);
      }
    }
    return null;
  }

  public void refresh() throws DeviceException {
    // TODO Auto-generated method stub

  }

  protected void setUid(String uid) {
    this.uid = uid;
  }

  protected void setController(ControllerImpl controller) {
    this.controller = controller;
  }

  protected void newPacketRecieved(Packet packet) {
    synchronized (lockPackets) {
      packets.addLast(packet);
      if (packets.size() == 1) {
        lockPackets.notify();
      }
    }
  }

  public void run() {
    List<Packet> packets0 = new LinkedList<Packet>();
    Packet p;
    while (running) {
      synchronized (lockPackets) {
        try {
          if (packets.isEmpty()) {
            lockPackets.wait();
          }
          if (!running) {
            break;
          }
        } catch (InterruptedException e) {
          Logger.error(TAG, "Error waiting for packets!", e);
          break;
        }
        p = packets.removeFirst();
        if (p.getType() == Packet.PACKET_TYPE_SERIAL) {
          if (packets0.isEmpty()) {
            Logger.warning(TAG, "Serial packet came but no starting one was found! Skip packet...");
            continue;
          }
        } else {
          if (!packets0.isEmpty()) {
            Logger.warning(TAG, "Starting packet came before serial end! Remove old packets...");
            packets0.clear();
          }
        }
        packets0.add(p);
      }
      if (p.isLast()) {
        List<Byte> dataBytes = new LinkedList<Byte>();
        p = packets0.get(0);
        byte type = p.getType();
        for (int i = 0; i < packets0.size(); i++) {
          byte[] pData = p.getData();
          for (byte b : pData) {
            dataBytes.add(b);
          }
        }
        byte[] data = new byte[dataBytes.size()];
        data = new byte[dataBytes.size()];
        for (int i = 0; i < dataBytes.size(); i++) {
          data[i] = dataBytes.get(i);
        }
        packets0.clear();
        try {
          processData(type, data);
        } catch (Exception e) {
          Logger.error(TAG, "Data processing failed!", e);
        }
      }
    }
    Logger.debug(TAG, "Device thread ended!");
  }

  private void processData(byte dataType, byte[] data) throws Exception {
    Logger.debug(TAG, "Processing data of type: " + dataType);
    switch (dataType) {
    case Packet.PACKET_TYPE_DEVICE_ADD: {
      functions.clear();
      for (int i = 0; i < data.length; i++) {
        if (data[i] == Packet.PACKET_FUNCTION_DATA) {
          i++;
          byte functionType = data[i++];
          byte functionUidLength = data[i++];
          byte[] functionUid = new byte[functionUidLength];
          for (int j = 0; j < functionUidLength; j++, i++) {
            functionUid[j] = data[i];
          }
          byte functionValueType = data[i++];
          Object value = null;
          switch (functionValueType) {
          case Function.VALUE_TYPE_BOOLEAN:
            value = new Boolean(data[i] != 0);
            break;
          case Function.VALUE_TYPE_BYTE:
            value = new Byte(data[i]);
            break;
          case Function.VALUE_TYPE_DOUBLE:
            long l = 0;
            for (int j = 0; j < 8; j++) {
              l = (l << 8) + (0xff & data[i++]);
            }
            i--;
            value = new Double(Double.longBitsToDouble(l));
            break;
          case Function.VALUE_TYPE_INTEGER:
            int n = 0;
            for (int j = 0; j < 4; j++) {
              n = (n << 8) + (0xff & data[i++]);
            }
            i--;
            value = new Integer(n);
            break;
          }
          FunctionImpl f = new FunctionImpl();
          f.setUid(new String(functionUid, Constants.CHARSET_NAME));
          f.setType(functionType);
          f.setValueType(functionValueType);
          f.setValueInternal(value);
          f.setDevice(this);
          functions.add(f);
          Logger.debug(TAG, "New function processed: " + f.getUid());
        }
      }
      Logger.info(TAG, "Device processed: " + this);
      break;
    }
    case Packet.PACKET_TYPE_FUNCTION_VALUE: {
      FunctionImpl function = null;
      int i = 0;
      byte functionType = data[i++];
      byte functionUidLength = data[i++];
      byte[] functionUid = new byte[functionUidLength];
      for (int j = 0; j < functionUidLength; j++, i++) {
        functionUid[j] = data[i];
      }
      String functionUidStr = new String(functionUid, Constants.CHARSET_NAME);
      for (FunctionImpl f : functions) {
        if (f.getUid().equals(functionUidStr)) {
          function = f;
          break;
        }
      }
      if (function == null || function.getType() != functionType) {
        throw new RuntimeException("Function: " + functionUidStr + " with type: " + functionType + " not found!");
      }
      byte functionValueType = data[i++];
      Object value = null;
      switch (functionValueType) {
      case Function.VALUE_TYPE_BOOLEAN:
        value = new Boolean(data[i++] != 0);
        break;
      case Function.VALUE_TYPE_BYTE:
        value = new Byte(data[i++]);
        break;
      case Function.VALUE_TYPE_DOUBLE:
        long l = 0;
        for (int j = 0; j < 8; j++) {
          l = (l << 8) + (0xff & data[i++]);
        }
        value = new Double(Double.longBitsToDouble(l));
        break;
      case Function.VALUE_TYPE_INTEGER:
        int n = 0;
        for (int j = 0; j < 4; j++) {
          n = (n << 8) + (0xff & data[i++]);
        }
        value = new Integer(n);
        break;
      }
      function.setValueInternal(value);
      break;
    }
    case Packet.PACKET_TYPE_FUNCTION_VALUE_SET: {
      break;
    }
    }
    Logger.debug(TAG, "Data processed: " + dataType);
  }

  @Override
  public String toString() {
    StringBuffer buff = new StringBuffer();
    buff.append("uid: ").append(uid).append(Constants.LINE_SEPARATOR);
    buff.append("online: ").append(online).append(Constants.LINE_SEPARATOR);
    buff.append("functions:").append(Constants.LINE_SEPARATOR);
    for (Function f : functions) {
      buff.append(f).append(Constants.LINE_SEPARATOR);
    }
    return buff.toString();
  }

}
