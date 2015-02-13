package smarthome.arduino.impl;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.NamedQuery;

import smarthome.arduino.utils.Constants;
import smarthome.arduino.utils.Utils;

@Entity
@NamedQuery(name = "Stats.getByFunctionUid", query = "SELECT s FROM StatisticEntry s WHERE s.functionUid = :functionUid")
public class StatisticEntry {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private long id;
  private String functionUid;
  private byte[] value;
  private byte valueType;
  private long timestamp;

  public StatisticEntry() {
  }

  public StatisticEntry(String functionUid, byte[] value, byte valueType, long timestamp) {
    this.functionUid = functionUid;
    this.value = value;
    this.valueType = valueType;
    this.timestamp = timestamp;
  }

  public String getFunctionUid() {
    return functionUid;
  }

  public Object getValue() {
    return Utils.getValueFromByteArray(value, valueType);
  }

  public long getTimestamp() {
    return timestamp;
  }

  @Override
  public String toString() {
    StringBuffer buff = new StringBuffer();
    buff.append("functionUid: ").append(functionUid).append(Constants.LINE_SEPARATOR);
    buff.append("value: ").append(Utils.getValueFromByteArray(value, valueType));
    return buff.toString();
  }
}