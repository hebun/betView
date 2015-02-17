package freela;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

public abstract class Sql<T extends Sql<T>> {
	protected String fieldList;

	public static final String ISNULL = " is null ";
	protected String tableName;
	protected boolean isBuilt = false;
	protected String currentSql = "";
	protected String orderColumn = null;
	protected String orderWay = "asc";
	protected int start = 0, count = 0;
	protected Map<String, Map.Entry<String, String>> where = new Hashtable<String, Map.Entry<String, String>>();
	protected boolean isPrepared = true;

	public T doNotUsePrepared() {
		this.isPrepared = false;
		return thisAsT;
	}

	@SuppressWarnings("unchecked")
	private final T thisAsT = (T) this;

	public T whereEntry(String type, String key, final Object value) {

		char charAt = key.charAt(key.length() - 1);
		if (charAt != ' ' && charAt != '<' && charAt != '>' && charAt != '=') {
			key = key + "=";
		}
		final String fkey = key;
		if (where.containsKey(type))
			type = type + " ";
		where.put(type, new Map.Entry<String, String>() {

			@Override
			public String getKey() {
				return fkey;
			}

			@Override
			public String getValue() {

				if (value == null)
					throw new RuntimeException("SQL: the value of key '" + fkey
							+ "' is null in where statement");
				return value.toString();
			}

			@Override
			public String setValue(String value) {

				return null;
			}
		});

		return thisAsT;
	}

	public T where(final String key, final Object value) {

		return whereEntry("where", key, value);
	}

	public T and(final String key, final String value) {
		if (where.size() == 0)
			throw new RuntimeException("cant use 'and' before 'where'");
		return whereEntry("and", key, value);
	}

	public T or(final String key, final String value) {
		if (where.size() == 0)
			throw new RuntimeException("cant use 'or' before 'where'");

		return whereEntry("or", key, value);
	}

	public T desc() {
		this.orderWay = "desc";
		return thisAsT;
	}

	public T limit(int start, int count) {
		this.start = start;
		this.count = count;
		return thisAsT;
	}

	public T limit(int count) {

		return this.limit(0, count);
	}

	public String getFollowings() {
		String ret = "";

		if (orderColumn != null) {
			ret += " order by " + orderColumn + " " + orderWay;
		}
		if (count != 0) {
			ret += " limit " + start + "," + count;
		}
		return ret;
	}

	public abstract String get();

	public static class Delete extends Sql<Delete> {

		public Delete(String table) {
			this.tableName = table;

		}

		@Override
		public String get() {
			if (isBuilt)
				return currentSql;
			if (where.size() == 0)
				throw new RuntimeException("cant use delete without where");
			StringBuilder builder = new StringBuilder("delete from  `");
			builder.append(this.tableName);
			builder.append("`");
			for (Map.Entry<String, Map.Entry<String, String>> en : where
					.entrySet()) {
				builder.append(' ').append(en.getKey()).append(' ')
						.append(en.getValue().getKey());

				builder.append("'").append(en.getValue().getValue())
						.append("' ");

			}

			this.currentSql = builder.toString();
			this.isBuilt = true;
			this.currentSql += super.getFollowings();
			return currentSql;
		}

		public int run() {

			return Db.delete(this.get());

		}

	}

	public static class Update extends Sql<Update> {
		Map<String, Object> fields = new Hashtable<String, Object>();

		public Update(String table) {
			this.tableName = table;

		}

		public List<String> params() {
			List<String> ret = new ArrayList<>();
			for (Object string : fields.values()) {
				ret.add(string.toString());
			}

			for (Map.Entry<String, Map.Entry<String, String>> en : where
					.entrySet()) {

				ret.add(en.getValue().getValue());
			}

			return ret;

		}

		@Override
		public String get() {
			if (isBuilt)
				return currentSql;
			if (where.size() == 0)
				throw new RuntimeException("cant use update without where");

			if (fields.size() <= 0) {
				throw new RuntimeException(
						"Sql.Update:there is no column to set  ");
			}

			StringBuilder builder = new StringBuilder("update ");
			builder.append(this.tableName);
			builder.append(" set ");
			for (Map.Entry<String, Object> en : fields.entrySet()) {
				builder.append(en.getKey()).append("=");

				if (isPrepared) {
					builder.append("?,");
				} else {

					builder.append("'").append(en.getValue().toString())
							.append("',");
				}
			}
			builder.deleteCharAt(builder.length() - 1);
			for (Map.Entry<String, Map.Entry<String, String>> en : where
					.entrySet()) {
				builder.append(' ').append(en.getKey()).append(' ')
						.append(en.getValue().getKey());
				if (isPrepared) {
					builder.append("? ");
				} else {
					builder.append("'").append(en.getValue().getValue())
							.append("' ");
				}
			}

			this.currentSql = builder.toString();
			this.currentSql += super.getFollowings();
			this.isBuilt = true;
			return currentSql;
		}

		public Update add(String key, Object value) {

			this.fields.put(key, value);
			return this;

		}

		public int run() {
			if (isPrepared) {
				return Db.prepareInsert(this.get(), this.params());
			} else {
				return Db.update(this.get());
			}
		}

	}

	public static class Insert extends Sql<Insert> {
		Map<String, Object> fields = new Hashtable<String, Object>();
String ignore="";
		public Insert(String table) {
			this.tableName = table;

		}

		public Insert ignore() {
			ignore="ignore";
			return this;
		}

		public List<String> params() {
			List<String> ret = new ArrayList<>();
			for (Object string : fields.values()) {
				ret.add(string.toString());
			}
			return ret;

		}

		public Insert add(String key, Object value) {

			if (value == null) {
				throw new RuntimeException("the value for key '" + key
						+ "' is null");
			}
			this.fields.put(key, value);
			return this;

		}

		public Insert addAll(Map<String, Object> fields) {

			if (fields == null) {
				throw new RuntimeException("the map for inserts  is null");
			}
			this.fields.putAll(fields);

			return this;

		}

		@Override
		public String get() {
			if (isBuilt)
				return currentSql;
			StringBuilder builder = new StringBuilder("insert "+ignore+" into ");
			builder.append("`" + this.tableName + "`");
			builder.append("(");
			for (Map.Entry<String, Object> en : fields.entrySet()) {
				builder.append(en.getKey());
				builder.append(",");
			}
			builder.deleteCharAt(builder.length() - 1);
			builder.append(") values (");
			for (Map.Entry<String, Object> en : fields.entrySet()) {

				if (isPrepared) {
					builder.append("?,");

				} else {
					builder.append("'").append(en.getValue().toString())
							.append("',");
				}
			}
			builder.deleteCharAt(builder.length() - 1);
			builder.append(")");
			this.currentSql = builder.toString();
			this.currentSql += super.getFollowings();
			this.isBuilt = true;
			return currentSql;
		}

		public int run() {
			
			if (isPrepared) {
				return Db.prepareInsert(this.get(), this.params());
			} else {
				return Db.insert(this.get());
			}
		}

	}

	public static class Count {
		String table;
		String key, value;

		public Count(String table) {
			this.table = table;
		}

		public Count where(String key, String value) {
			this.key = key;
			this.value = value;
			return this;
		}

		public int get() {
			if (key == null || value == null) {
				key = "1";
				value = "1";
			}
			Select sql = new Sql.Select("count(*) as say").from(this.table)
					.where(key, value);

			String say = sql.getTable().get(0).get("say");
			int parseInt = 0;
			try {
				parseInt = Integer.parseInt(say);
			} catch (NumberFormatException e) {
				e.printStackTrace();
			}
			return parseInt;

		}
	}

	public static class Select extends Sql<Select> {

		public static final String COUNT = " count(id) as say ";

		String tableAlias;
		String onKey, onValue, onKey2, onValue2;
		String joinType, secondJoinType;
		String groupBy;
		List<Join> joins = new ArrayList<Sql.Join>();

		public Select(String params) {
			setFields(params);

		}

		public Select() {
			this("*");

		}

		public Select as(String al) {
			this.tableAlias = al;
			return this;
		}

		public Select asFirst() {
			this.tableAlias = this.tableName.substring(0, 1);
			return this;
		}

		public Select groupBy(String f) {
			this.groupBy = f;
			return this;
		}

		public Join join(String tablej) {
			Join join = new Join(tablej, this);
			this.joins.add(join);
			return join;
		}

		public Select setFields(String params) {
			this.isBuilt = false;
			fieldList = params;

			return this;
		}

		public Select from(String table) {
			this.isBuilt = false;
			tableName = table;

			return this;
		}

		public List<String> params() {
			if (!isPrepared) {
				throw new RuntimeException("This is not a prepared statement");
			}
			List<String> ret = new ArrayList<>();
			for (Map.Entry<String, String> entry : where.values()) {
				ret.add(entry.getValue());
			}
			return ret;

		}

		@Override
		public String get() {

			StringBuilder builder = new StringBuilder("select ");
			builder.append(fieldList);

			if (tableName != null && tableName != "") {

				builder.append(" from `").append(this.tableName).append("` ");
				if (tableAlias != null && !tableAlias.equals("")) {
					builder.append(" as " + tableAlias);
				}
				for (Join join : joins) {
					builder.append(" " + join.joinType + " `" + join.table
							+ "` as " + join.alias + "  on " + join.onKey + "="
							+ join.onValue + " ");
				}

				for (Map.Entry<String, Map.Entry<String, String>> en : where
						.entrySet()) {
					builder.append(' ').append(en.getKey()).append(' ')
							.append(en.getValue().getKey());
					if (isPrepared) {
						builder.append("? ");
					} else {
						builder.append("'").append(en.getValue().getValue())
								.append("' ");
					}
				}
				if (groupBy != null) {
					builder.append(" group by ").append(this.groupBy)
							.append("");
				}
			}
			this.currentSql = builder.toString();
			this.currentSql += super.getFollowings();
			this.isBuilt = true;
			return currentSql;
		}

		public Select order(String order) {

			this.orderColumn = order;
			return this;
		}

		public List<Map<String, String>> getTable() {
			if (isPrepared) {
				return Db.preparedSelect(this.get(), this.params());
			} else {
				return Db.selectTable(this.get());
			}
		}

		public <T> List<T> getType(Class<T> type) {
			if (isPrepared) {
				return Db.preparedSelect(this.get(), this.params(), type);
			} else {
				return Db.select(this.get(), type);
			}
		}

	}

	public static class Join {
		String table, joinType, onKey, onValue, alias;
		private Select select;

		public Join(String table, Select select) {
			this.select = select;
			this.joinType = "inner join";
			this.table = table;
		}

		public Join left() {
			this.joinType = "left join";
			return this;
		}

		public Join right() {
			this.joinType = "right join";
			return this;
		}

		public Join as(String a) {
			this.alias = a;
			return this;
		}

		public Join asFirst() {
			this.alias = this.table.substring(0, 1);
			return this;
		}

		public Select on(String key, String value) {
			this.onKey = key;
			this.onValue = value;
			return select;
		}
	}

}
