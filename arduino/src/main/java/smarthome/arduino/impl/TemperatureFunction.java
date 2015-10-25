package smarthome.arduino.impl;

import javax.persistence.Entity;
import javax.persistence.Transient;

import smarthome.arduino.utils.Constants;

@Entity
public class TemperatureFunction extends AbstractFunction {

  @Transient
  private long lastValueTimestamp = -1;

  public TemperatureFunction() {
    super(FUNCTION_TYPE_TEMPERATURE);
    TAG = "TemperatureFunction";
  }

  @Override
  public boolean isStatistics() {
    return true;
  }

  @Override
  protected void storeNewValue(double newValue) {
    if (newValue != value || (lastValueTimestamp == -1 || timestamp - lastValueTimestamp > Constants.STORE_INTERVAL)) {
      lastValueTimestamp = timestamp;
      super.storeNewValue(newValue);
    }
  }

}
