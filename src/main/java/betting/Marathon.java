package betting;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import javax.sound.sampled.LineUnavailableException;

import live.BuroBase;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class Marathon extends BuroBase {

	private String url;

	public Marathon() {
		this.url = "https://www.marathonbet.com/tr/betting/11?periodGroupAllEvents=24";
	}

	@Override
	public List<Map<String, String>> getMatchs() {
		StringBuffer fromNet = this.getFromNet(this.url);

		Document doc = Jsoup.parse(fromNet.toString());

		Elements rows = doc.getElementsByAttributeValue("data-mutable-id",
				"mainRowFirstCouponeRow");
		int k = 0;

		List<Map<String, String>> datatable = new ArrayList<Map<String, String>>();

		for (Element element : rows) {
			HashMap<String, String> map = new HashMap<String, String>();
			Elements names = element.getElementsByClass("today-member-name");
			if (names.size() > 1) {
				String homeTeam = names.get(0).text();
				map.put("homeTeam", homeTeam);

				//System.out.print(homeTeam + " VS ");
				String awayTeam = names.get(1).text();
				//System.out.print(awayTeam + " ");
				map.put("awayTeam", awayTeam);
				Elements odds = element.getElementsByClass("selection-link");
				String ht = "0", at = "0", draw = "0";
				for (Element element2 : odds) {
					String text = element2.text();
					if (k == 0) {
						ht = text;
					} else if (k == 1) {
						draw = text;
					} else if (k == 2) {
						at = text;
					}
					k++;
				}
				k=0;
				map.put("ht", ht.replaceAll(Pattern.quote("."), ""));
				map.put("at", at.replaceAll(Pattern.quote("."), ""));
				map.put("draw", draw.replaceAll(Pattern.quote("."), ""));
				map.put("buro", "marathon");
				datatable.add(map);
			//	System.out.println();
			}
		}

		saveToFile(fromNet);
		return datatable;

	}

	public static void main(String[] args) throws LineUnavailableException,
			InterruptedException {

		new Marathon().getMatchs();

	}
}
