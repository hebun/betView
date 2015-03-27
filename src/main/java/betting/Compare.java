package betting;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import freela.MyLogger;
import util.LevenshteinDistance;

public class Compare {
	private MyLogger log = new MyLogger(3);
	private double BURO_AMOUNT;
	private double COMISSION;
	private double EURO;
	private double MIN_ODD;
	private double MAX_ODD;

	public Compare(double BURO_AMOUNT, double COMISSION, double EURO,
			double MIN_ODD, double MAX_ODD) {

		this.BURO_AMOUNT = BURO_AMOUNT;
		this.COMISSION = COMISSION;
		this.EURO = EURO;
		this.MIN_ODD = MIN_ODD;
		this.MAX_ODD = MAX_ODD;
	}

	private void divideBy100(Map<String, String> map, String col) {
		String value = map.get(col);
		if (value == null)
			return;
		float fv = Float.parseFloat(value) / 100;
		map.put(col, fv + "");
	}

	public double round(double value, int places) {
		if (places < 0)
			throw new IllegalArgumentException();

		BigDecimal bd = new BigDecimal(value);
		bd = bd.setScale(places, RoundingMode.HALF_UP);
		return bd.doubleValue();
	}

	private Map<String, String> checkPro(Map<String, String> dual,
			String deodd, String odd) {
		double oldHt = (double) Integer.parseInt(dual.get(odd));
		double ht = oldHt / 100;

		if (ht < MIN_ODD || ht > MAX_ODD)
			return null;
		// System.out.println(dual);
		double deht = 0;
		try {
			deht = (double) Integer.parseInt(dual.get(deodd)) / 100;
		} catch (NumberFormatException e1) {
			return null;
		}
		// deht=deht*1.004;
//System.out.println(deht+"-"+ht);
		if (ht > deht || (deht - ht) < 0.2) {
			// System.out.println(ht);

			double fark = (double) (ht - deht);
			double pro = fark / (ht * deht - fark);
			double round = round(pro * 100, 2);
			if (round > 20.0)
				return null;
			Map<String, String> hm = new LinkedHashMap<String, String>();
			hm.put("Mac", dual.get("bfMatch"));

			String min = dual.get("min");
			if (min != null)
				hm.put("min", min);
			hm.put("Buro Mac", dual.get("buroMatch"));
			// hm.put("buroTable", dual.get("buroTable"));
			hm.put("tarih", dual.get("tarih"));
			// hm.put("odd", odd);
			hm.put("Oran", dual.get(odd) + " / " + dual.get(deodd));

			double C7 = (BURO_AMOUNT * ht * (deht - 1)) / (deht - COMISSION);

			double C8 = C7 / EURO;

			double lay = C8 / (deht - 1);

			double netProfit = ht * BURO_AMOUNT - BURO_AMOUNT - C7;

			if (netProfit > 0 && ht < 4 && deht > 1) {
				new Thread(new Runnable() {

					@Override
					public void run() {
//						Live.tone(400, 500);
					}
				}).start();

			}

			hm.put("profit", round(netProfit, 2) + "");
			hm.put("pcol", odd);
			hm.put("buro", dual.get("buro"));
			// hm.put("lay", round(lay, 2) + "");
			// hm.put("liability", round(C8,2)+"");
			return hm;

		}
		return null;
	}

	public ArrayList<Map<String, String>> findProfitibleMatches(
			List<Map<String, String>> tempoTable,
			List<Map<String, String>> betfairTable) {
		log.info("started find findProfitibleMatches ");

		List<Map<String, String>> dualMatchs = populateDualMatchs(tempoTable,
				betfairTable);

		log.info("-- fetched " + dualMatchs.size() + " matchs from dualView");

		ArrayList<Map<String, String>> profitibles = new ArrayList<Map<String, String>>();

		for (final Map<String, String> dual : dualMatchs) {

			Map<String, String> checkPro = checkPro(dual, "deht", "ht");
			if (checkPro != null)
				profitibles.add(checkPro);

			checkPro = checkPro(dual, "deat", "at");

			if (checkPro != null)
				profitibles.add(checkPro);

			checkPro = checkPro(dual, "dedraw", "draw");

			if (checkPro != null)
				profitibles.add(checkPro);

		}

		profitibles.sort(new Comparator<Map<String, String>>() {

			@Override
			public int compare(Map<String, String> o1, Map<String, String> o2) {
				Double ap = Double.parseDouble(o1.get("profit"));
				Double bp = Double.parseDouble(o2.get("profit"));

				return ap > bp ? -1 : ap == bp ? 0 : 1;

			}
		});
		for (Map<String, String> map : profitibles) {
			String col = "deht";

			divideBy100(map, col);
			divideBy100(map, "ht");
			divideBy100(map, "at");
			divideBy100(map, "deat");
			divideBy100(map, "draw");
			divideBy100(map, "dedraw");

			// Map<String, String> hm = map;
			// hm.put("ht-deht", hm.get("ht") + "/" + hm.get("deht"));
			// hm.put("at-deat", hm.get("at") + "/" + hm.get("deat"));
			// hm.put("draw-dedraw", hm.get("draw") + "/" + hm.get("dedraw"));

			// hm.remove("ht");
			// hm.remove("at");
			// hm.remove("draw");
			// hm.remove("deht");
			// hm.remove("deat");
			// hm.remove("dedraw");
		}
		log.info("-- found " + profitibles.size() + " profitible matchs ");
		return profitibles;
	}

	private List<Map<String, String>> populateDualMatchs(
			List<Map<String, String>> tempoTable,
			List<Map<String, String>> betfairTable) {

		log.info("started to populate dual matchs");

		log.info("-- got " + tempoTable.size() + " rows for buroTable");

		log.info("-- got " + betfairTable.size() + " rows for betfair");

		List<Map<String, String>> dualList = new ArrayList<Map<String, String>>();
		int foundDualCount = 0;
		for (Map<String, String> rowBetFair : betfairTable) {

			for (Map<String, String> rowTempo : tempoTable) {

				double similarityAway = LevenshteinDistance.similarity(
						rowBetFair.get("awayTeam"), rowTempo.get("awayTeam"));

				double similarityHome = LevenshteinDistance.similarity(
						rowBetFair.get("homeTeam"), rowTempo.get("homeTeam"));

				if (similarityAway > 0.6 && similarityHome > 0.6) {
					foundDualCount++;
					Map<String, String> dualMap = new HashMap<String, String>();

					dualMap.put("bfMatch", rowBetFair.get("homeTeam") + "-"
							+ rowBetFair.get("awayTeam"));
					dualMap.put("buroMatch", rowTempo.get("homeTeam") + "-"
							+ rowTempo.get("awayTeam"));
					dualMap.put("ht", rowTempo.get("ht"));
					dualMap.put("at", rowTempo.get("at"));
					dualMap.put("draw", rowTempo.get("draw"));
					dualMap.put("deht", rowBetFair.get("deht"));
					dualMap.put("deat", rowBetFair.get("deat"));
					dualMap.put("dedraw", rowBetFair.get("dedraw"));
					dualMap.put("min", rowTempo.get("min"));
					dualMap.put("tarih", rowBetFair.get("tarih"));
					dualMap.put("buro", rowTempo.get("buro"));
					dualList.add(dualMap);
				}
			}
		}
		log.info("-- found " + foundDualCount + " matchs ");

		return dualList;

	}
}
