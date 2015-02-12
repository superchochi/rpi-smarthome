package smarthome.arduino.impl;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.ManyToOne;

import smarthome.arduino.DeviceException;
import smarthome.arduino.Function;
import smarthome.arduino.utils.Constants;
import smarthome.arduino.utils.Logger;

@Entity
public class FunctionImpl implements Function {

  private static final String TAG = "Function";

  @Id
  private String id;
  @ManyToOne
  private DeviceImpl device;
  private String uid;
  private byte type;
  private byte[] value;
  private byte valueType;

  public String getUid() {
    return uid;
  }

  public byte getType() {
    return type;
  }

  public Object getValue() {
    Object val = null;
    if (value != null) {
      switch (valueType) {
      case Function.VALUE_TYPE_BOOLEAN:
        val = new Boolean(value[0] != 0);
        break;
      case Function.VALUE_TYPE_BYTE:
        val = new Byte(value[0]);
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
        val = new Integer(n);
        break;
      }
    }
    return val;
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
    if (uid != null) {
      id = device.getUid() + "_" + uid;
    }
  }

  protected void setValueInternal(byte[] value) {
    this.value = value;
    Logger.info(TAG, "Value updated: " + getValue());
  }

  protected void setUid(String uid) {
    this.uid = uid;
    if (device != null) {
      id = device.getUid() + "_" + uid;
    }
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
    buff.append("value: ").append(getValue());
    return buff.toString();
  }

}
