package betting;

import java.util.List;
import java.util.Map;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;

import freela.FaceUtils;
import freela.Sql;
import freela.Sql.Update;

@ManagedBean
@ViewScoped
public class EnterIp {
	String message;

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public EnterIp() {
		this.setIp(FaceUtils.getIp());
	}

	public String updateIp() {
		List<Map<String, String>> table = new Sql.Select().from("iptable")
				.where("name", username).getTable();
		if (table == null || table.size() == 0) {
			this.message = "Kullanici Bulunamadi";
			return null;

		}

		Update where = new Sql.Update("iptable").add("ip", ip).where("name",
				username);
		if (where.run() > 0) {
			message = "Ip guncellendi";
			return null;
		}
		message = "Ip guncellenirken hata olustu.";
		return null;

	}

	private String username, ip;

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getIp() {
		return ip;
	}

	public void setIp(String ip) {
		this.ip = ip;
	}
}
