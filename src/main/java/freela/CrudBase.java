package freela;

import java.util.List;
import java.util.Map;

import freela.Db;
import freela.FaceUtils;
import freela.Sql;
import freela.Sql.Update;

public class CrudBase {
	String table;
	public void setTable(String table) {
		this.table = table;
	}

	boolean hasMessage;
	String messageType;
	String message;
	String newCat;
	String editRowId = "0";
	List<ColumnModel> columns;
	List<Map<String, String>> data;
	Map<String, String> selected;

	

	public CrudBase() {

		// initColumns();
	}

	public void initColumns() {
		columns = new Sql.Select("id,header,name").from("gridfield")
				.where("tableName", this.table).and("state", "0").getType(ColumnModel.class);

	}

	public void delete() {
		new Sql.Delete(table).where("id", selected.get("id")).run();
		data.remove(selected);
		selected = null;
		FaceUtils.addInfo("Kayit Silindi");
	}

	public void updateColumns() {
		for (ColumnModel columnModel : columns) {

			Sql.Update update = new Sql.Update("gridfield").add("header",
					columnModel.getHeader());

			if (columnModel.getHeader() == null
					|| columnModel.getHeader().equals("")) {
				update.add("state", "1");

			}
			update.where("id", columnModel.getId());
			update.run();

		}
	}

	public void inform(String type, String message) {
		hasMessage = true;
		messageType = "alert_" + type;
		this.message = message;
	}

	public void warn(String message) {
		this.inform("warning", message);
	}

	public void error(String message) {
		this.inform("error", message);
	}

	public void info(String message) {
		this.inform("info", message);
	}

	public void success(String message) {
		this.inform("success", message);
	}

	public List<ColumnModel> getColumns() {
		return columns;
	}

	public void setColumns(List<ColumnModel> columns) {
		this.columns = columns;
	}

	public String getTable() {
		return table;
	}

	public boolean isHasMessage() {
		return hasMessage;
	}

	public String getNewCat() {
		return newCat;
	}

	public void setNewCat(String newCat) {
		this.newCat = newCat;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public void setHasMessage(boolean hasMessage) {
		this.hasMessage = hasMessage;
	}

	public String getMessageType() {
		return messageType;
	}

	public void setMessageType(String messageType) {
		this.messageType = messageType;
	}

	public String getMessage() {
		return message;
	}

	public void toggleRead(Object m) {
	}

	public void errorOccured() {
		this.error("Hata Oluştu.");

	}
	public List<Map<String, String>> getData() {
		return data;
	}

	public void setData(List<Map<String, String>> data) {
		this.data = data;
	}

	public Map<String, String> getSelected() {
		return selected;
	}

	public void setSelected(Map<String, String> selected) {
		this.selected = selected;
	}

	public String getEditRowId() {
		return editRowId;
	}

	public void setEditRowId(String editRowId) {
		this.editRowId = editRowId;
	}

	public String editRow(String id) {
		editRowId = id;
		return null;
	}

}