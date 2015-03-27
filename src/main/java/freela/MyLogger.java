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
		
		
		StringBuilder builder = new StringBuilder(1000);
		builder.append("[").append(level).append("] ");
		builder.append(message);

	
	
		
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
