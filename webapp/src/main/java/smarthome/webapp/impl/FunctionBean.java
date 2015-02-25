package smarthome.webapp.impl;

import smarthome.arduino.impl.Function;

public class FunctionBean {

  private String uid;
  private byte type;
  private Object value;
  private byte valueType;

  public FunctionBean() {
  }

  public FunctionBean(Function function) {
    uid = function.getUid();
    type = function.getType();
    value = function.getValue();
    valueType = function.getValueType();
  }

  public String getUid() {
    return uid;
  }

  public void setUid(String uid) {
    this.uid = uid;
  }

  public byte getType() {
    return type;
  }

  public void setType(byte type) {
    this.type = type;
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

}
