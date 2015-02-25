package smarthome.arduino.utils;

import smarthome.arduino.impl.Function;

public class Utils {

  public static final double getValueFromByteArray(byte[] value, byte valueType) {
    double val = 0;
    if (value != null) {
      switch (valueType) {
      case Function.VALUE_TYPE_BOOLEAN:
        val = new Double(value[0] == 0 ? 0 : 1);
        break;
      case Function.VALUE_TYPE_BYTE:
        val = new Double(value[0]);
        break;
      case Function.VALUE_TYPE_DOUBLE:
        long l = 0;
        for (int j = 0; j < 8; j++) {
          l = (l << 8) + (0xff & value[j]);
        }
        val = new Double(Double.longBitsToDouble(l));
        break;
      case Function.VALUE_TYPE_INTEGER:
        int n = 0;
        for (int j = 0; j < 4; j++) {
          n = (n << 8) + (0xff & value[j]);
        }
        val = new Double(n);
        break;
      }
    }
    return val;
  }

}
