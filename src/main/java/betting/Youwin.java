package betting;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Calendar;
import java.util.Date;

import org.apache.commons.codec.binary.Base64;
import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClientBuilder;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import util.JdbcLong;
import util.MyLogger;

public class Youwin {

	/**
	 * current: different lists from imajbet, tarih
	 */

	private final static String USER_AGENT = "Mozilla/5.0 (X11; Ubuntu; Linux x86_64; rv:28.0) Gecko/20100101 Firefox/28.0";
	private static final MyLogger log = new MyLogger(3);
	private static int matchCount = 0;

	public Youwin() {

	}

	public static void main(String[] args) {

		Youwin.getMatchs();

	}

	public static StringBuilder insert;

	public static void getMatchs() {
		log.info("youwin  started");
		StringBuffer result = getFromNet();

		String sub;
		sub = result.toString();
		try {
			// sub.split("id=\"sprt-1\"");
			Files.write(Paths.get("./youwin.html"), sub.getBytes());
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}

		insert = new StringBuilder();
		insert.append("insert ignore into `tempo`(externId,homeTeam,awayTeam,ht,at,draw,sitename,tarih)"
				+ " values ");
		int matchCount = 0, failCount = 0;
		Document doc = Jsoup.parse(sub);
		Elements elements = doc.getElementsByClass("eventTSoccer");
		for (Element element : elements) {
			try {
				String home = "";
				Elements marketnames = element.getElementsByClass("marketName");
				if (marketnames.size() > 0)
					home = marketnames.get(0).text();
				// System.out.println("home:" + home);
				String away = "";
				Elements marketnamesaway = element
						.getElementsByClass("marketNameAway");
				if (marketnamesaway.size() > 0)
					away = marketnamesaway.get(0).text();
				// System.out.println("away:" + away);

				Elements odds = element.getElementsByClass("odd");
				int oddk = 0;
				String ht = null, at = null, draw = null;
				for (Element element2 : odds) {

					if (oddk == 0) {

						ht = element2.text();
						ht = ht.replaceAll("[^\\d]", "");
					} else if (oddk == 1) {
						draw = element2.text();
						draw = draw.replaceAll("[^\\d]", "");
					} else if (oddk == 2) {
						at = element2.text();
						at = at.replaceAll("[^\\d]", "");
					}
					oddk++;
					// System.out.print(element2.text() + "-");

				}
				String day = element.getElementsByClass("date").get(0).text()
						.split(" ")[1];
				String time = element.getElementsByClass("time").get(0).text();

				String tarih = "2015-"
						+ (Calendar.getInstance().get(Calendar.MONTH) + 1)
						+ "-" + day + " " + time;
				insert.append("('"
						+ getExternId(home + away + Youwin.class.getName())
						+ "','" + home + "','" + away + "'," + ht + "," + at
						+ "," + draw + ",'youwin','" + tarih + "'),");
				matchCount++;
			} catch (Exception e) {
				failCount++;
				log.warning(e.getMessage());
			}
		}
		insert.deleteCharAt(insert.length() - 1);
		insert.append(" on duplicate key update ht=Values(ht),at=values(at),draw=values(draw),tarih=values(tarih) ");

		log.info("-- inserting " + matchCount + " matchs for youwin. "
				+ failCount + " entris failed");
		JdbcLong.query(insert.toString());
		JdbcLong.close("youwin");
		if (true)
			return;

	}

	private static String getExternId(String str) {
		return new String(Base64.encodeBase64(str.getBytes()));
	}

	private static String getFromFile() {
		try (BufferedReader br = new BufferedReader(new FileReader(
				"target/tempobet.html"))) {
			StringBuilder sb = new StringBuilder();
			String line = br.readLine();

			while (line != null) {
				sb.append(line);
				sb.append(System.lineSeparator());
				line = br.readLine();
			}
			return sb.toString();
		} catch (IOException e) {

			e.printStackTrace();
			return "";
		}
	}

	private static StringBuffer getFromNet() {

		String url = "http://www.hepsibahis8.com/tr/spor/futbol/";

		HttpClient client = HttpClientBuilder.create().build();
		HttpGet request = new HttpGet(url);
		StringBuffer result = null;
		// add request header
		request.addHeader("User-Agent", USER_AGENT);

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
