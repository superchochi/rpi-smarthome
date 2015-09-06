package smarthome.arduino.impl;

import java.io.InputStream;

import com.pi4j.io.serial.SerialDataEvent;

import gnu.io.CommPortIdentifier;
import gnu.io.SerialPort;
import gnu.io.SerialPortEvent;
import gnu.io.SerialPortEventListener;

public class ControllerWinImpl extends ControllerImpl implements SerialPortEventListener {

  private SerialPort port = null;
  private InputStream in = null;

  @Override
  protected void initSerial() {
    try {
      CommPortIdentifier portId = CommPortIdentifier.getPortIdentifier("COM4");
      port = (SerialPort) portId.open("smarthome", 5000);
      port.setSerialPortParams(115200, SerialPort.DATABITS_8, SerialPort.STOPBITS_1, SerialPort.PARITY_NONE);
      port.notifyOnDataAvailable(true);
      port.addEventListener(this);
      in = port.getInputStream();
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  @Override
  protected void deinitSerial() {
    try {
      if (port != null) {
        port.removeEventListener();
        port.close();
        port = null;
      }
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  public void serialEvent(SerialPortEvent event) {
    try {
      switch (event.getEventType()) {
      case SerialPortEvent.DATA_AVAILABLE:
        byte[] buff = new byte[1024];
        int len;
        while ((len = in.read(buff, 0, 1024)) > 0) {
          String data = new String(buff, 0, len, "Windows-1252");
          dataReceived(new SerialDataEvent(this, data));
        }
        break;
      }
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

}
