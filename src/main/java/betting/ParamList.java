package betting;

import java.util.ArrayList;
import java.util.List;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;

@SuppressWarnings("serial")
public class ParamList extends ArrayList<NameValuePair> {

	public boolean add(String key, String value) {
		return super.add(new BasicNameValuePair(key, value));

	}
}
