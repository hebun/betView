package freela;

import java.io.Serializable;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

@SuppressWarnings("serial")
public class MyLogger implements Serializable {
	public static final int INFO = 3;
	public static final int WARNING = 2;
	public static final int SEVERE = 1;
	private static final DateFormat df = new SimpleDateFormat(
			"hh:mm:ss dd/MM/yyyy ");

	private int level;
	
	private String format(String level, String message) {

		StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();

		String caller = stackTrace[4].getMethodName();
		StringBuilder builder = new StringBuilder(1000);
		builder.append("[").append(level).append("] ");
		builder.append(message);

		builder.append("    [" + stackTrace[3].getMethodName()).append("]<-")
				.append(caller).append(" - ");
		builder.append(df.format(new Date())).append(" - ");
		builder.append("[").append(stackTrace[3].getClassName()).append(".")
				.append(stackTrace[3].getMethodName()).append("]");

		for (@SuppressWarnings("unused") StackTraceElement stackTraceElement : stackTrace) {
			// System.out.print(stackTraceElement.getMethodName()+"<-");
		}
		return builder.toString();
	}

	public MyLogger(int level) {

		this.level = level;
	}



	public void info(String message) {
		if (level >= INFO)
			System.out.println(format("INFO", message));
	}

	public void warning(String message) {
		if (level >= WARNING)
			System.out.println(format("WARNING", message));
	}

	public void severe(String message) {
		if (level >= SEVERE)
			System.out.println(format("SEVERE", message));
	}

	public static void main(String[] args) {
		extracted();
	}

	private static void extracted() {
		MyLogger logger = new MyLogger(MyLogger.INFO);
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
