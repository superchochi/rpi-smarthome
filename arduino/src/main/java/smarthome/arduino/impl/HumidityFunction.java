package smarthome.arduino.impl;

import javax.persistence.Entity;
import javax.persistence.Transient;

import smarthome.arduino.utils.Constants;

@Entity
public class HumidityFunction extends AbstractFunction {

  @Transient
  private long lastValueTimestamp = -1;

  public HumidityFunction() {
    super(FUNCTION_TYPE_HUMIDITY);
    TAG = "HumidityFunction";
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
