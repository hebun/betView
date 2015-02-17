package freela;

import static freela.FaceUtils.*;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import freela.FaceUtils;
import static freela.Sql.*;

@WebFilter(filterName = "AuthFilter", servletNames = "Faces Servlet")
public class AuthFilter implements Filter {

	private static final String REMEMBER_COOKIE = "remember";
	private static final String USER_TABLE = "user";
	private static final String LOGIN_PAGE = "/kullanici-giris";
	private static final String[] MEMBER_PAGES = { "urunlerim", "urun-ekle",
			"uye-profil" };

	static boolean devStage = false;

	public AuthFilter() {
	}

	@Override
	public void init(FilterConfig filterConfig) throws ServletException {

	}

	@SuppressWarnings("unchecked")
	@Override
	public void doFilter(ServletRequest request, ServletResponse response,
			FilterChain chain) throws IOException, ServletException {

		try {
			HttpServletRequest req = (HttpServletRequest) request;
			HttpServletResponse res = (HttpServletResponse) response;
			HttpSession ses = req.getSession(false);

			String reqURI = req.getRequestURI();

			Map<String, String> user = (Map<String, String>) req.getSession()
					.getAttribute(USER_TABLE);

			if (user == null) {
				String uuid = getCookieValue(req, REMEMBER_COOKIE);

				if (uuid != null) {
					List<Map<String, String>> users = new Select()
							.from(USER_TABLE).where("uuid", uuid).getTable();

					if (users.size() > 0) {
						user = users.get(0);

						req.getSession().setAttribute(USER_TABLE, user); //
						addCookie(res, REMEMBER_COOKIE, uuid, 100_000_000);

					} else {

						removeCookie(res, REMEMBER_COOKIE);
					}
				}
			}

			if (user != null) {
				chain.doFilter(request, response);
			} else {

				if (devStage) {
					chain.doFilter(request, response);
				} else {

					if (adminControle(req, res, ses, reqURI)) {

						if (memberControle(req, res, ses, reqURI)) {

							chain.doFilter(request, response);
						}
					}
				}
			}
		} catch (Throwable t) {
			FaceUtils.log.info(t.getMessage());
			t.printStackTrace();
		}

	}

	@SuppressWarnings("unchecked")
	private boolean memberControle(HttpServletRequest req,
			HttpServletResponse res, HttpSession ses, String reqURI)
			throws IOException {

	
		for (String string : MEMBER_PAGES) {

			if (reqURI.indexOf(string) >= 0) {

				Map<String, String> user = (Map<String, String>) req
						.getSession().getAttribute(USER_TABLE);
				if (user == null) {

					res.sendRedirect(req.getContextPath() + LOGIN_PAGE);
					return false;

				} 
			}
		}

		return true;

	}

	@SuppressWarnings("unchecked")
	private boolean adminControle(HttpServletRequest req,
			HttpServletResponse res, HttpSession ses, String reqURI)
			throws IOException {
		/**
		 * admin access only
		 */

		if (reqURI.indexOf("/admin") >= 0 && false/* admin check disabled */) {

			HttpSession session = req.getSession();
			if (session == null || session.getAttribute(USER_TABLE) == null) {
				FaceUtils.log.warning("not authorized attempt to access admin/"
						+ session.getAttribute(USER_TABLE));

				res.sendRedirect(req.getContextPath() + LOGIN_PAGE);
				return false;
			} else {
				Map<String, String> user = (Map<String, String>) session
						.getAttribute(USER_TABLE);
				System.out.println(user);
				if (!user.get("state").equals("ADMIN")) {
					FaceUtils.log
							.warning("Not authorized attempt to access admin/");

					res.sendRedirect(req.getContextPath() + LOGIN_PAGE);
					return false;
				}
			}

		}

		return true;
	}

	@Override
	public void destroy() {

	}
}