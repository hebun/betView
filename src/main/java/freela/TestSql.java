package freela;

import static org.junit.Assert.*;

import java.util.List;
import java.util.Map;

import org.junit.Test;

import freela.Sql.Delete;
import freela.Sql.Insert;
import freela.Sql.Select;
import freela.Sql.Update;
import static freela.Sql.*;

public class TestSql {
/**
 * SELECT message, day( tarih ) , count( 0 )
FROM log
GROUP BY message, YEAR( tarih ) , MONTH( tarih ) , DAY( tarih )
ORDER BY id DESC
LIMIT 0 , 30
 */
	public void back() {

		int ret = new Insert("test").add("test", "blblba").run();
		assertTrue(ret > 0);

		ret = new Insert("test").add("test", "pppp").run();
		assertTrue(ret > 0);

		ret = new Update("test").add("test", "update").where("id", ret).run();
		assertTrue(ret > 0);
		ret = new Update("test").add("test", "updatez").where("id", 3).run();
		assertTrue(ret > 0);

		ret = new Update("test").add("test", "updatev").where("id", 3).run();
		assertTrue(ret > 0);

		ret = new Delete("test").where("id", ret).run();

		List<Map<String, String>> table = ((Select) new Select().from("test")
				.where("id>", "7")).getTable();

		assertTrue(ret > 0);
	

	}

	@Test
	public void test() {
		int i = new Sql.Count("user").get();
		
		System.out.println(i+"");

	}

	private void bla(String name) {
		// TODO Auto-generated method stub

	}

	private void bla(Class<?> class1) {
		// TODO Auto-generated method stub

	}
}
