package smarthome.arduino.impl;

import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.Timer;
import java.util.TimerTask;

import javax.persistence.Entity;
import javax.persistence.Transient;

import smarthome.arduino.api.StatisticEntry;
import smarthome.arduino.utils.Logger;
import smarthome.arduino.utils.Utils;
import smarthome.db.DBManager;

@Entity
public class TotalMeterFunction extends AbstractFunction {

  public static final String PROPERTY_METER_STATISTIC_INTERVAL = "smarthome.arduino.meter.statistic.interval";
  private static final long DEFAULT_METER_STATISTIC_INTERVAL = 3600000;

  @Transient
  private Timer timer = null;
  @Transient
  private volatile double lastStoredValue = -1;
  @Transient
  private volatile long lastValueTimestamp = 0;

  public TotalMeterFunction() {
    super(FUNCTION_TYPE_METER_TOTAL);
    TAG = "TotalMeterFunction";
  }

  @Override
  public boolean isStatistics() {
    return true;
  }

  @Override
  protected int init(byte[] data, int i) {
    lastValueTimestamp = 0;
    setTimer();
    int res = super.init(data, i);
    lastStoredValue = value;
    return res;
  }

  @Override
  protected void storeNewValue(double newValue) {
    lastValueTimestamp = Utils.getTimestamp();
    if (lastStoredValue == -1) {
      lastStoredValue = value;
    }
    if (newValue < value) {
      lastStoredValue -= value;
    }
    if (timer == null) {
      setTimer();
    }
  }

  private void setTimer() {
    GregorianCalendar calendar = new GregorianCalendar();
    calendar.set(Calendar.HOUR_OF_DAY, calendar.get(Calendar.HOUR_OF_DAY) + 1);
    calendar.set(Calendar.MINUTE, 0);
    calendar.set(Calendar.SECOND, 0);
    calendar.set(Calendar.MILLISECOND, 0);
    timer = new Timer("Meter timer");
    timer.scheduleAtFixedRate(new TimerTaskImpl(), calendar.getTime(),
        Long.getLong(PROPERTY_METER_STATISTIC_INTERVAL, DEFAULT_METER_STATISTIC_INTERVAL));
    Logger.info(TAG, id + " > timer set: " + calendar.getTime());
  }

  private class TimerTaskImpl extends TimerTask {

    @Override
    public void run() {
      Logger.info(TAG, id + " > timer executed");
      if (lastValueTimestamp == 0) {
        timer.cancel();
        timer.purge();
        timer = null;
      } else {
        double consumption = value - lastStoredValue;
        long timestamp = Utils.getHourBeginMillis(Utils.getTimestamp());
        lastValueTimestamp = 0;
        DBManager.persistObject(new StatisticEntry(id, consumption, valueType, timestamp));
        Logger.info(TAG, id + " > Statistic stored!");
        lastStoredValue = value;
      }
    }

  }

}
