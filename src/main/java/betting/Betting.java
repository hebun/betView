package betting;

import java.io.Serializable;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import javax.faces.component.behavior.AjaxBehavior;

import util.JdbcLong;
import freela.CrudBase;
import freela.FaceUtils;
import freela.Measure;
import freela.MyLogger;
import freela.Sql;
import freela.Sql.Select;

@ManagedBean
@ViewScoped
public class Betting extends CrudBase implements Serializable {
	// multi threading

	/**
	 * 10 dkkada bir kendisi guncellesin. %3 ustu varsa mail/tel uyari atsin.
	 * grid sutun genislikleri farkli olsun
	 */

	private static final long serialVersionUID = 1120356482214096067L;

	private boolean loading = true;

	public boolean isLoading() {
		return loading;
	}

	public void setLoading(boolean loading) {
		this.loading = loading;
	}

	@SuppressWarnings("rawtypes")
	public Map parameters;

	private String ssoid;

	private String user = "";

	public String getUser() {
		return user;
	}

	public void setUser(String user) {
		this.user = user;
	}

	private List<Map<String, String>> profitibles;

	private MyLogger log = new MyLogger(3);

	public Betting(String console) {
		this.setTable("dualView");
	}

	public Betting() {

		log.info("construc");
		this.setTable("dualView");

		String odd = FaceUtils.getGET("odd");
		String bt = FaceUtils.getGET("bt");
		String ip = FaceUtils.getIp();

		bt = bt == null ? "" : bt;
		odd = odd == null ? "" : odd;
		FaceUtils.setSession("bt", bt);
		FaceUtils.setSession("odd", odd);

		new Sql.Insert("log").add("level", "ACCESS").add("message", ip)
				.add("method", "").run();

		//
		// if (checkIp(ip)) {
		//
		// List<Map<String, String>> tableIp = new
		// Select("name").from("iptable").where("ip", ip).getTable();
		// String name="";
		// if(tableIp.size()>0){
		// name=tableIp.get(0).get("name");
		// }
		// new Sql.Insert("log").add("level", "ACCESS")
		// .add("message", ip).add("method", name).run();
		// } else {
		// new Sql.Insert("log").add("level", "UA")
		// .add("message", ip).run();
		// }
	}

	public void fetchNow() {

		// if (bt == null || bt.equals("") || bt.equals("tempo")) {
		Tempobet.getWeekend();
		// }
		// if (bt == null || bt.equals("") || bt.equals("imaj")) {
		Imajbet.getMatchs();

		Youwin.getMatchs();
		
		WonClub.getMatchs();
		
		Betfair.getMatchs();
		SwingMain.populateDualMatchs();
		JdbcLong.query("insert into betfairupdate(type,tarih) values('FETCH',NOW())");
		JdbcLong.close("");

		initColumns();
		findProfitibleMatches();
	}

	public void increment() {

		loadList();
	}

	public void loadList() {

		if (checkCall("FETCH")) {

			// if (bt == null || bt.equals("") || bt.equals("tempo")) {
			Tempobet.getWeekend();
			// }
			// if (bt == null || bt.equals("") || bt.equals("imaj")) {
			Imajbet.getMatchs();

			Youwin.getMatchs();
			
			WonClub.getMatchs();
			
			Betfair.getMatchs();
			
			SwingMain.populateDualMatchs();
			JdbcLong.query("insert into betfairupdate(type,tarih) values('FETCH',NOW())");
			JdbcLong.close("");
		}
		initColumns();
		findProfitibleMatches();

		// try {
		// Thread.sleep(2000);
		// } catch (InterruptedException e) {
		// // TODO Auto-generated catch block
		// e.printStackTrace();
		// }
	}

	public static boolean checkCall(String string) {
		List<Map<String, String>> dataTable = JdbcLong
				.select("select * from betfairupdate where type='" + string
						+ "' and tarih >= DATE_SUB(NOW(),INTERVAL 5 MINUTE)");

		return dataTable.size() == 0;

	}

	public boolean checkIp(String string) {
		List<Map<String, String>> dataTable = JdbcLong
				.select("select * from iptable where ip='" + string + "'");

		if (dataTable.size() != 0) {
			user = dataTable.get(0).get("name").toString();
			return true;
		}
		return false;

	}

	public static void main(String[] args) {
		Betting betting = new Betting("blba");
		betting.loadList();
		ASCIITable.printTableS(betting.getProfitibles());
	}

	private int withOdd = 0;

	public void findProfitibleMatches() {
		log.info("started find findProfitibleMatches ");

		Select dualView = new Sql.Select().from("dualView");

		String bt = FaceUtils.getSession("bt").toString();

		if (bt != null && !bt.equals("")) {
			dualView.where("buroTable", bt);
		}
		List<Map<String, String>> dualMatchs = dualView.getTable();

		String odd = FaceUtils.getSession("odd").toString();

		if (odd != null && !odd.equals("")) {
			withOdd = Integer.parseInt(odd);
		}

		log.info("-- fetched " + dualMatchs.size() + " matchs from dualView");

		profitibles = new ArrayList<Map<String, String>>();

		for (final Map<String, String> dual : dualMatchs) {

			String tarih = dual.get("tarih");
			dual.put("tarih", tarih.substring(5, tarih.length() - 5));
			checkPro(dual, "deht", "ht");

			checkPro(dual, "deat", "at");
			checkPro(dual, "dedraw", "draw");

		}

		profitibles.sort(new Comparator<Map<String, String>>() {

			@Override
			public int compare(Map<String, String> o1, Map<String, String> o2) {
				Double ap = Double.parseDouble(o1.get("profit"));
				Double bp = Double.parseDouble(o2.get("profit"));

				return ap > bp ? -1 : ap == bp ? 0 : 1;

			}
		});
		for (Map<String, String> map : profitibles) {
			String col = "deht";

			divideBy100(map, col);
			divideBy100(map, "ht");
			divideBy100(map, "at");
			divideBy100(map, "deat");
			divideBy100(map, "draw");
			divideBy100(map, "dedraw");

		}
		log.info("-- found " + profitibles.size() + " profitible matchs ");
		// new ASCIITable().printTable(profitibles, true);
		Measure.dump();

	}

	private void divideBy100(Map<String, String> map, String col) {
		String value = map.get(col);
		float fv = Float.parseFloat(value) / 100;
		map.put(col, fv + "");
	}

	private void checkPro(Map<String, String> dual, String deodd, String odd) {
		double oldHt = (double) Integer.parseInt(dual.get(odd));
		double ht = oldHt / 100;
		if (withOdd > 0) {
			if (oldHt < withOdd)
				return;
		}
		double deht = (double) Integer.parseInt(dual.get(deodd)) / 100;
		// deht=deht*1.004;
		if (ht > deht || (deht - ht) < 0.2) {

			double fark = (double) (ht - deht);
			double pro = fark / (ht * deht - fark);
			double round = round(pro * 100, 2);
			if (round > 10.0)
				return;
			Map<String, String> hm = new LinkedHashMap<String, String>();
			hm.putAll(dual);

			// deht = deht / 100;
			// ht = ht / 100;
			// double commision = 0.04;
			// double newPro = (ht * (deht + deht*commision) -
			// deht+deht*commision-ht * (deht - 1))
			// / (deht - deht*commision + ht * (deht - 1));
			//
			// newPro=(ht*(deht-commision))/(deht-commision+ht*(deht-1));
			//
			// newPro = round(newPro, 2);

			hm.put("profit", round + "");
			hm.put("pcol", odd);
			profitibles.add(hm);

		}
	}

	public static double round(double value, int places) {
		if (places < 0)
			throw new IllegalArgumentException();

		BigDecimal bd = new BigDecimal(value);
		bd = bd.setScale(places, RoundingMode.HALF_UP);
		return bd.doubleValue();
	}

	public List<Map<String, String>> getProfitibles() {
		return profitibles;
	}

	public void setProfitibles(List<Map<String, String>> profitibles) {
		this.profitibles = profitibles;
	}

	public String updateSsoid() {

		ssoid = "";
		return null;
	}

	public String getSsoid() {
		return ssoid;
	}

	public void setSsoid(String ssoid) {
		this.ssoid = ssoid;
	}

}
