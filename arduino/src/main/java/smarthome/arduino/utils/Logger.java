package smarthome.arduino.utils;

public class Logger {

  private static final String LEVEL_DEBUG = "DEBUG";
  private static final String LEVEL_INFO = "INFO";
  private static final String LEVEL_WARNING = "WARNING";
  private static final String LEVEL_ERROR = "ERROR";

  private static final int DEBUG = 4;
  private static final int INFO = 3;
  private static final int WARNING = 2;
  private static final int ERROR = 1;

  private static int level = 4;

  private static final Object sync = new Object();

  public static void debug(String tag, String msg, Throwable t) {
    log(LEVEL_DEBUG, tag, msg, t, DEBUG);
  }

  public static void debug(String tag, String msg) {
    log(LEVEL_DEBUG, tag, msg, null, DEBUG);
  }

  public static void info(String tag, String msg, Throwable t) {
    log(LEVEL_INFO, tag, msg, t, INFO);
  }

  public static void info(String tag, String msg) {
    log(LEVEL_INFO, tag, msg, null, INFO);
  }

  public static void warning(String tag, String msg, Throwable t) {
    log(LEVEL_WARNING, tag, msg, t, WARNING);
  }

  public static void warning(String tag, String msg) {
    log(LEVEL_WARNING, tag, msg, null, WARNING);
  }

  public static void error(String tag, String msg, Throwable t) {
    log(LEVEL_ERROR, tag, msg, t, ERROR);
  }

  public static void error(String tag, String msg) {
    log(LEVEL_ERROR, tag, msg, null, ERROR);
  }

  private static void log(String level, String tag, String msg, Throwable t, int l) {
    if (l <= Logger.level) {
      synchronized (sync) {
        System.out.println(level + " [" + tag + "]: " + msg);
        if (t != null) {
          t.printStackTrace();
        }
      }
    }
  }

}
