package smarthome.arduino.impl;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.ManyToOne;

import smarthome.arduino.DeviceException;
import smarthome.arduino.Function;
import smarthome.arduino.utils.Constants;
import smarthome.arduino.utils.Logger;
import smarthome.arduino.utils.Utils;
import smarthome.db.DBManager;

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
    Object val = Utils.getValueFromByteArray(value, valueType);
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

  protected void setValueInternal(byte[] value, boolean storeInDB) {
    this.value = value;
    Logger.info(TAG, "Value updated: " + getValue());
    if (storeInDB) {
      DBManager.mergeObject(this);
      DBManager.persistObject(new StatisticEntry(id, value, valueType, System.currentTimeMillis()));
    }
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
    buff.append("value: ").append(Utils.getValueFromByteArray(value, valueType));
    return buff.toString();
  }

}
