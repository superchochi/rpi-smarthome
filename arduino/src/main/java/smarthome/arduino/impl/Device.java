package smarthome.arduino.impl;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.OneToMany;
import javax.persistence.Transient;

import smarthome.arduino.utils.Constants;
import smarthome.arduino.utils.Logger;
import smarthome.db.DBManager;

@Entity
@NamedQueries({ @NamedQuery(name = "Devices.getAll", query = "SELECT c FROM Device c"),
    @NamedQuery(name = "Devices.updateName", query = "UPDATE Device d SET d.name = :name WHERE d.uid = :uid") })
public class Device implements Runnable {

  private static final String TAG = "Device";

  @Transient
  private Controller controller;

  @Id
  private String uid;

  @OneToMany(mappedBy = "device", cascade = CascadeType.ALL, orphanRemoval = true)
  private List<Function> functions = new LinkedList<Function>();

  @Transient
  private boolean online = false;
  private boolean initialized = false;
  private String name = null;

  @Transient
  private Thread thr;

  @Transient
  private volatile boolean running;

  @Transient
  private LinkedList<Packet> packets = new LinkedList<Packet>();

  @Transient
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
    try {
      thr.join();
    } catch (InterruptedException e) {
      Logger.error(TAG, uid + " > Thread join failed!" + e);
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

  public boolean isInitialized() {
    return initialized;
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

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
    Map<String, Object> params = new HashMap<String, Object>();
    params.put("name", name);
    params.put("uid", uid);
    DBManager.updateObject("Devices.updateName", this.getClass(), params);
    Logger.info(TAG, uid + " > Device name set: " + name);
  }

  protected void setUid(String uid) {
    this.uid = uid;
  }

  protected void setController(Controller controller) {
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
    Logger.debug(TAG, uid + " > Device thread started!");
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
          Logger.error(TAG, uid + " > Error waiting for packets!", e);
          break;
        }
        p = packets.removeFirst();
        if (p.getType() == Packet.PACKET_TYPE_SERIAL) {
          if (packets0.isEmpty()) {
            Logger.warning(TAG, uid + " > Serial packet came but no starting one was found! Skip packet...");
            continue;
          }
        } else {
          if (!packets0.isEmpty()) {
            Logger.warning(TAG, uid + " > Starting packet came before serial end! Remove old packets...");
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
          p = packets0.get(i);
          byte[] pData = p.getData();
          for (byte b : pData) {
            dataBytes.add(b);
          }
        }
        byte[] data = new byte[dataBytes.size()];
        for (int i = 0; i < dataBytes.size(); i++) {
          data[i] = dataBytes.get(i);
        }
        packets0.clear();
        try {
          processData(type, data);
        } catch (Exception e) {
          Logger.error(TAG, uid + " > Data processing failed!", e);
        }
      }
    }
    Logger.debug(TAG, uid + " > Device thread ended!");
  }

  private void processData(byte dataType, byte[] data) throws Exception {
    Logger.debug(TAG, uid + " > Processing data of type: " + dataType);
    switch (dataType) {
    case Packet.PACKET_TYPE_DEVICE_ADD: {
      functions.clear();
      for (int i = 0; i < data.length; i++) {
        if (data[i] == Packet.PACKET_FUNCTION_DATA) {
          i++;
          Function f = new Function();
          i = f.init(data, i);
          f.setDevice(this);
          functions.add(f);
          Logger.debug(TAG, uid + " > New function processed: " + f.getUid());
        }
      }
      initialized = true;
      online = true;
      DBManager.mergeObject(this);
      Logger.info(TAG, uid + " > Device add processed: " + this);
      break;
    }
    case Packet.PACKET_TYPE_FUNCTION_VALUE: {
      Function function = null;
      int i = 0;
      byte functionType = data[i++];
      byte functionUidLength = data[i++];
      char[] functionUid = new char[functionUidLength];
      for (int j = 0; j < functionUidLength; j++, i++) {
        functionUid[j] = (char) data[i];
      }
      String functionUidStr = new String(functionUid);
      for (Function f : functions) {
        if (f.getUid().equals(functionUidStr)) {
          function = f;
          break;
        }
      }
      if (function == null || function.getType() != functionType) {
        throw new RuntimeException("Function: " + functionUidStr + " with type: " + functionType + " not found!");
      }
      function.updateValue(data, i);
      if (!online) {
        online = true;
      }
      Logger.debug(TAG, uid + " > Device function updated.");
      break;
    }
    case Packet.PACKET_TYPE_FUNCTION_VALUE_SET: {
      break;
    }
    }
    Logger.debug(TAG, uid + " > Data processed: " + dataType);
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
