package betting;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Calendar;

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

public class Wonodds {

	/**
	 * current: different lists from imajbet, tarih
	 */

	private final static String USER_AGENT = "Mozilla/5.0 (X11; Ubuntu; Linux x86_64; rv:28.0) Gecko/20100101 Firefox/28.0";
	private static final MyLogger log =new MyLogger(3);
	private static int matchCount = 0;

	public Wonodds() {

	}

	public static void main(String[] args) {

			Wonodds.getMatchs();
		
	}

	public static StringBuilder insert;

	public static void getMatchs() {
		log.info("wonodds  started");
		StringBuffer result = getFromNet();

		String sub;
		sub = result.toString();
		try {
			// sub.split("id=\"sprt-1\"");
			Files.write(Paths.get("./wonoddsx.html"), sub.getBytes());
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		
		Document doc = Jsoup.parse(sub);

		Elements elements = doc.getElementsByClass("MMItem");
	//	new ASCIITable().printTable(elements);
		log.info("-- got " + elements.size() + " tags of matchs at wonodds");
		
		
		insert = new StringBuilder();
		insert.append("insert ignore into `tempo`(externId,homeTeam,awayTeam,ht,at,draw,sitename,tarih)"
				+ " values ");
		int matchCount = 0, failCount = 0;
		for (Element element : elements) {
			try {
				Element name = element.getElementsByClass("MMPartita").get(0);

				
				

				String[] split = name.text().split("-");
				String homeTeam = split[0].replace('\'', '"');
				String awayTeam = split[1].replace('\'', '"');
				
				Element htt = element.getElementsByClass("MM1").get(0);


				String text = htt.text().replace(',', '.');
				int ht = (int) (Float.parseFloat(text) * 100);
				
				Element att = element.getElementsByClass("MM2").get(0);
				 text = att.text().replace(',', '.');
				int at = (int) (Float.parseFloat(text) * 100);
				
				Element drawtt = element.getElementsByClass("MMX").get(0);
				 text = drawtt.text().replace(',', '.');
				int drawt = (int) (Float.parseFloat(text) * 100);
		System.out.println(name.text()+":"+ht+"-"+drawt+"-"+at);
				String tarih ="2015-01-07 20:20"; //Calendar.getInstance().get(Calendar.YEAR) + "-"
						// + dates[1] + "-" + dates[0] + " " + time.text();

				insert.append("('"
						+ getExternId(homeTeam + awayTeam
								+ Wonodds.class.getName()) + "','" + homeTeam
						+ "','" + awayTeam + "'," + ht + "," + at + "," + drawt
						+ ",'imaj','" + tarih + "'),");
				matchCount++;

			} catch (Exception e) {
				failCount++;
				e.printStackTrace();
			}

		}
		insert.deleteCharAt(insert.length() - 1);
		insert.append(" on duplicate key update ht=Values(ht),at=values(at),draw=values(draw),tarih=values(tarih) ");

		log.info("-- inserting " + matchCount + " matchs for wonodds. "
				+ failCount + " entris failed");
		//JdbcLong.query(insert.toString());
		//JdbcLong.close("imajbet");
		// System.out.println(insert.toString());
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

		String url = "http://wonodds88.com/Sport/OddsAsync.aspx?EventID=7937,7944,59259,7909,9435,7916,8140,7912";

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
