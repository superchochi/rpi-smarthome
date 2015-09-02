package smarthome.arduino.impl;

import javax.persistence.Entity;

@Entity
public class BatteryFunction extends AbstractFunction {

  public BatteryFunction() {
    super(FUNCTION_TYPE_BATTERY);
  }

}
