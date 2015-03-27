package betting;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import util.MyLogger;
import freela.Measure;

;
public class SwingMain {
	/**
	 * match table update
	 * 
	 */
	private static final MyLogger log = new MyLogger(3);
	public static final double BURO_AMOUNT = 500;

	private static final double MIN_ODD = 2.0;
	private static final double COMISSION = 0.048;
	private static final double EURO = 2.78;
	private static final double MAX_ODD = 5;




	public static void main(String[] args) {
		// clearDb();
		// if(true) return;
		// log.setOut("db");
		log.info("Application Started");

		try {
			
			//Tempobet.getWeekend();
			//List<Map<String, String>> betvole = new Marathon().getMatchs();
			List<Map<String, String>> imaj= Imajbet.getMatchs();
			// WonClub.getMatchs();
			//ASCIITable.printTableS(betvole);
			List<Map<String, String>> bf = Betfair.getMatchs();
		//	ASCIITable.printTableS(bf);
			Compare compare = new Compare(BURO_AMOUNT, COMISSION, EURO, MIN_ODD,
					MAX_ODD);
			
		// betvole.addAll(imaj);
			ArrayList<Map<String, String>> findProfitibleMatches = compare
					.findProfitibleMatches(imaj,bf);
			
			ASCIITable.printTableS(findProfitibleMatches);
			
		} catch (Exception e) {
			log.severe("application stopped:");
			e.printStackTrace();

		}
		Measure.dump();

	}


	private static final long serialVersionUID = 4741507128571219377L;

}
