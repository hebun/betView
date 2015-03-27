package betting;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class Datatable extends ArrayList<HashMap<String, String>> {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	public Datatable() {
		
	}
	public String get(int index,String key){
		return this.get(index).get(key);
	}

}
