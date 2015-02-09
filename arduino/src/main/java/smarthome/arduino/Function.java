package smarthome.arduino;

public interface Function {

  public static final byte VALUE_TYPE_INTEGER = -1;
  public static final byte VALUE_TYPE_DOUBLE = -2;
  public static final byte VALUE_TYPE_BYTE = -3;
  public static final byte VALUE_TYPE_BOOLEAN = -4;

  public static final byte FUNCTION_TYPE_TEMPERATURE = -100;
  public static final byte FUNCTION_TYPE_HUMIDITY = -101;

  public String getUid();

  public byte getType();

  public byte getValueType();

  public Object getValue();

  public void setValue(Object value) throws DeviceException;

  public Object[] getStatisticValues(long from, long to);

}
