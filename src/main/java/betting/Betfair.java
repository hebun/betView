package betting;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClientBuilder;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import util.MyLogger;
import freela.Callable;
import freela.Db;
import freela.Measure;
import freela.Sql.Insert;

public class Betfair {

	/**
	 * current: different lists from imajbet, tarih
	 */

	private final static String USER_AGENT = "Mozilla/5.0 (X11; Ubuntu; Linux x86_64; rv:28.0) Gecko/20100101 Firefox/28.0";
	private static final MyLogger log = new MyLogger(3);
	private static int matchCount = 0;

	public Betfair() {

	}

	public static void main(String[] args) {

		Betfair.getMatchs();

	}

	public static List<Map<String, String>> getMatchs() {
		log.info("betfair  started");

		java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat(
				"yyyy-MM-dd");
		Calendar now = Calendar.getInstance();
		// now.add(Calendar.DAY_OF_MONTH, 1);
		String time = sdf.format(now.getTime());

		List<Map<String, String>> matchs = new ArrayList<Map<String, String>>();

		matchs.addAll(getByDay(time, "0"));
		matchs.addAll(getByDay(time, "20"));
		return matchs;

	}

	private static List<Map<String, String>> getByDay(String time, String offset) {
		StringBuffer result = getFromNet(time, offset);

		String sub;
		sub = result.toString();

		Document doc = Jsoup.parse(sub);
		List<Map<String, String>> matchs = new ArrayList<Map<String, String>>();
		Elements trs = doc.getElementsByClass("table_line1");
		for (Element element : trs) {
			Elements tds = element.getElementsByTag("td");
			int tdCount = 0;
			final Map<String, String> match = new HashMap<String, String>();
			boolean depthSet = false;
			for (Element td : tds) {
				if (td.parent() == element) {

					if (td.hasAttr("width")) {
						if (td.attr("width").equals("220")) {

							match.put("homeTeam", td.text().split(" v ")[0]);
							match.put("awayTeam", td.text().split(" v ")[1]);
							String query = td.getElementsByTag("a")
									.attr("href").split(Pattern.quote("?"))[1];
							try {
								String id = splitQuery(query).get("id");
								match.put("externId", id);

							} catch (UnsupportedEncodingException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}

						}
						if (td.attr("width").equals("50")) {
							java.text.SimpleDateFormat sdft = new java.text.SimpleDateFormat(
									"yyyy-MM-dd HH:mm");
							try {
								String timeText = time + " " + td.text();
								match.put("tarih", timeText);

								sdft.parse(timeText);
							} catch (ParseException e) {
								// TODO Auto-generated catch block
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
						if (tdCount == 3) {

							match.put("deunder", odd);
						}
						if (tdCount == 4) {

							match.put("deover", odd);
						}

						tdCount++;
					}
					Elements strongs = td.getElementsByTag("strong");
					if (strongs.size() > 0 && !depthSet) {

						Element stro = strongs.get(0);
						if (stro.getElementsByTag("a").size() == 0) {
							match.put("depth", stro.text());
							depthSet = true;
						}
					}
				}
			}
	

			matchs.add(match);
		}
		return matchs;

	}

	public static Map<String, String> splitQuery(String query)
			throws UnsupportedEncodingException {
		Map<String, String> query_pairs = new LinkedHashMap<String, String>();

		String[] pairs = query.split("&");
		for (String pair : pairs) {
			int idx = pair.indexOf("=");
			query_pairs.put(URLDecoder.decode(pair.substring(0, idx), "UTF-8"),
					URLDecoder.decode(pair.substring(idx + 1), "UTF-8"));
		}
		return query_pairs;
	}

	private static StringBuffer getFromNet(String time, String offset) {

		String url = "http://www.oddsfair.net/frame/en/index.php?date=" + time
				+ "&order=1x2desc&offset=" + offset;
		// log.info(url);
		HttpClient client = HttpClientBuilder.create().build();
		HttpGet request = new HttpGet(url);
		StringBuffer result = null;
		// add request header
		request.addHeader("User-Agent", USER_AGENT);
		// request.addHeader("X-Requested-With", "XMLHttpRequest");
		request.addHeader("Referer", url);
		for (Header head : request.getAllHeaders()) {
			// System.out.println(head.getName() + ":" + head.getValue());
		}

		HttpResponse response;
		try {
			response = client.execute(request);

			BufferedReader rd = new BufferedReader(new InputStreamReader(
					response.getEntity().getContent()));

			result = new StringBuffer();
			String line = "";
			while ((line = rd.readLine()) != null) {
				result.append(line + "\n");

			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return result;
	}

}
