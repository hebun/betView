package freela;

import static freela.FaceUtils.log;

import java.util.Arrays;
import java.util.List;
import java.util.Map;


public class BaseBean {

	protected List<Map<String, String>> data;
	protected Map<String, String> record;
	protected String table;
	protected List<Map<String, String>> allData;
	protected int currentPage;

	public int getCurrentPage() {
		return currentPage;
	}

	public void setCurrentPage(int currentPage) {
		this.currentPage = currentPage;
	}

	public List<Map<String, String>> getAllData() {
		return allData;
	}

	public void setAllData(List<Map<String, String>> allData) {
		this.allData = allData;
	}

	public void loadDataBase() {
		data = Db.selectFrom(table);
	}

	protected int pageCount;

	public int getPageCount() {
	
		return pageCount;
	}

	public Object[] createArray(int size) {
		return new Object[size];
	}

	public void setPageCount(int pageCount) {
		this.pageCount = pageCount;
	}

	public String delete(String id) {

		Db.prepareInsert("delete from `" + table + "` where id=?",
				Arrays.asList(new String[] { id }));
		return "";
	}

	public void loadData(String column, String value) {

		if (this.table == null) {
			throw new RuntimeException("first, set 'table' in baseBean");
		}

		data = Db.preparedSelect("select * from `" + table + "` where `"
				+ column + "`=?", Arrays.asList(new String[] { value }));
	}

	public BaseBean() {
		this.currentPage = 0;
	}

	public String getTable() {

		return table;
	}

	public void setTable(String table) {
		this.table = table;
	}

	public List<Map<String, String>> getData() {
		return data;
	}

	public void setData(List<Map<String, String>> data) {
		this.data = data;
	}

	public Map<String, String> getRecord() {
		return record;
	}

	public void setRecord(Map<String, String> record) {
		this.record = record;
	}

	public void loadData() {

		pageCount = allData.size() / 20;
	
		if (allData.size() % 20 > 0) {
			pageCount++;
		
		}
		this.loadData(currentPage++);

	}

	public void loadData(int page) {
		this.currentPage = page;
		log.info("pagecoun:" + pageCount + " ");
		if (this.pageCount <= 1)
			this.data = this.allData;
		else {
			int toIndex = 20 * (page + 1);

			if (toIndex >= this.allData.size()) {

				toIndex = this.allData.size();

			}
			int fromIndex = page * 20;
			log.info("fromindex:" + fromIndex);
			log.info("toindex:" + toIndex);

			this.data = this.allData.subList(fromIndex, toIndex);
		}

	}
}
