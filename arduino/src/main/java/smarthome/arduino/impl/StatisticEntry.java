package smarthome.arduino.impl;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.NamedQuery;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;

import smarthome.arduino.utils.Constants;
import smarthome.arduino.utils.Utils;

@Entity
@NamedQuery(name = "Stats.getByFunctionUid", query = "SELECT s FROM StatisticEntry s WHERE s.functionUid = :functionUid")
@XmlRootElement
public class StatisticEntry {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @XmlTransient
  private long id;
  @XmlElement
  private String functionUid;
  @XmlTransient
  private byte[] value;
  @XmlElement
  private byte valueType;
  @XmlElement
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

  @XmlElement
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