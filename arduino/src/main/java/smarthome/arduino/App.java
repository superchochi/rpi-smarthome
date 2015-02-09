package smarthome.arduino;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import smarthome.arduino.impl.ControllerImpl;
import smarthome.arduino.impl.Packet;
import smarthome.arduino.utils.Logger;

import com.pi4j.io.serial.SerialDataEvent;

public class App {

  private static final String TAG = "ArduinoTestApp";

  public static void main(String[] args) throws Exception {
    System.out.println("Hello World!");
    addTestDevice();
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
          break;
        }
        System.out.println("Line read: " + line);
        for (byte b : line.getBytes()) {
          // System.out.println("byte: '" + b + "'");
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

    ControllerImpl controller = new ControllerImpl();
    byte[] b1 = { '1', '2', '3', '4', '5', Packet.PACKET_TYPE_DEVICE_ADD, 0, Packet.PACKET_FUNCTION,
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
}
