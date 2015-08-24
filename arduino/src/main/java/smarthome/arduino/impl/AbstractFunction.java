package smarthome.arduino.impl;

import java.util.HashMap;
import java.util.Map;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQuery;
import javax.persistence.Transient;

import smarthome.arduino.api.DeviceException;
import smarthome.arduino.api.Function;
import smarthome.arduino.api.StatisticEntry;
import smarthome.arduino.utils.Constants;
import smarthome.arduino.utils.Logger;
import smarthome.arduino.utils.Utils;
import smarthome.db.DBManager;

@Entity
@NamedQuery(name = "Function.updateValue", query = "UPDATE AbstractFunction f SET f.value = :value, f.timestamp = :timestamp WHERE f.id = :id")
public class AbstractFunction implements Function {

  private static final String TAG = "Function";

  @Id
  private String id;

  @ManyToOne
  private DeviceImpl device;

  private String uid;
  private byte type;
  private double value;
  private byte valueType;
  private long timestamp;
  private boolean statistics;
  @Transient
  private long lastStoredStatistic = -1;

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

  public boolean isStatistics() {
    return statistics;
  }

  public void setValue(double value) throws DeviceException {
    device.setFunctionValue(uid, value);
  }

  //  public Object[] getStatisticValues(long from, long to) {
  //    return null;
  //  }

  protected void setDevice(DeviceImpl device) {
    this.device = device;
    if (uid != null && device != null) {
      id = device.getUid() + "_" + uid;
    }
  }

  protected int init(byte[] data, int i) {
    type = data[i++];
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
    if (type == FUNCTION_TYPE_BATTERY) {
      statistics = false;
    } else {
      statistics = true;
    }
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
    timestamp = System.currentTimeMillis();
    Map<String, Object> params = new HashMap<String, Object>();
    params.put("value", newValue);
    params.put("timestamp", timestamp);
    params.put("id", id);
    DBManager.updateObject("Function.updateValue", this.getClass(), params);
    Logger.info(TAG, id + " > Value updated: " + newValue);
    if (statistics && (this.value != newValue || (timestamp - lastStoredStatistic) >= 3540000)) {
      DBManager.persistObject(new StatisticEntry(id, newValue, valueType, timestamp));
      lastStoredStatistic = timestamp;
      Logger.info(TAG, id + " > Statistic stored!");
    }
    this.value = newValue;
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
