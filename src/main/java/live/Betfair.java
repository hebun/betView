package live;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class Betfair extends BuroBase {

	/**
	 * current: different lists from imajbet, tarih
	 */

	public Betfair() {

	}

	public static void main(String[] args) {

		// Betfair.getMatchs();

	}

	public List<Map<String, String>> getMatchs() {
		log.info("betfair  started");

		java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat(
				"yyyy-MM-dd");
		Calendar now = Calendar.getInstance();
		// now.add(Calendar.DAY_OF_MONTH, 1);
		String time = sdf.format(now.getTime());
		return getByDay(time);

	}

	private  List<Map<String, String>> getByDay(String time) {

		StringBuffer result = getFromNet("http://www.oddsfair.net/frame/en/index.php?date="
				+ time + "&order=timeasc");

		String sub;
		sub = result.toString();

		Document doc = Jsoup.parse(sub);

		Elements trs = doc.getElementsByClass("table_line1");

		List<Map<String, String>> matchs = new ArrayList<Map<String, String>>();

		for (Element element : trs) {
			Elements tds = element.getElementsByTag("td");
			int tdCount = 0;
			Map<String, String> match = new HashMap<String, String>();
			for (Element td : tds) {
				if (td.parent() == element) {

					if (td.hasAttr("width")) {
						if (td.attr("width").equals("220")) {

							match.put("homeTeam", td.text().split(" v ")[0]);

							// insert.add("homeTeam",
							// td.text().split(" v ")[0]);
							match.put("awayTeam", td.text().split(" v ")[1].replaceAll("Live!", "")) ;

						}
						if (td.attr("width").equals("50")) {
							java.text.SimpleDateFormat sdft = new java.text.SimpleDateFormat(
									"yyyy-MM-dd HH:mm");
							try {
								String timeText = time + " " + td.text();
								match.put("tarih", timeText);

								sdft.parse(timeText);
							} catch (ParseException e) {

								e.printStackTrace();
							}
						}
					}
					Elements tables = td.getElementsByTag("table");
					if (tables.size() > 0) {
						String text = tables.get(0)
								.getElementsByClass("layblock").text();
						String odd = text.replaceAll(Pattern.quote(","), "");

						if (tdCount == 0) {

							match.put("deht", odd);
						}
						if (tdCount == 1) {

							match.put("dedraw", odd);
						}
						if (tdCount == 2) {

							match.put("deat", odd);
						}

						tdCount++;
					}
				}
			}
			matchs.add(match);
			// Db.insert(insert.get());

			// insert.run();
		}
		return matchs;

	}

}
