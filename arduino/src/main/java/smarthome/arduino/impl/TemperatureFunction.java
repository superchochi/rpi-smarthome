package smarthome.arduino.impl;

import javax.persistence.Entity;

@Entity
public class TemperatureFunction extends AbstractFunction {

  public TemperatureFunction() {
    super(FUNCTION_TYPE_TEMPERATURE);
  }

}
