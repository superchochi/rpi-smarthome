package smarthome.arduino.impl;

import javax.persistence.Entity;

@Entity
public class HumidityFunction extends AbstractFunction {

  public HumidityFunction() {
    super(FUNCTION_TYPE_HUMIDITY);
  }

}
