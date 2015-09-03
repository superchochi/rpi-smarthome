package smarthome.arduino.impl;

import javax.persistence.Entity;

@Entity
public class TemperatureFunction extends AbstractFunction {

  public TemperatureFunction() {
    super(FUNCTION_TYPE_TEMPERATURE);
    TAG = "TemperatureFunction";
  }

  @Override
  public boolean isStatistics() {
    return true;
  }

}
