package freela;

import java.io.Serializable;
import java.util.Map;

import javax.faces.bean.ApplicationScoped;
import javax.faces.bean.ManagedBean;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;

import util.JdbcLong;

@ManagedBean(name = "app", eager = true)
@ApplicationScoped
public class App implements Serializable {

	int callerCount = 0;
	public String siteUrl = "";
	public Map parameters;
	String cunrrentInfoMessage;

	public String getCunrrentInfoMessage() {
		return cunrrentInfoMessage;
	}

	public void setCurrentInfoMessage(String cunrrentInfoMessage) {
		this.cunrrentInfoMessage = cunrrentInfoMessage;
	}

	public App() {
		try {
			final FacesContext facesContext = FacesContext.getCurrentInstance();
			ExternalContext ctx = facesContext.getExternalContext();
			parameters = ctx.getInitParameterMap();

			Db.DB_URL=JdbcLong.DB_URL = parameters.get("jdbcurl").toString();

			Db.USER =JdbcLong.USER =parameters.get("jdbcuser").toString();

			if(Db.USER.equals("root"))
			{
				//Db.debug=true;
				FaceUtils.log.setLevel(MyLogger.INFO);
			}
			else{
				FaceUtils.log.setLevel(MyLogger.WARNING);
			}
			Db.PASS=JdbcLong.PASS = parameters.get("jdbcpassword").toString();

			//Db.debug = parameters.get("javax.faces.PROJECT_STAGE").equals(
			//		"Development");
	
			
			FaceUtils.SMTP_HOST_NAME=parameters.get("mailhost").toString();
			FaceUtils.SMTP_AUTH_USER=parameters.get("mailuser").toString();
			FaceUtils.SMTP_AUTH_PWD=parameters.get("mailpassword").toString();
			FaceUtils.emailFromAddress=parameters.get("mailuser").toString();

		} catch (Exception e) {
			FaceUtils.log.severe(e.getMessage());
		//	e.printStackTrace();

		}

	}

	public void out(String in) {
		System.out.println(in);
	}
}