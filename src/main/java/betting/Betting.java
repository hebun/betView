package betting;

import java.io.Serializable;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

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

	private String console;

	public Betting(String console) {
		this.console = console;
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
		//SwingMain.populateDualMatchs();
		JdbcLong.query("insert into betfairupdate(type,tarih) values('FETCH',NOW())");
		JdbcLong.close("");

		initColumns();
		findProfitibleMatches();
	}

	public void increment() {

		loadList();
	}

	boolean emailSent = false;

	public void sendMail() {

		log.info("sending emails...");
		final String username = "itung73@gmail.com";
		final String password = "280682gmt";

		Properties props = new Properties();
		props.put("mail.smtp.auth", "true");
		props.put("mail.smtp.starttls.enable", "true");
		props.put("mail.smtp.host", "smtp.gmail.com");
		props.put("mail.smtp.port", "587");

		Session session = Session.getInstance(props,
				new javax.mail.Authenticator() {
					protected PasswordAuthentication getPasswordAuthentication() {
						return new PasswordAuthentication(username, password);
					}
				});

		try {

			MimeMessage message = new MimeMessage(session);
			message.setFrom(new InternetAddress("bfprogram@gmail.com"));
			message.setRecipients(Message.RecipientType.TO,
			// isatonk1@gmail.com,mazlumkaplan88@gmail.com,
					InternetAddress
							.parse("isatonk1@gmail.com,ismettung@gmail.com,mazlumkaplan88@gmail.com"));
			message.setSubject("betfair maclar");

			List<Map<String, String>> forMail = new ArrayList<Map<String, String>>();
			for (Map<String, String> map : profitibles) {
				LinkedHashMap<String, String> e = new LinkedHashMap<String, String>();

				e.put("Mac", map.get("bfMac"));

				String oranName = "";

				if (map.get("pcol").equals("ht")) {

					oranName = "1";

				} else if (map.get("pcol").equals("at")) {

					oranName = "2";

				} else if (map.get("pcol").equals("draw")) {

					oranName = "0";
				} else if (map.get("pcol").equals("under")) {

					oranName = "Alt";
				} else if (map.get("pcol").equals("over")) {

					oranName = "Ust";
				}
				e.put("Bahis", oranName);
				String pcol = map.get(map.get("pcol"));
				if (pcol.length() < 4) {
					pcol += "0";
				}
				String depcol = map.get("de" + map.get("pcol"));

				if (depcol.length() < 4) {
					depcol += "0";
				}

				e.put("Oran",
						"<span style='background-color: #D2EC6E;border-width: 1px;border-style: solid;'>"
								+ pcol
								+ "</span> "
								+ "<span style='background-color: #F3BE93;border-width: 1px;border-style: solid;'>"
								+ depcol + "</span>");
				e.put("Tarih", map.get("tarih"));
				e.put("Buro", map.get("buroTable"));
				e.put("BF Hacim", map.get("depth"));

				forMail.add(e);
			}

			StringBuilder content = new StringBuilder("<html>	<head>"
					+ "		<style>	.backblock{" + "	background-color:#d2ec6e;"
					+ "	width:50%;" + "	text-align:center;"
					+ "	font-weight:bold;" + "	padding:2px 2px 2px 2px ;"
					+ "	border-bottom:1px solid #8fa343;"
					+ "	border-right:1px solid #8fa343;"
					+ "	border-top:1px solid #e0eaba;"
					+ "	border-left:1px solid #e0eaba;" + "}" + ".layblock{"
					+ "	background-color:#f3be93;"
					+ "	padding:2px 2px 2px 2px ;" + "	width:50%;"
					+ "	text-align:center;" + "	font-weight:bold;"
					+ "	border-bottom:1px solid #b5845d;"
					+ "	border-right:1px solid #b5845d;"
					+ "	border-top:1px solid #f1e0d2;"
					+ "	border-left:1px solid #f1e0d2;" + "}" + "</style>" +

					"</head><body><table border='1'>");
			content.append("<tr>");
			for (Map<String, String> col : forMail) {

				for (Entry<String, String> colx : col.entrySet()) {

					content.append("<td><b>" + colx.getKey());
					content.append("</b></td>");

				}
				break;
			}
			content.append("</tr>");
			for (Map<String, String> map : forMail) {

				content.append("<tr>");
				for (Map.Entry<String, String> td : map.entrySet()) {

					content.append("<td>");

					content.append(td.getValue());

					content.append("</td>");

				}
				content.append("</tr>");

			}
			content.append("</table></body></html>");

			message.setText(content.toString(), "utf-8", "html");

			Transport.send(message);

			System.out.println("Done");

		} catch (MessagingException e) {
			throw new RuntimeException(e);
		}
	}

	public void loadList() {

		if (checkCall("FETCH", 5)) {

			// if (bt == null || bt.equals("") || bt.equals("tempo")) {
			Tempobet.getWeekend();
			// }
			// if (bt == null || bt.equals("") || bt.equals("imaj")) {
			Imajbet.getMatchs();

			// Youwin.getMatchs();

			// WonClub.getMatchs();

			Betfair.getMatchs();

		//	SwingMain.populateDualMatchs();
			JdbcLong.query("insert into betfairupdate(type,tarih) values('FETCH',NOW())");
			JdbcLong.close("");

		}
		initColumns();
		findProfitibleMatches();
		if (checkCall("MAIL", 120)) {
			sendMail();
			JdbcLong.query("insert into betfairupdate(type,tarih) values('MAIL',NOW())");
			JdbcLong.close("");
		}
		// try {
		// Thread.sleep(2000);
		// } catch (InterruptedException e) {
		// // TODO Auto-generated catch block
		// e.printStackTrace();
		// }
	}

	public static boolean checkCall(String string, int min) {
		List<Map<String, String>> dataTable = JdbcLong
				.select("select * from betfairupdate where type='" + string
						+ "' and tarih >= DATE_SUB(NOW(),INTERVAL " + min
						+ " MINUTE)");

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
		args = args;
		Betting betting = new Betting("console");
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
			checkPro(dual, "deunder", "under");
			checkPro(dual, "deover", "over");

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

			divideBy100(map, "deht");
			divideBy100(map, "ht");
			divideBy100(map, "at");
			divideBy100(map, "deat");
			divideBy100(map, "draw");
			divideBy100(map, "dedraw");
			divideBy100(map, "under");
			divideBy100(map, "deunder");
			divideBy100(map, "over");
			divideBy100(map, "deover");

		}
		log.info("-- found " + profitibles.size() + " profitible matchs ");
		// new ASCIITable().printTable(profitibles, true);
	//	Measure.dump();

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
