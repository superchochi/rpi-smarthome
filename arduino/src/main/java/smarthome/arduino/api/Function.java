package smarthome.arduino.api;

public interface Function {

  public static final byte VALUE_TYPE_BYTE = (byte) 0xB1;
  public static final byte VALUE_TYPE_INTEGER = (byte) 0xB2;
  public static final byte VALUE_TYPE_DOUBLE = (byte) 0xB3;
  public static final byte VALUE_TYPE_BOOLEAN = (byte) 0xB4;

  public static final byte FUNCTION_TYPE_TEMPERATURE = (byte) 0xA1;
  public static final byte FUNCTION_TYPE_HUMIDITY = (byte) 0xA2;
  public static final byte FUNCTION_TYPE_BATTERY = (byte) 0xA3;
  public static final byte FUNCTION_TYPE_METER_CURRENT = (byte) 0xA4;
  public static final byte FUNCTION_TYPE_METER_TOTAL = (byte) 0xA5;

  public String getUid();

  public byte getType();

  public double getValue();

  public void setValue(double value) throws DeviceException;

  public byte getValueType();

  public long getTimestamp();

  public boolean isStatistics();

}
