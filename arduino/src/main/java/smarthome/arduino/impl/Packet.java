package smarthome.arduino.impl;

import smarthome.arduino.utils.Constants;

public class Packet {

  /*
   * 5 bytes - device address (used for UID) 1 byte - packet type (kind of packet info) 1 byte - flag for last packet of
   * serial 20 bytes - data
   */

  public static final int PACKET_LENGTH = 27;
  public static final int PACKET_UID_LENGTH = 5;
  public static final int PACKET_TYPE_INDEX = PACKET_UID_LENGTH;
  public static final int PACKET_IS_LAST_INDEX = PACKET_TYPE_INDEX + 1;
  public static final int PACKET_DATA_LENGTH = PACKET_LENGTH - PACKET_UID_LENGTH - 2;
  public static final int PACKET_DATA_START_INDEX = PACKET_IS_LAST_INDEX + 1;

  public static final byte PACKET_TYPE_PING = -1;
  public static final byte PACKET_TYPE_FUNCTION_VALUE = -2;
  public static final byte PACKET_TYPE_FUNCTION_VALUE_SET = -3;
  public static final byte PACKET_TYPE_DEVICE_ADD = -4;
  public static final byte PACKET_TYPE_SERIAL = -5;

  public static final byte PACKET_FUNCTION_DATA = -100;
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
    type = data[PACKET_TYPE_INDEX];
    if (!isValidType(type)) {
      throw new IllegalArgumentException("Packet type unknown: " + type);
    }
    isLast = (data[PACKET_IS_LAST_INDEX] == 1);
    this.data = new byte[PACKET_DATA_LENGTH];
    for (int i = 0, j = PACKET_DATA_START_INDEX; i < PACKET_DATA_LENGTH; i++, j++) {
      this.data[i] = data[j];
    }
  }

  public String getUid() {
    char[] chars = new char[uid.length];
    for (int i = 0; i < chars.length; i++) {
      chars[i] = (char) uid[i];
    }
    return new String(chars);
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

  public static boolean isValidType(byte type) {
    switch (type) {
    case PACKET_TYPE_DEVICE_ADD:
    case PACKET_TYPE_FUNCTION_VALUE:
    case PACKET_TYPE_FUNCTION_VALUE_SET:
    case PACKET_TYPE_PING:
    case PACKET_TYPE_SERIAL:
      return true;
    default:
      return false;
    }
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
