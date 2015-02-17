package betting;

import java.util.List;
import java.util.Map;

import util.MyLogger;
import freela.Db;
import freela.Sql;
import freela.Sql.Delete;


;
public class SwingMain {
	/**
	 * match table update
	 * 
	 */
	private static final MyLogger log = new MyLogger(3);
	public static void main(String[] args) {

	
		// log.setOut("db");
		log.info("Application Started");


		try {
			

			Tempobet.getWeekend();
			Imajbet.getMatchs();
			Betfair.getMatchs();
			// ApiNGDemo.fetchEvents();
			// ApiNGDemo.fetchMarkets();
			// ApiNGDemo.fetchMarketBook();
			populateDualMatchs();
			new Betting().findProfitibleMatches();
		} catch (Exception e) {
			log.severe("application stopped:");
			e.printStackTrace();
			
		}
	

		// clearDb();

	}



	private static void clearDb() {
		new Delete("dualmatch").where("'1'", "1").run();
		new Delete("match").where("'1'", "1").run();
		new Delete("tempo").where("'1'", "1").run();
	}

	public static void populateDualMatchs() {

		log.info("started to populate dual matchs");
		List<Map<String, String>> tempoTable = new Sql.Select().from(
				"tempoView").getTable();
		log.info("-- got " + tempoTable.size() + " rows for buroTable");
		List<Map<String, String>> betfairTable = new Sql.Select().from(
				"matchView").getTable();
		log.info("-- got " + betfairTable.size() + " rows for betfair");

		StringBuilder insertDual = new StringBuilder(
				"insert ignore into dualmatch(betfairId,buroId,buroTable) values ");
		int foundDualCount = 0;
		for (Map<String, String> rowBetFair : betfairTable) {

			for (Map<String, String> rowTempo : tempoTable) {
				double similarityAway = Measure.similarityImp(
						rowBetFair.get("awayTeam"), rowTempo.get("awayTeam"));

				double similarityHome = Measure.similarityImp(
						rowBetFair.get("homeTeam"), rowTempo.get("homeTeam"));

				if (similarityAway > 0.6 && similarityHome > 0.6) {
					foundDualCount++;
					insertDual.append("(" + rowBetFair.get("id") + ","
							+ rowTempo.get("id") + ",'"
							+ rowTempo.get("siteName") + "'),");

				}
			}
		}
		log.info("-- found " + foundDualCount + " matchs ");

		if (foundDualCount > 0) {

			insertDual.deleteCharAt(insertDual.length() - 1);
			Db.insert(insertDual.toString());

		}

	}

	private static final long serialVersionUID = 4741507128571219377L;
	// SELECT CONCAT( m.homeTeam, ' VS ', m.awayTeam ) AS betfair, CONCAT(
	// t.homeTeam, ' VS ', t.awayTeam ) AS tempo, CONCAT( m.ht, ' ', m.at ) AS
	// bfOdds, CONCAT( t.ht, ' ', t.at ) AS tempoOdds
	// FROM dualmatch AS d
	// INNER JOIN `match` AS m ON m.id = d.betFairId
	// INNER JOIN tempo AS t ON t.id = d.buroId
	// LIMIT 0 , 30
}
