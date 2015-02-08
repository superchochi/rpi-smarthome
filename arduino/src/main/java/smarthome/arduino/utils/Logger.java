package smarthome.arduino.utils;

public class Logger {

	private static final String LEVEL_DEBUG = "DEBUG";
	private static final String LEVEL_INFO = "INFO";
	private static final String LEVEL_WARNING = "WARNING";
	private static final String LEVEL_ERROR = "ERROR";

	private static final Object sync = new Object();

	public static void debug(String tag, String msg, Throwable t) {
		log(LEVEL_DEBUG, tag, msg, t);
	}

	public static void debug(String tag, String msg) {
		debug(tag, msg, null);
	}

	public static void info(String tag, String msg, Throwable t) {
		log(LEVEL_INFO, tag, msg, t);
	}

	public static void info(String tag, String msg) {
		info(tag, msg, null);
	}

	public static void warning(String tag, String msg, Throwable t) {
		log(LEVEL_WARNING, tag, msg, t);
	}

	public static void warning(String tag, String msg) {
		warning(tag, msg, null);
	}

	public static void error(String tag, String msg, Throwable t) {
		log(LEVEL_ERROR, tag, msg, t);
	}

	public static void error(String tag, String msg) {
		error(tag, msg, null);
	}

	private static void log(String level, String tag, String msg, Throwable t) {
		synchronized (sync) {
			System.out.println(level + " [" + tag + "]: " + msg);
			if (t != null) {
				t.printStackTrace();
			}
		}
	}

}
