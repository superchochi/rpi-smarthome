package smarthome.arduino.api;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;

import smarthome.arduino.utils.Constants;

@Entity
@NamedQueries({
    @NamedQuery(name = "Stats.uid", query = "SELECT s FROM StatisticEntry s WHERE s.functionUid = :functionUid"),

    @NamedQuery(name = "Stats.uidTime", query = "SELECT s FROM StatisticEntry s WHERE s.functionUid = :functionUid "
        + "AND s.timestamp >= :from AND s.timestamp < :to"),

    @NamedQuery(name = "Stats.uidMax", query = "SELECT MAX(s.value) FROM StatisticEntry s WHERE s.functionUid = :functionUid"),

    @NamedQuery(name = "Stats.uidMin", query = "SELECT MIN(s.value) FROM StatisticEntry s WHERE s.functionUid = :functionUid"),

    @NamedQuery(name = "Stats.uidAvg", query = "SELECT AVG(s.value) FROM StatisticEntry s WHERE s.functionUid = :functionUid"),

    @NamedQuery(name = "Stats.uidTimeMax", query = "SELECT MAX(s.value) FROM StatisticEntry s WHERE s.functionUid = :functionUid AND s.timestamp >= :from AND s.timestamp < :to"),

    @NamedQuery(name = "Stats.uidTimeMin", query = "SELECT MIN(s.value) FROM StatisticEntry s WHERE s.functionUid = :functionUid AND s.timestamp >= :from AND s.timestamp < :to"),

    @NamedQuery(name = "Stats.uidTimeAvg", query = "SELECT AVG(s.value) FROM StatisticEntry s WHERE s.functionUid = :functionUid AND s.timestamp >= :from AND s.timestamp < :to") })
public class StatisticEntry {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private long id;
  private String functionUid;
  private double value;
  private byte valueType;
  private long timestamp;

  public StatisticEntry() {
  }

  public StatisticEntry(String functionUid, double value, byte valueType, long timestamp) {
    this.functionUid = functionUid;
    this.value = value;
    this.valueType = valueType;
    this.timestamp = timestamp;
  }

  public String getFunctionUid() {
    return functionUid;
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

  @Override
  public String toString() {
    StringBuffer buff = new StringBuffer();
    buff.append("functionUid: ").append(functionUid).append(Constants.LINE_SEPARATOR);
    buff.append("value: ").append(value);
    return buff.toString();
  }
}