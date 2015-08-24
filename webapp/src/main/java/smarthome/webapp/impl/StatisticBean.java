package smarthome.webapp.impl;

import smarthome.arduino.api.StatisticEntry;

public class StatisticBean {

  private String functionUid;
  private Object value;
  private byte valueType;
  private long timestamp;

  public StatisticBean() {
  }

  public StatisticBean(StatisticEntry entry) {
    functionUid = entry.getFunctionUid();
    value = entry.getValue();
    valueType = entry.getValueType();
    timestamp = entry.getTimestamp();
  }

  public String getFunctionUid() {
    return functionUid;
  }

  public void setFunctionUid(String functionUid) {
    this.functionUid = functionUid;
  }

  public Object getValue() {
    return value;
  }

  public void setValue(Object value) {
    this.value = value;
  }

  public byte getValueType() {
    return valueType;
  }

  public void setValueType(byte valueType) {
    this.valueType = valueType;
  }

  public long getTimestamp() {
    return timestamp;
  }

  public void setTimestamp(long timestamp) {
    this.timestamp = timestamp;
  }

}
