package live;

import java.awt.Toolkit;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonNumber;
import javax.json.JsonObject;
import javax.json.JsonReader;
import javax.json.JsonString;
import javax.json.JsonValue;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.SourceDataLine;

public class Pronet extends BuroBase {
	String url = "";
	private String site;

	public Pronet(String site) {
		this.site = site;
		this.url = "http://livesocket.pronetgaming.com/"
				+ site
				+ "?type=subscribe-live-overview&traderName="
				+ site
				+ "&languageId=1&firstConnection=1&X-Atmosphere-tracking-id=0&X-Atmosphere-Framework=2.0.5-jquery&X-Atmosphere-Transport=long-polling&X-Cache-Date=0";

	}

	@Override
	public List<Map<String, String>> getMatchs() {

		String fromNet = getFromNet(url).toString();
		
		String trim = fromNet.toString().trim();
		;
		String str = trim.substring(trim.indexOf("{"));

		saveToFile(new StringBuffer(str));
		JsonReader jsonReader = Json.createReader(new StringReader(str));

		JsonObject jsonObject = jsonReader.readObject();
		JsonArray ms = jsonObject.getJsonArray("ms");

		JsonObject ms0 = ms.getJsonObject(0);
		JsonObject football = ms0.getJsonArray("d").getJsonObject(0);
		JsonArray jsonArray = football.getJsonArray("cs");
		List<Map<String, String>> matchs = new ArrayList<Map<String, String>>();

		for (int i = 0; i < jsonArray.size(); i++) {
			JsonObject country = jsonArray.getJsonObject(i);
			JsonArray ligs = country.getJsonArray("s");
			for (int j = 0; j < ligs.size(); j++) {
				JsonObject lig = ligs.getJsonObject(j);
				JsonArray mathcs = lig.getJsonArray("f");
				for (int k = 0; k < mathcs.size(); k++) {
					Map<String, String> map = new HashMap<String, String>();
					JsonObject match = mathcs.getJsonObject(k);
					String hcn = match.getJsonString("hcn").getString();
					String acn = match.getJsonString("acn").getString();
					
					try {
						JsonNumber min = match.getJsonObject("match").getJsonNumber("mt");
						map.put("min", min.toString());
			
					} catch (Exception e) {
						map.put("min", "NULL");
					}
					
					JsonObject bet = match.getJsonArray("btgs")
							.getJsonObject(0).getJsonArray("odds")
							.getJsonObject(0);
					String ht = bet.getJsonString("hs").getString();
					String at = bet.getJsonString("as").getString();
					JsonNumber draw = bet.getJsonNumber("do");
					String draws = "";
					if (draw != null)
						draws = draw.toString();

					map.put("homeTeam", hcn);
					map.put("awayTeam", acn);
					String string = bet.getJsonNumber("ho").toString();
					if (string == null) {
						ht = ht;
						;
					}
					String htOdd = string.replaceAll("\\.", "");
					if (htOdd.length() < 3)
						htOdd += "0";
					JsonNumber jsonNumber = bet.getJsonNumber("ao");
					String atOdd = "";
					if (jsonNumber != null)
						atOdd = jsonNumber.toString().replaceAll("\\.", "");
					if (atOdd.length() < 3)
						atOdd += "0";
					String drawOdd = draws.replaceAll("\\.", "");
					if (drawOdd.length() < 3)
						drawOdd += "0";
					map.put("ht", htOdd);
					map.put("at", atOdd);
					map.put("draw", drawOdd);
					map.put("buro", site);
					if (!drawOdd.equals("0"))
						matchs.add(map);
				}
			}
		}

		return matchs;
	}
	
	public static void main(String[] args) throws LineUnavailableException, InterruptedException {
		 
		   
		 
		 
		   
	}
}
