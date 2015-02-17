package betting;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import org.apache.commons.codec.binary.Base64;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import util.JdbcLong;
import util.MyLogger;

public class WonClub {

	private static final String REFERER = "http://www.wonclub.com/ComPg/Firm12.aspx?From=STARTPAGE";

	private static final String HOST = "www.wonclub.com";

	private static final String URL = "http://www.wonclub.com/ajaxpro/BetFrmWrk.ComPg.Pages12,BetFrmWrk.ashx";

	private static final String name = "wonclub";

	private final static String USER_AGENT = "Mozilla/5.0 (X11; Ubuntu; Linux x86_64; rv:28.0) Gecko/20100101 Firefox/28.0";
	private static final MyLogger log = new MyLogger(3);
	private static int matchCount = 0;

	public WonClub() {

	}

	public static void main(String[] args) {

		// if(true)return;

		Measure.callAndMeasure(new Callable() {

			@Override
			public void run() {
				// TODO Auto-generated method stub
				WonClub.getMatchs();
			}
		}, "wonclu");

		Measure.dump();

	}

	public static StringBuilder insert;

	@SuppressWarnings("unused")
	private static void saveToFile(String fromNet, String filename) {
		try {

			Files.write(Paths.get("./" + filename + ".html"), fromNet
					.toString().getBytes());
		} catch (IOException e1) {

			e1.printStackTrace();
		}
	}

	public static void getMatchs() {
		log.info(name + "  started");
		StringBuilder result = getFromNet();

		insert = new StringBuilder();
		insert.append("insert ignore into `tempo`(externId,homeTeam,awayTeam,ht,at,draw,sitename,tarih)"
				+ " values ");
		int matchCount = 0, failCount = 0;
		Document doc = Jsoup.parse(result.toString());

		Elements elements = doc.getElementsByClass("contents-bg");
		elements.addAll(doc.getElementsByClass("contents-bg2"));
		for (Element element : elements) {
			try {
				String match = element.getElementsByClass("area5").get(0)
						.text();
				String[] teams = match.split(" v ");
				if (teams.length < 2) {
					continue;
				}

				String ht = element.getElementsByClass("area6").get(0).text().replaceAll("[^\\d]", "");
				String draw = element.getElementsByClass("area7").get(0).text().replaceAll("[^\\d]", "");
				String at = element.getElementsByClass("area8").get(0).text().replaceAll("[^\\d]", "");
				
				Date instance = Calendar.getInstance().getTime();
				SimpleDateFormat dateFormat=new SimpleDateFormat("Y-M-d");
				
				String tarih =dateFormat.format(instance)+" "
						+ element.getElementsByClass("area4").get(0).text();
				
			//	log.info(tarih+" "+teams[0]+" "+teams[1]+" "+ht+" "+draw+" "+at+" ");
				insert.append("('"
						+ getExternId(teams[0] + teams[1] +name) + "','"
						+ teams[0] + "','" + teams[1] + "'," + ht + "," + at + "," + draw
						+ ",'"+name+"','" + tarih + "'),");
				matchCount++;
			} catch (Exception e) {
				failCount++;
				log.warning(e.getMessage());
			}
		}
		insert.deleteCharAt(insert.length() - 1);
		insert.append(" on duplicate key update ht=Values(ht),at=values(at),draw=values(draw),tarih=values(tarih) ");

		log.info("-- inserting " + matchCount + " matchs for " + name + ". "
				+ failCount + " entris failed");
		JdbcLong.query(insert.toString());
		 JdbcLong.close(name);
		if (true)
			return;

	}

	private static String getExternId(String str) {
		return new String(Base64.encodeBase64(str.getBytes()));
	}

	private static StringBuilder getFromFile() {
		try (BufferedReader br = new BufferedReader(new FileReader(
				"wonclub.html"))) {
			StringBuilder sb = new StringBuilder();
			String line = br.readLine();

			while (line != null) {
				sb.append(line);
				sb.append(System.lineSeparator());
				line = br.readLine();
			}
			return sb;
		} catch (IOException e) {

			e.printStackTrace();
			return new StringBuilder();
		}
	}

	private static StringBuilder getFromNet() {

		String url = URL;

		try {
			StringBuilder post = MyRequest.post(url);
			saveToFile(post.toString(), "wonclub");
			return post;

		} catch (Exception e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
			return new StringBuilder("empty");
		}

	}

}
