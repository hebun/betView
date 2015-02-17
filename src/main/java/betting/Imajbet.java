package betting;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.Reader;
import java.io.StringReader;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

import javax.swing.text.MutableAttributeSet;
import javax.swing.text.html.HTML;
import javax.swing.text.html.HTMLEditorKit;
import javax.swing.text.html.parser.ParserDelegator;

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

public class Imajbet {

	/**
	 * current: different lists from imajbet, tarih
	 */

	private final static String USER_AGENT = "Mozilla/5.0 (X11; Ubuntu; Linux x86_64; rv:28.0) Gecko/20100101 Firefox/28.0";
	private static final MyLogger log = new MyLogger(3);
	private static int matchCount = 0;

	public Imajbet() {

	}

	public static void main(String[] args) {

			Imajbet.getMatchs();
	
		Measure.dump();
	}

	public static StringBuilder insert;

	public static void getMatchs() {
		log.info("imajbet  started");

		StringBuffer result = getFromNet();

		final String sub;
		sub = result.toString();

		Document doc = Jsoup.parse(sub);

		Elements elements = doc.getElementsByClass("fixtureLayout");
		log.info("-- got " + elements.size() + " tags of matchs at imajbet");
		insert = new StringBuilder();
		insert.append("insert ignore into `tempo`(externId,homeTeam,awayTeam,ht,at,draw,sitename,tarih)"
				+ " values ");
		int matchCount = 0, failCount = 0;
		for (Element element : elements) {
			try {
				Element ul = element.getElementsByTag("ul").get(0);

				Elements fixtureEventName = ul
						.getElementsByClass("fixture-event-name");
				if (fixtureEventName.size() == 0)
					continue;
				Element li = fixtureEventName.get(0);

				Elements a = li.getElementsByTag("a");

				String homeTeam = a.get(0).text().replace('\'', '"');
				String awayTeam = a.get(1).text().replace('\'', '"');

				Elements odds = ul.getElementsByClass("fixture-bet-title")
						.get(0).getElementsByTag("a");

				String text = odds.get(0).text().replace(',', '.');
				int ht = (int) (Float.parseFloat(text) * 100);
				String text2 = odds.get(1).text().replace(',', '.');
				int draw = (int) (Float.parseFloat(text2) * 100);
				String text3 = odds.get(2).text().replace(',', '.');
				int at = (int) (Float.parseFloat(text3) * 100);

				Element date = ul.getElementsByClass("fixture-date").get(0);
				Element time = ul.getElementsByClass("fixture-time").get(0);

				String dateText = date.text();
				String[] dates = dateText.split("\\.");
				String tarih = Calendar.getInstance().get(Calendar.YEAR) + "-"
						+ dates[1] + "-" + dates[0] + " " + time.text();

				insert.append("('"
						+ getExternId(homeTeam + awayTeam
								+ Imajbet.class.getName()) + "','" + homeTeam
						+ "','" + awayTeam + "'," + ht + "," + at + "," + draw
						+ ",'imaj','" + tarih + "'),");
				matchCount++;

			} catch (Exception e) {
				failCount++;
				// System.out.println("fail:" + element.outerHtml());
			}

		}
		insert.deleteCharAt(insert.length() - 1);
		insert.append(" on duplicate key update ht=Values(ht),at=values(at),draw=values(draw),tarih=values(tarih) ");

		log.info("-- inserting " + matchCount + " matchs for imajbet. "
				+ failCount + " entris failed");
		JdbcLong.query(insert.toString());
		JdbcLong.close("imajbet");
		// System.out.println(insert.toString());
		if (true)
			return;

	}

	private static String getExternId(String str) {
		return new String(Base64.encodeBase64(str.getBytes()));
	}

	private static StringBuffer getFromNet() {

		String url = "http://www.imajbet.com/sports/1/";

		HttpClient client = HttpClientBuilder.create().build();
		HttpGet request = new HttpGet(url);
		StringBuffer result = new StringBuffer();;
		// add request header
		request.addHeader("User-Agent", USER_AGENT);
		request.addHeader("X-Requested-With", "XMLHttpRequest");
		request.addHeader("Referer", "http://imajbet.com/sport/");
		for (Header head : request.getAllHeaders()) {
			// System.out.println(head.getName() + ":" + head.getValue());
		}
	
			HttpResponse response;
			try {
				response = client.execute(request);

				BufferedReader rd = new BufferedReader(new InputStreamReader(
						response.getEntity().getContent()));

				
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
