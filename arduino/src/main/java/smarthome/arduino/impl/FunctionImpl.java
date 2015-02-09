package smarthome.arduino.impl;

import smarthome.arduino.DeviceException;
import smarthome.arduino.Function;
import smarthome.arduino.utils.Constants;

public class FunctionImpl implements Function {

  private DeviceImpl device;
  private String uid;
  private byte type;
  private Object value;
  private byte valueType;

  public String getUid() {
    return uid;
  }

  public byte getType() {
    return type;
  }

  public Object getValue() {
    return value;
  }

  public byte getValueType() {
    return valueType;
  }

  public void setValue(Object value) throws DeviceException {
    device.setFunctionValue(uid, value);
  }

  public Object[] getStatisticValues(long from, long to) {
    return null;
  }

  protected void setDevice(DeviceImpl device) {
    this.device = device;
  }

  protected void setValueInternal(Object value) {
    this.value = value;
  }

  protected void setUid(String uid) {
    this.uid = uid;
  }

  protected void setType(byte type) {
    this.type = type;
  }

  protected void setValueType(byte valueType) {
    this.valueType = valueType;
  }

  @Override
  public String toString() {
    StringBuffer buff = new StringBuffer();
    buff.append("uid: ").append(uid).append(Constants.LINE_SEPARATOR);
    buff.append("type: ").append(type).append(Constants.LINE_SEPARATOR);
    buff.append("value: ").append(value);
    return buff.toString();
  }

}
