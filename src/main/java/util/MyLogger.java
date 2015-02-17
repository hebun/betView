package util;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

import freela.Sql;

public class MyLogger {
	public static final int INFO = 3;
	public static final int WARNING = 2;
	public static final int SEVERE = 1;
	private int level;
	private String output = "stdout";

	public String getOut() {
		return output;
	}

	public void setOut(String out) {
		this.output = out;
	}

	private String format(String level, String message) {

		StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();
		String caller = "NONE";
		if (stackTrace.length > 4) {

			caller = stackTrace[4].getMethodName();
		}
		StringBuilder builder = new StringBuilder(1000);
		builder.append("[").append(level).append("] ");
		builder.append(message);

		builder.append("    [" + stackTrace[3].getMethodName()).append("]<-")
				.append(caller);
		if (stackTrace.length > 5) {
			builder.append(" <- ").append(stackTrace[5].getMethodName())
					.append(" - ");
		}

		builder.append("[").append(stackTrace[3].getClassName()).append(".")
				.append(stackTrace[3].getMethodName()).append("]");

		for (@SuppressWarnings("unused")
		StackTraceElement stackTraceElement : stackTrace) {
			// System.out.print(stackTraceElement.getMethodName()+"<-");
		}
		return builder.toString();
	}

	private Map<String, Object> getMap(String level, String message) {

		StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();
		String caller = "NONE";
		if (stackTrace.length > 4) {

			caller = stackTrace[4].getMethodName();
		}

		Map<String, Object> ret = new HashMap<String, Object>();
		ret.put("level", level);

		ret.put("message", message);

		String method = "    [" + stackTrace[3].getMethodName() + "]<-"
				+ caller;

		if (stackTrace.length > 5) {

			String str = " <- " + stackTrace[5].getMethodName() + " - ";

			method += str;
		}

		String cls = "[" + stackTrace[3].getClassName() + "."
				+ stackTrace[3].getMethodName() + "]";
		method += cls;

		ret.put("method", method);
		final DateFormat df = new SimpleDateFormat("Y-M-d hh:mm:ss ");
		ret.put("tarih", df.format(Calendar.getInstance().getTime()));
		//ret.put("tarih","NOW()");
		return ret;
	}

	public MyLogger(int level) {

		this.level = level;
	}

	public void info(String message) {
		String level2 = "INFO";
		if (level >= INFO) {

			if (getOut().equals("db")) {
				//new Sql.Insert("log").addAll(getMap(level2, message)).run();
			} else {
				System.out.println(format(level2, message));
			}
		}
	}

	public void warning(String message) {
		if (level >= WARNING)
			if (getOut().equals("db")) {
				//new Sql.Insert("log").addAll(getMap("WARNING", message)).run();
			} else {
				System.out.println(format("WARNING", message));
			}
	}

	public void severe(String message) {
		if (level >= SEVERE)
			if (getOut().equals("db")) {
			//	new Sql.Insert("log").addAll(getMap("SEVERE", message)).run();
			} else {
				System.out.println(format("SEVERE", message));
			}
	}

	public static void main(String[] args) {
		extracted();
	}

	private static void extracted() {
		MyLogger logger = new MyLogger(MyLogger.INFO);
		logger.setOut("db");
		logger.info("this info message");
		logger.warning("warning message");
		logger.severe("severe message");
	}

	public int getLevel() {
		return level;
	}

	public void setLevel(int level) {
		this.level = level;
	}
}
