package betting;

import java.io.BufferedReader;
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
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

import javax.swing.text.MutableAttributeSet;
import javax.swing.text.html.HTML;
import javax.swing.text.html.HTMLEditorKit;
import javax.swing.text.html.parser.ParserDelegator;

import org.apache.commons.codec.binary.Base64;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClientBuilder;

import util.JdbcLong;
import util.MyLogger;

public class Tempobet {
	private final static String USER_AGENT = "Mozilla/5.0 (X11; Ubuntu; Linux x86_64; rv:28.0) Gecko/20100101 Firefox/28.0";
	private static final MyLogger log = new MyLogger(3);
	private static int matchCount = 0;

	public Tempobet() {

	}

	public static String getContentResult(URL url) throws IOException {

		InputStream in = url.openStream();
		StringBuffer sb = new StringBuffer();

		byte[] buffer = new byte[256];

		while (true) {
			int byteRead = in.read(buffer);
			if (byteRead == -1)
				break;
			for (int i = 0; i < byteRead; i++) {
				sb.append((char) buffer[i]);
			}
		}
		return sb.toString();
	}

	public static StringBuilder insert;

	public static void getWeekend() {
		log.info("tempobet started");
		Long start = System.currentTimeMillis();
		StringBuffer result = getFromNet();
		// String result = getFromFile();

		Reader reader = new StringReader(result.toString());
		HTMLEditorKit.Parser parser = new ParserDelegator();
		insert = new StringBuilder();
		insert.append("insert ignore into `tempo`(externId,homeTeam,awayTeam,ht,at,draw,tarih,bahisSayi,sitename)"
				+ " values ");
		try {

			parser.parse(reader, new HTMLTableParser(), true);
			reader.close();
			insert.deleteCharAt(insert.length() - 1);

			log.info("-- insertin " + matchCount + " records for tempobet");

			insert.append(" on duplicate key update ht=Values(ht),at=values(at),draw=values(draw),bahisSayi=values(bahisSayi) ");

			JdbcLong.query(insert.toString());
			JdbcLong.close("getWeeken");

		} catch (IOException e) {
			throw new RuntimeException(e);
		}

	}

	public static class HTMLTableParser extends HTMLEditorKit.ParserCallback {

		private boolean encounteredATableRow = false;
		private int tdCount = 0;
		private String tarih, homeTeam, awayTeam;
		private int ht, at, draw, bahisSayi;

		private final String[] aylar = { "Oca", "Şub", "Mar", "Nis", "May",
				"Haz", "Tem", "Agu", "Eyl", "Eki", "Kas", "Ara" };
		private Map<String, Integer> months;
		private boolean error = false;

		public HTMLTableParser() {
			months = new HashMap<String, Integer>();
			int k = 0;
			for (String ay : aylar) {

				months.put(ay, ++k);

			}
		}

		public static void main(String[] args) {

			Tempobet.getWeekend();
			;

			Measure.dump();
		}

		private String genExternId(String str) {
			return new String(Base64.encodeBase64(str.getBytes()));
		}

		public void handleText(char[] data, int pos) {
			if (encounteredATableRow) {
				tdCount++;
				String string = new String(data);
				switch (tdCount) {
				case 1:

					String[] tar = string.split(" ");
					if (tar.length > 2) {
						String tarih = Calendar.getInstance()
								.get(Calendar.YEAR)
								+ "-"
								+ months.get(tar[1])
								+ "-" + tar[0] + " " + tar[2];
						this.tarih = tarih;
					} else {
						error = true;
					}

					break;
				case 2:

					try {
						String[] teams = string.split(" - ");
						this.homeTeam = getRidOfTr(teams[0]);
						this.awayTeam = getRidOfTr(teams[1]);
					} catch (Exception e) {
						this.error = true;

					}

					break;
				case 3:
					try {
						this.ht = (int) (Float.parseFloat(string) * 100);
					} catch (NumberFormatException e) {

						error = true;
					}

					break;

				case 4:
					try {
						this.draw = (int) (Float.parseFloat(string) * 100);
					} catch (NumberFormatException e) {

						error = true;
					}
				case 5:
					try {
						this.at = (int) (Float.parseFloat(string) * 100);
					} catch (NumberFormatException e) {

						error = true;
					}
					break;
				case 6:
					bahisSayi = Integer.parseInt(string);
				default:
					break;
				}

				// System.out.println(tdCount + ":" + string);
			}
		}

		private String getRidOfTr(String string) {

			String replace = string.replace('ç', 'c').replace('ş', 's')
					.replace('ö', 'o').replace('ü', 'u').replace('ğ', 'g')
					.replace('Ç', 'C').replace('Ş', 'S').replace('Ö', 'O')
					.replace('Ü', 'U').replace('Ğ', 'G').replace('ı', 'i')
					.replace('İ', 'I');

			return replace;
		}

		public void handleStartTag(HTML.Tag t, MutableAttributeSet a, int pos) {
			if (t == HTML.Tag.TR) {

				encounteredATableRow = true;
				tdCount = 0;
				error = false;
			}
		}

		public void handleEndTag(HTML.Tag t, int pos) {
			if (t == HTML.Tag.TR) {

				encounteredATableRow = false;
				if (error == false) {
					insert.append("('"
							+ genExternId(
									homeTeam + tarih
											+ this.getClass().getName())
									.replace('\'', ' ') + "','"
							+ homeTeam.replace('\'', ' ') + "','"
							+ awayTeam.replace('\'', ' ') + "'," + this.ht
							+ "," + this.at + "," + draw + ",'" + tarih + "',"
							+ bahisSayi + ",'tempo' ),");
					matchCount++;
				}
			}
		}
	}

	private static void writeToFile(String result) {
		PrintWriter writer;
		try {
			writer = new PrintWriter("target/tempobet.html", "UTF-8");

			writer.println(result);

			writer.close();
		} catch (FileNotFoundException | UnsupportedEncodingException e) {

			e.printStackTrace();
		}
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
		String url = "https://www.tempobet90.com/weekend_football.html";

		HttpClient client = HttpClientBuilder.create().build();
		HttpGet request = new HttpGet(url);
		StringBuffer result = null;
		// add request header
		request.addHeader("User-Agent", USER_AGENT);

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
