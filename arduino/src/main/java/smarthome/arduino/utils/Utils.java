package smarthome.arduino.utils;

import java.util.Calendar;
import java.util.GregorianCalendar;

import smarthome.arduino.api.Function;

public class Utils {

  public static final String TEST_TIMESTAMP_PROP = "smarthome.test.timestamp";

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

  public static final long getTimestamp() {
    long timestamp = Long.getLong(TEST_TIMESTAMP_PROP, -1);
    return timestamp == -1 ? System.currentTimeMillis() : timestamp;
  }

  public static long getDayBeginMillis(long timestamp) {
    GregorianCalendar calendar = new GregorianCalendar();
    calendar.setTimeInMillis(timestamp);
    calendar.set(Calendar.HOUR_OF_DAY, 0);
    calendar.set(Calendar.MINUTE, 0);
    calendar.set(Calendar.SECOND, 0);
    calendar.set(Calendar.MILLISECOND, 0);
    return calendar.getTimeInMillis();
  }

  public static long getNextDayBeginMillis(long timestamp) {
    GregorianCalendar calendar = new GregorianCalendar();
    calendar.setTimeInMillis(timestamp);
    calendar.set(Calendar.HOUR_OF_DAY, 0);
    calendar.set(Calendar.MINUTE, 0);
    calendar.set(Calendar.SECOND, 0);
    calendar.set(Calendar.MILLISECOND, 0);
    calendar.set(Calendar.DAY_OF_MONTH, calendar.get(Calendar.DAY_OF_MONTH) + 1);
    return calendar.getTimeInMillis();
  }

  public static long getHourBeginMillis(long timestamp) {
    GregorianCalendar calendar = new GregorianCalendar();
    calendar.setTimeInMillis(timestamp);
    calendar.set(Calendar.MINUTE, 0);
    calendar.set(Calendar.SECOND, 0);
    calendar.set(Calendar.MILLISECOND, 0);
    return calendar.getTimeInMillis();
  }

}
