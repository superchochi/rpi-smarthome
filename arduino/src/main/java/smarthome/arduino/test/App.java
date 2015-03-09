package smarthome.arduino.test;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import smarthome.arduino.impl.Controller;
import smarthome.arduino.impl.Device;
import smarthome.arduino.impl.Function;
import smarthome.arduino.impl.Packet;
import smarthome.arduino.impl.StatisticEntry;
import smarthome.arduino.utils.Logger;
import smarthome.db.DBManager;

import com.pi4j.io.serial.SerialDataEvent;

public class App {

  private static final String TAG = "ArduinoTestApp";

  private static Controller controller = null;
  private static List<TestDevice> devices = null;

  public static void main(String[] args) throws Exception {
    System.setProperty(Logger.PROPERTY_LOG_LEVEL, String.valueOf(Logger.DEBUG));
    Logger.open();
    controller = new Controller();
    controller.init();
    devices = new LinkedList<TestDevice>();
    Map<String, Byte> functions = new HashMap<String, Byte>();
    functions.put("func1", Function.FUNCTION_TYPE_TEMPERATURE);
    functions.put("func2", Function.FUNCTION_TYPE_HUMIDITY);
    addDevice("dev01", functions);
    Thread.sleep(5000);
    for (TestDevice d : devices) {
      d.stopRunning(false);
    }
    controller.close();
  }

  private static void doInput() {
    BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
    try {
      while (true) {
        String line = br.readLine();
        if (line == null) {
          Logger.debug(TAG, "End of stream!");
          break;
        }
        if (line.equalsIgnoreCase("exit")) {
          Logger.debug(TAG, "Exit!");
          for (TestDevice device : devices) {
            device.stopRunning(true);
          }
          break;
        }
        System.out.println("Line read: " + line);
        if (line.equalsIgnoreCase("stats")) {
          Map<String, String> params = new HashMap<String, String>();
          params.put("functionUid", "dev01_func1");
          List<StatisticEntry> stats = DBManager.getObjects("Stats.getByFunctionUid", StatisticEntry.class, params);
          System.out.println(stats);
        }
      }
    } catch (IOException e) {
      Logger.error(TAG, "Error reading line!", e);
    }
  }

  public static void addTestDevice() throws Exception {
    double value = 65.43;
    byte[] output = new byte[8];
    long lng = Double.doubleToLongBits(value);
    System.out.println(lng);
    for (int i = 0; i < 8; i++) {
      output[i] = (byte) (lng >> ((7 - i) * 8));
    }

    Controller controller = new Controller();
    byte[] b1 = { '1', '2', '3', '4', '5', Packet.PACKET_TYPE_DEVICE_ADD, 0, Packet.PACKET_FUNCTION_DATA,
        Function.FUNCTION_TYPE_TEMPERATURE, 20, '5', '5', '5', '5', '5', '5', '5', '5', '5', '5', '5', '5', '5', '5',
        '5', '5', '5' };

    controller.dataReceived(new SerialDataEvent(new Object(), new String(b1, "Cp1252")));
    byte[] b2 = { '1', '2', '3', '4', '5', Packet.PACKET_TYPE_SERIAL, 0, '5', '5', '5', Function.VALUE_TYPE_DOUBLE,
        output[0], output[1], output[2], output[3], output[4], output[5], output[6], output[7], '5', '5', '5', '5',
        '5', '5', '5', '5' };
    System.out.println(new String(b2, "Cp1252"));
    controller.dataReceived(new SerialDataEvent(new Object(), new String(b2, "Cp1252")));

    byte[] b3 = { '1', '2', '3', '4', '5', Packet.PACKET_TYPE_SERIAL, 1, '5', '5', '5', '5', '5', '5', '5', '5', '5',
        '5', '5', '5', '5', '5', '5', '5', '5', '5', '5', '5' };

    controller.dataReceived(new SerialDataEvent(new Object(), new String(b3, "Cp1252")));

    Thread.sleep(1000);
    Device[] devices = controller.getDevices();
    for (Device d : devices) {
      System.out.println(d);
    }
  }

  private static void addDevice(String uid, Map<String, Byte> functions) {
    TestDevice device = new TestDevice(controller, uid);
    for (Iterator<String> it = functions.keySet().iterator(); it.hasNext();) {
      String fUid = it.next();
      byte fType = functions.get(fUid);
      device.addFunction(fType, fUid);
    }
    device.startRunning();
    devices.add(device);
  }
}
