package smarthome.arduino.impl;

import javax.persistence.Entity;

@Entity
public class CurrentMeterFunction extends AbstractFunction {

  public CurrentMeterFunction() {
    super(FUNCTION_TYPE_METER_CURRENT);
    TAG = "CurrentMeterFunction";
  }

  @Override
  public boolean isStatistics() {
    return false;
  }

}
