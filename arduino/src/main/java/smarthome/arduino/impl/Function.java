package smarthome.arduino.impl;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;

import smarthome.arduino.utils.Constants;
import smarthome.arduino.utils.Logger;
import smarthome.arduino.utils.Utils;
import smarthome.db.DBManager;

@Entity
@XmlRootElement
public class Function {

  public static final byte VALUE_TYPE_INTEGER = -1;
  public static final byte VALUE_TYPE_DOUBLE = -2;
  public static final byte VALUE_TYPE_BYTE = -3;
  public static final byte VALUE_TYPE_BOOLEAN = -4;

  public static final byte FUNCTION_TYPE_TEMPERATURE = -100;
  public static final byte FUNCTION_TYPE_HUMIDITY = -101;

  private static final String TAG = "Function";

  @Id
  @XmlTransient
  private String id;

  @ManyToOne
  @XmlTransient
  private Device device;

  @XmlElement
  private String uid;
  @XmlElement
  private byte type;
  @XmlTransient
  private byte[] value;
  @XmlElement
  private byte valueType;

  public String getUid() {
    return uid;
  }

  public byte getType() {
    return type;
  }

  @XmlElement
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

  protected void setDevice(Device device) {
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
