package live;

import java.util.List;
import java.util.Map;

public class Artemis extends BuroBase {
String url= "https://sportsfrontlive.artemisbet1.com/fe_liveHome?lang=tr_TR";
	@Override
	public List<Map<String, String>> getMatchs() {
		StringBuffer fromNet = getFromNet(url);
		saveToFile(fromNet);
		
		return null;
	}
	public static void main(String[] args) {
		new Artemis().getMatchs();
	}

}
