package smarthome.arduino.impl;

import smarthome.arduino.utils.Constants;

public class Packet {

  public static final int PACKET_LENGTH = 27;
  public static final int PACKET_UID_LENGTH = 5;
  public static final int PACKET_DATA_LENGTH = PACKET_LENGTH - PACKET_UID_LENGTH - 2;
  public static final byte PACKET_TYPE_PING = -1;
  public static final byte PACKET_TYPE_FUNCTION_VALUE = -2;
  public static final byte PACKET_TYPE_FUNCTION_VALUE_SET = -3;
  public static final byte PACKET_TYPE_DEVICE_ADD = -4;
  public static final byte PACKET_TYPE_SERIAL = -5;

  public static final byte PACKET_FUNCTION = -100;
  public static final byte PACKET_FUNCTION_UID_LENGTH = -101;
  public static final byte PACKET_FUNCTION_VALUE_TYPE = -102;

  private byte[] uid;
  private byte type;
  private boolean isLast;
  private byte[] data;

  public Packet(byte[] data) {
    if (data.length != PACKET_LENGTH) {
      throw new IllegalArgumentException("Packet length must be: " + PACKET_LENGTH + " but is: " + data.length + "!");
    }
    uid = new byte[PACKET_UID_LENGTH];
    for (int i = 0; i < PACKET_UID_LENGTH; i++) {
      uid[i] = data[i];
    }
    type = data[PACKET_UID_LENGTH];
    isLast = (data[PACKET_UID_LENGTH + 1] == 1);
    this.data = new byte[PACKET_DATA_LENGTH];
    for (int i = 0, j = PACKET_UID_LENGTH + 2; i < PACKET_DATA_LENGTH; i++, j++) {
      this.data[i] = data[j];
    }
  }

  public String getUid() {
    return new String(uid);
  }

  public byte getType() {
    return type;
  }

  public boolean isLast() {
    return isLast;
  }

  public byte[] getData() {
    return data;
  }

  @Override
  public String toString() {
    StringBuffer buff = new StringBuffer();
    buff.append("uid: ");
    for (byte b : uid) {
      buff.append(b).append(" ");
    }
    buff.append(Constants.LINE_SEPARATOR);
    buff.append("type: ").append(type).append(Constants.LINE_SEPARATOR);
    buff.append("isLast: ").append(isLast).append(Constants.LINE_SEPARATOR);
    buff.append("data: ").append(Constants.LINE_SEPARATOR);
    for (byte b : data) {
      buff.append(b).append(" ");
    }
    return buff.toString();
  }

}
