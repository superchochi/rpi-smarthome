package smarthome.arduino.impl;

import java.util.HashMap;
import java.util.Map;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQuery;

import smarthome.arduino.utils.Constants;
import smarthome.arduino.utils.Logger;
import smarthome.db.DBManager;

@Entity
@NamedQuery(name = "Function.updateValue", query = "UPDATE Function f SET f.value = :value WHERE f.id = :id")
public class Function {

  public static final byte VALUE_TYPE_INTEGER = -1;
  public static final byte VALUE_TYPE_DOUBLE = -2;
  public static final byte VALUE_TYPE_BYTE = -3;
  public static final byte VALUE_TYPE_BOOLEAN = -4;

  public static final byte FUNCTION_TYPE_TEMPERATURE = -100;
  public static final byte FUNCTION_TYPE_HUMIDITY = -101;

  private static final String TAG = "Function";

  @Id
  private String id;

  @ManyToOne
  private Device device;

  private String uid;
  private byte type;
  private double value;
  private byte valueType;

  public String getUid() {
    return uid;
  }

  public byte getType() {
    return type;
  }

  public double getValue() {
    return value;
  }

  public byte getValueType() {
    return valueType;
  }

  public void setValue(double value) throws DeviceException {
    device.setFunctionValue(uid, value);
  }

  public Object[] getStatisticValues(long from, long to) {
    return null;
  }

  protected void setDevice(Device device) {
    this.device = device;
    if (uid != null && device != null) {
      id = device.getUid() + "_" + uid;
    }
  }

  protected void setValueInternal(double value, boolean storeInDB) {
    this.value = value;
    Logger.info(TAG, id + " > Value updated: " + getValue());
    if (storeInDB) {
      Map<String, Object> params = new HashMap<String, Object>();
      params.put("value", value);
      params.put("id", id);
      DBManager.updateObject("Function.updateValue", this.getClass(), params);
      DBManager.persistObject(new StatisticEntry(id, value, valueType, System.currentTimeMillis()));
    }
  }

  protected void setUid(String uid) {
    this.uid = uid;
    if (device != null && uid != null) {
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
    buff.append("value: ").append(value);
    return buff.toString();
  }

}
