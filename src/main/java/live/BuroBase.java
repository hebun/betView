package live;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClientBuilder;

import com.sun.javafx.image.impl.ByteIndexed.Getter;

import util.MyLogger;

public abstract class BuroBase {
	protected final static String USER_AGENT = "Mozilla/5.0 (X11; Ubuntu; Linux x86_64; rv:28.0) Gecko/20100101 Firefox/28.0";
	protected static final MyLogger log = new MyLogger(1);

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

	protected void saveToFile(StringBuffer stringBuffer) {
		try {

			Files.write(Paths.get("./" + this.getClass().getName() + ".html"),
					stringBuffer.toString().getBytes());
		} catch (IOException e1) {

			e1.printStackTrace();
		}
	}

	protected String getFromFile() {
		try (BufferedReader br = new BufferedReader(new FileReader(""
				+ this.getClass().getName() + ".html"))) {
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

	protected StringBuffer getFromNet(String url) {
		log.info("getting " + this.getClass().getName() + " from net");
		HttpClient client = HttpClientBuilder.create().build();
		HttpGet request = new HttpGet(url);
		StringBuffer result = null;

		request.addHeader("User-Agent", USER_AGENT);

		request.addHeader("Referer", url);

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

			e.printStackTrace();
		}
		return result;
	}

	public abstract List<Map<String, String>> getMatchs();
}
