package smarthome.arduino.impl;

import java.util.HashMap;
import java.util.Map;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQuery;

import smarthome.arduino.api.DeviceException;
import smarthome.arduino.api.Function;
import smarthome.arduino.api.StatisticEntry;
import smarthome.arduino.utils.Constants;
import smarthome.arduino.utils.Logger;
import smarthome.arduino.utils.Utils;
import smarthome.db.DBManager;

@Entity
@NamedQuery(name = "Function.updateValue", query = "UPDATE AbstractFunction f SET f.value = :value, f.timestamp = :timestamp WHERE f.id = :id")
public abstract class AbstractFunction implements Function {

  protected String TAG = "Function";

  @Id
  protected String id;

  @ManyToOne
  protected DeviceImpl device;

  protected String uid;
  protected byte type;
  protected double value;
  protected byte valueType;
  protected long timestamp;

  public AbstractFunction() {
  }

  public AbstractFunction(byte type) {
    this.type = type;
  }

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

  public long getTimestamp() {
    return timestamp;
  }

  public abstract boolean isStatistics();

  public void setValue(double value) throws DeviceException {
    device.setFunctionValue(uid, value);
  }

  protected void setDevice(DeviceImpl device) {
    this.device = device;
    if (uid != null && device != null) {
      id = device.getUid() + "_" + uid;
    }
  }

  protected int init(byte[] data, int i) {
    byte functionUidLength = data[i++];
    char[] functionUid = new char[functionUidLength];
    for (int j = 0; j < functionUidLength; j++, i++) {
      functionUid[j] = (char) data[i];
    }
    uid = new String(functionUid);
    valueType = data[i++];
    byte[] value = null;
    switch (valueType) {
    case Function.VALUE_TYPE_BOOLEAN:
      value = new byte[1];
      value[0] = data[i];
      break;
    case Function.VALUE_TYPE_BYTE:
      value = new byte[1];
      value[0] = data[i];
      break;
    case Function.VALUE_TYPE_DOUBLE:
      value = new byte[8];
      for (int j = 0; j < 8; j++) {
        value[j] = data[i++];
      }
      i--;
      break;
    case Function.VALUE_TYPE_INTEGER:
      value = new byte[4];
      for (int j = 0; j < 4; j++) {
        value[j] = data[i++];
      }
      i--;
      break;
    }
    this.value = Utils.getValueFromByteArray(value, valueType);
    return i;
  }

  protected void updateValue(byte[] data, int i) {
    byte[] value = null;
    switch (valueType) {
    case Function.VALUE_TYPE_BOOLEAN:
      value = new byte[1];
      value[0] = data[i++];
      break;
    case Function.VALUE_TYPE_BYTE:
      value = new byte[1];
      value[0] = data[i++];
      break;
    case Function.VALUE_TYPE_DOUBLE:
      value = new byte[8];
      for (int j = 0; j < 8; j++) {
        value[j] = data[i++];
      }
      break;
    case Function.VALUE_TYPE_INTEGER:
      value = new byte[4];
      for (int j = 0; j < 4; j++) {
        value[j] = data[i++];
      }
      break;
    }
    double newValue = Utils.getValueFromByteArray(value, valueType);
    timestamp = Utils.getTimestamp();
    Map<String, Object> params = new HashMap<String, Object>();
    params.put("value", newValue);
    params.put("timestamp", timestamp);
    params.put("id", id);
    DBManager.updateObject("Function.updateValue", this.getClass(), params);
    Logger.info(TAG, id + " > Value updated: " + newValue);
    if (isStatistics()) {
      storeNewValue(newValue);
    }
    this.value = newValue;
  }

  protected void storeNewValue(double newValue) {
    DBManager.persistObject(new StatisticEntry(id, newValue, valueType, timestamp));
    Logger.info(TAG, id + " > Statistic stored!");
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
