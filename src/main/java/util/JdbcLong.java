package util;

//STEP 1. Import required packages
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;


public class JdbcLong {
	// JDBC driver name and database URL
	static final String JDBC_DRIVER = "com.mysql.jdbc.Driver";
	public static String DB_URL = "jdbc:mysql://localhost:3306/betting?useUnicode=true&characterEncoding=utf8";
	static Connection conn = null;
	static Statement stmt = null;

	// Database credentials
	static public String USER = "root";
	static public String PASS = "2882";
	public static boolean started = false;
	private final static MyLogger log = new MyLogger(MyLogger.INFO);

	public static void start(String caller) {
		try {
			Class.forName("com.mysql.jdbc.Driver");

			// STEP 3: Open a connection
			// System.out.println("connecting long "+caller);
			conn = DriverManager.getConnection(DB_URL, USER, PASS);

			stmt = conn.createStatement();

		} catch (ClassNotFoundException e) {

			e.printStackTrace();
		} catch (SQLException e) {

			e.printStackTrace();
		}
		started = true;
	}
	public static void transStartNormal(String caller) {
		try {
			Class.forName("com.mysql.jdbc.Driver");

			// STEP 3: Open a connection
			// System.out.println("connecting long "+caller);
			conn = DriverManager.getConnection(DB_URL, USER, PASS);

			stmt = conn.createStatement();
			conn.setAutoCommit(false);

		} catch (ClassNotFoundException e) {

			e.printStackTrace();
		} catch (SQLException e) {

			e.printStackTrace();
		}
		started = true;
	}
	public static void close(String caller) {
		// System.out.println("closing long..."+caller);
		try {
			stmt.close();
			conn.close();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		started = false;
	}
	public static void transClose(String caller) {
		// System.out.println("closing long..."+caller);
		try {
			conn.commit();
			stmt.close();
			conn.close();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		started = false;
	}

	public static int say = 0;
	private static Connection conn2;
	private static PreparedStatement statement2;

	public static void query(String sql) {
		if (true) {
			// System.out.println(sql);
			long start = System.currentTimeMillis();
			if (!started)
				start("query not started");
			try {
				say++;
				if (say % 100 == 0)
					System.out.print(say + ".");

				int rs = stmt.executeUpdate(sql);

			} catch (SQLException se) {
				log.info(se.getMessage()+":"+ sql);
				throw new RuntimeException(se);
			} catch (Exception e) {
				log.info(sql);
				throw new RuntimeException(e);
			} finally {

			}// end try
				// System.out.print(" qt" + (System.currentTimeMillis() - start)
				// +
				// ":");
		}
	}

	public static void prepareStart(String sql) {

		try {
			Class.forName("com.mysql.jdbc.Driver");

			conn = DriverManager.getConnection(DB_URL, USER, PASS);
		

			statement = conn.prepareStatement(sql);

		} catch (Exception ex) {
			System.out.println(ex.getMessage());
			log.warning(ex.getMessage());

		}
	}
	public static void transStart(String sql) {

		try {
			Class.forName("com.mysql.jdbc.Driver");

			conn = DriverManager.getConnection(DB_URL, USER, PASS);
		

			statement = conn.prepareStatement(sql);
			conn.setAutoCommit(false);
			

		} catch (Exception ex) {
			System.out.println(ex.getMessage());
			log.warning(ex.getMessage());

		}
	}
	public static void transClose() {

		try {
			conn.commit();
			conn.close();
			statement.close();
		} catch (Exception ex) {
			System.out.println(ex.getMessage());
		}
	}
	public static void prepareClose() {

		try {
			conn.close();
			statement.close();
		} catch (Exception ex) {
			System.out.println(ex.getMessage());
		}
	}

	static PreparedStatement statement = null;

	public static void prepareInsert(List<String> params) {

		try {
			// statement.clearParameters();
			for (int i = 1; i <= params.size(); i++) {
			
					statement.setString(i, (params.get(i - 1)));
				

				
			}
	
			 int efectedRows;
			
			efectedRows = statement.executeUpdate();
		
		} catch (Exception ex) {
			log.warning(ex.getMessage());

		}

	}

	public static List<Map<String, String>> select(String sql) {
		if (false == started)
			start("select not started");
		List<Map<String, String>> list = null;
		try {
			say++;
			if (say % 100 == 0)
				System.out.print(say + ".");

			ResultSet rs = stmt.executeQuery(sql);
			ResultSetMetaData data = rs.getMetaData();
			int colCount = data.getColumnCount();
			list = new ArrayList<Map<String, String>>();
			while (rs.next()) {

				Hashtable<String, String> hash = new Hashtable<String, String>();
				for (int i = 1; i <= colCount; i++) {

					String value = rs.getString(i) == null ? "NULL" : rs
							.getString(i);
					hash.put(data.getColumnLabel(i), value);
				}
				list.add(hash);

			}
		} catch (SQLException se) {

			se.printStackTrace();
		} catch (Exception e) {

			e.printStackTrace();
		} finally {

		}// end try
		return list;
	}
}// end FirstExample