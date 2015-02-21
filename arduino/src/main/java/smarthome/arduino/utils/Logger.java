package smarthome.arduino.utils;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.util.Calendar;

public class Logger {

  public static final String PROPERTY_LOG_LEVEL = "smarthome.logger.level";
  public static final String PROPERTY_LOG_FILENAME = "smarthome.arduino.log";

  public static final String LEVEL_DEBUG = "DEBUG";
  public static final String LEVEL_INFO = "INFO";
  public static final String LEVEL_WARNING = "WARNING";
  public static final String LEVEL_ERROR = "ERROR";

  public static final int DEBUG = 4;
  public static final int INFO = 3;
  public static final int WARNING = 2;
  public static final int ERROR = 1;

  private static int level = Integer.getInteger(PROPERTY_LOG_LEVEL, INFO);

  private static final Object sync = new Object();
  private static File logFile = null;
  private static BufferedOutputStream out = null;

  public static void open() {
    String logFilename = System.getProperty(PROPERTY_LOG_FILENAME, "controller.log");
    logFile = new File(logFilename);
    if (logFile.exists() && !logFile.isFile()) {
      System.out.println("Could not start smarthome controller logger because file is a directory!");
      return;
    }
    if (!logFile.exists()) {
      try {
        if (!logFile.createNewFile()) {
          System.out.println("Could not create logFile: " + logFilename);
          return;
        }
      } catch (Exception e) {
        e.printStackTrace();
        return;
      }
    }
    try {
      out = new BufferedOutputStream(new FileOutputStream(logFile, true));
    } catch (Exception e) {
      e.printStackTrace();
      return;
    }
  }

  public static void close() {
    synchronized (sync) {
      if (out != null) {
        try {
          out.close();
        } catch (Exception e) {
        }
        out = null;
        logFile = null;
      }
    }
  }

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
      StringBuffer buff = new StringBuffer();
      buff.append(Calendar.getInstance().getTime()).append(" ").append(level).append(" [").append(tag).append("]: ")
          .append(msg).append(Constants.LINE_SEPARATOR);
      synchronized (sync) {
        // System.out.println(level + " [" + tag + "]: " + msg);
        try {
          Calendar.getInstance().getTime();
          out.write(buff.toString().getBytes());
          if (t != null) {
            t.printStackTrace(new PrintStream(out));
          }
          out.flush();
        } catch (Exception e) {
          e.printStackTrace();
        }
      }
    }
  }

  public static void main(String[] args) {
    System.out.println(Calendar.getInstance().getTime());
  }

}
