package betting;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLDecoder;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.protocol.HTTP;
import org.apache.http.protocol.HttpCoreContext;

public class MyRequest {
	private static PoolingHttpClientConnectionManager cm = new PoolingHttpClientConnectionManager();
	private static CloseableHttpClient client = HttpClients.custom()
			.setConnectionManager(cm).build();
	public static String cookie = "";

	public static String get(String url) throws Exception {

		String URL = url;
		HttpGet post = new HttpGet(URL);

		post.setHeader("Host", new URL(URL).getHost());
		String USER_AGENT = "Mozilla/5.0 (X11; Linux x86_64; rv:34.0) Gecko/20100101 Firefox/34.0";
		post.setHeader("User-Agent", USER_AGENT);
		post.setHeader("Accept",
				"text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8");
		post.setHeader("Accept-Language", "en-US,en;q=0.5");
		post.setHeader("Cookie", cookie);
		post.setHeader("Connection", "keep-alive");
		post.setHeader("Referer", URL);
		post.setHeader("Content-Type", "application/x-www-form-urlencoded");

		HttpResponse response = client.execute(post);

		int responseCode = response.getStatusLine().getStatusCode();

		// log.info("..........Sending 'GET' request to URL : " + URL
		// + " with parameters: " + Arrays.asList(post.getAllHeaders())
		// + ", with response code: " + responseCode);

		StringBuffer result = new StringBuffer();
		try (BufferedReader br = new BufferedReader(new InputStreamReader(
				response.getEntity().getContent()))) {
			for (String line; (line = br.readLine()) != null;) {
				result.append(line);
			}
			br.close();
		}
		return result.toString();
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

	public static StringBuilder post(String url)
			throws Exception {

		String URL = url;
		HttpPost post = new HttpPost(URL);

		post.setHeader("Host", new URL(URL).getHost());

		String USER_AGENT = "Mozilla/5.0 (X11; Linux x86_64; rv:34.0) Gecko/20100101 Firefox/34.0";
		post.setHeader("User-Agent", USER_AGENT);

		post.setHeader("Accept",
				"text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8");

		post.setHeader("Accept-Language", "en-US,en;q=0.5");
	//	post.setHeader("Cookie", "ASP.NET_SessionId=usx2sk45rzh1pwn3xiigipcd45");
		post.setHeader("Connection", "keep-alive");
		post.setHeader("Referer",
				"http://www.wonclub.com/ComPg/Firm12.aspx?From=SPORTSBET&M=1&MF=1");
		post.setHeader("Content-Type", "text/plain; charset=utf-8");
		post.setHeader("X-AjaxPro-Method", "RefreshRateList");
		String pars = "{\"marketId\":1,\"marketFixedId\":1,\"countryId\":\"\","
				+ "\"leaguId\":\"\",\"timeName\":1,\"groupName\":0,\"searchText\":\"\"}";
		// System.out.println(pars);
		// if(true)return "";
		HttpEntity entity = new StringEntity(pars);
		HttpCoreContext localContext = new HttpCoreContext();

		post.setEntity(entity);

		HttpResponse response = client.execute(post, localContext);

		Header[] headers = localContext.getRequest().getAllHeaders();
	
		int responseCode = response.getStatusLine().getStatusCode();

		// log.info(".........Sending 'POST' request to URL : " + URL
		// + " with parameters: " + postParams + ", with response code: "
		// + responseCode);

		StringBuilder result = new StringBuilder();
		try (BufferedReader br = new BufferedReader(new InputStreamReader(
				response.getEntity().getContent()))) {
			for (String line; (line = br.readLine()) != null;) {
				result.append(line);
			}
			br.close();
		}
		return result.delete(0, 10);
	}
}
