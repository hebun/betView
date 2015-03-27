package live;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.SourceDataLine;

import util.LevenshteinDistance;
import betting.ASCIITable;
import betting.Compare;
import freela.MyLogger;

public class Live {
	private static MyLogger log = new MyLogger(1);

	public static final double BURO_AMOUNT = 150;

	private static final double MIN_ODD = 2;
	private static final double MAX_ODD = 5;
	private static final double COMISSION = 0.048;
	private static final double EURO = 2.78;

	public final static void clearConsole() {
		try {

			for (int i = 0; i < 22; i++) {

				System.out.println();

			}
		} catch (final Exception e) {
			// Handle any exceptions.
		}
	}

	public static void main(String[] args) throws IOException,
			InterruptedException {
		// System.out.println("blablalb");
		while (true) {
			// clearConsole();
			printResult();
			System.gc();
			Thread.sleep(15000);

		}
	}

	private static void printResult() {
		final BuroBase base = new Pronet("thebetend");
		BuroBase betfair = new Betfair();
		List<Map<String, String>> betvole = base.getMatchs();
		List<Map<String, String>> bf = betfair.getMatchs();


		Compare compare = new Compare(BURO_AMOUNT, COMISSION, EURO, MIN_ODD,
				MAX_ODD);
		
		ArrayList<Map<String, String>> findProfitibleMatches = compare
				.findProfitibleMatches(betvole,bf);
		
		ASCIITable.printTableS(findProfitibleMatches);
	}

	// private ArrayList<Map<String, String>> profitibles;

	public static float SAMPLE_RATE = 8000f;

	public static void tone(int hz, int msecs) throws LineUnavailableException {
		tone(hz, msecs, 1.0);
	}

	public static void tone(int hz, int msecs, double vol)
			throws LineUnavailableException {
		byte[] buf = new byte[1];
		AudioFormat af = new AudioFormat(SAMPLE_RATE, // sampleRate
				8, // sampleSizeInBits
				1, // channels
				true, // signed
				false); // bigEndian
		SourceDataLine sdl = AudioSystem.getSourceDataLine(af);
		sdl.open(af);
		sdl.start();
		for (int i = 0; i < msecs * 8; i++) {
			double angle = i / (SAMPLE_RATE / hz) * 2.0 * Math.PI;
			buf[0] = (byte) (Math.sin(angle) * 127.0 * vol);
			sdl.write(buf, 0, 1);
		}
		sdl.drain();
		sdl.stop();
		sdl.close();
	}
}
