package tech.codingless.core.plugs.mybaties3.helper;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import lombok.Getter;
import lombok.Setter;
import tech.codingless.core.plugs.mybaties3.annotation.MyColumn;
import tech.codingless.core.plugs.mybaties3.annotation.MyTable;
import tech.codingless.core.plugs.mybaties3.annotation.OrderTypeEnum;
import tech.codingless.core.plugs.mybaties3.data.BaseDO;
import tech.codingless.core.plugs.mybaties3.helper.MyTableColumnParser.ColumnProp;
import tech.codingless.core.plugs.mybaties3.util.MybatiesAssertUtil;
import tech.codingless.core.plugs.mybaties3.util.MybatiesStringUtil;

public class CommonSQLHelper {
	private static final String BLOCK = " ";
	private static final String SPLIT_AND = " and ";
	private static final String WHERE = "  where  ";

	private static String SPLIT_WORDS = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
	/**
	 * column-->propertity
	 */
	private static Map<String, Map<String, String>> CACHE = new HashMap<String, Map<String, String>>(); 
	private static ConcurrentHashMap<Class<?>,String> TABLE_NAME_CACHE=new ConcurrentHashMap<>(100);

	public static String getTableName(Object entity) {
		return getTableName(entity.getClass());
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	public static String getTableName(Class entity) {
		if(TABLE_NAME_CACHE.containsKey(entity)) {
			return TABLE_NAME_CACHE.get(entity);
		}
		
		
		String tableName = change2dbFormat(entity.getSimpleName());
		MyTable myTable;
		try {

			myTable = entity.getDeclaredConstructor().newInstance().getClass().getAnnotation(MyTable.class);
			tableName = MybatiesStringUtil.isEmpty(myTable.prefix()) ? "uni" : myTable.prefix().trim() + "_" + change2dbFormat(entity.getSimpleName());
			tableName = tableName.replace("_D_O", "").toUpperCase(); 
			TABLE_NAME_CACHE.put(entity, tableName);
			return tableName;
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	public static String getInsertSQL(Object entity) {
		Map<String, String> columns = getColumnAndProperties(entity.getClass());
		if (columns == null) {
			throw new RuntimeException(entity.getClass() + " 获取不到字段关系!");
		}
		StringBuffer columnSB = new StringBuffer();
		StringBuffer propertySB = new StringBuffer();
		for (String column : columns.keySet()) {
			columnSB.append(column).append(",");
			if ("gmtCreate".equals(columns.get(column)) || "gmtWrite".equals(columns.get(column))) {
				propertySB.append("UNIX_TIMESTAMP(),");
			} else {
				propertySB.append("#{").append(columns.get(column)).append("},");
			}
		}
		if (columnSB.length() > 0) {
			columnSB.deleteCharAt(columnSB.length() - 1);
			propertySB.deleteCharAt(propertySB.length() - 1);
		}
		return String.format("INSERT INTO %s(%s)values(%s)", getTableName(entity), columnSB.toString(), propertySB.toString());
	}

	public static String getInsertSQLBatch(Object entity) {
		Map<String, String> columns = getColumnAndProperties(entity.getClass());
		if (columns == null) {
			throw new RuntimeException(entity.getClass() + " 获取不到字段关系!");
		}
		StringBuffer columnSB = new StringBuffer();
		StringBuffer propertySB = new StringBuffer();
		for (String column : columns.keySet()) {
			columnSB.append(column).append(",");
			if ("gmtCreate".equals(columns.get(column)) || "gmtWrite".equals(columns.get(column))) {
				propertySB.append("UNIX_TIMESTAMP(),");
			} else {
				propertySB.append("#{").append(columns.get(column)).append("},");
			}
		}
		if (columnSB.length() > 0) {
			columnSB.deleteCharAt(columnSB.length() - 1);
			propertySB.deleteCharAt(propertySB.length() - 1);
		}

		String forEachStart = "<foreach collection=\"list\" index=\"index\" item=\"ele\"  separator=\",\">";
		String forEachEnd = "</foreach>";

		return String.format("INSERT INTO %s(%s)values %s(%s) %s", getTableName(entity), columnSB.toString(), forEachStart, propertySB.toString(), forEachEnd);
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	private static Map<String, String> getColumnAndProperties(Class clazz) {
		if (clazz.getAnnotation(MyTable.class) == null) {
			throw new RuntimeException(clazz.getName() + " 不是一个实体类!");
		}

		String tableName = getTableName(clazz);
		if (CACHE.containsKey(tableName)) {
			return CACHE.get(tableName);
		}
		Map<String, String> map = new HashMap();
		for (Method method : clazz.getMethods()) {
			String methodName = method.getName();
			if (methodName.equals("getClass")) {
				continue;
			}
			if (!methodName.startsWith("get") && !methodName.startsWith("is")) {
				continue;
			}
			String pName = new String();
			if (methodName.startsWith("get")) {
				pName = methodName.substring(3);
			}
			if (methodName.startsWith("is")) {
				pName = methodName.substring(2);
			}
			pName = pName.substring(0, 1).toLowerCase() + pName.substring(1);
			String columnName = null;

			try {
				Field filed = clazz.getDeclaredField(pName);
				MyColumn myColumn = filed.getAnnotation(MyColumn.class);
				if (myColumn != null) {
					columnName = MybatiesStringUtil.isNotEmpty(myColumn.name()) ? myColumn.name() : columnName;
				}
			} catch (Exception e) {

			}

			if (MybatiesStringUtil.isEmpty(columnName)) {
				columnName = change2dbFormat(pName);
			}
			map.put(columnName, pName);
		}
		CACHE.put(tableName, map);
		return map;
	}

	public static String change2dbFormat(String string) {
		StringBuffer sb = new StringBuffer(string);
		int len = sb.length();
		for (int i = 1; i < len; i++) {
			if (!SPLIT_WORDS.contains(sb.charAt(i) + "")) {
				continue;
			}
			sb.insert(i, "_");
			i++;
			len++;
		}
		return sb.toString();
	}

	public static String getDeleteSQL(Class<?> clazz) {
		PrimaryKey primaryKey = getPrimaryKey(clazz); 
		MybatiesAssertUtil.assertNotNull(primaryKey, clazz + " 缺少主键!"); 
		return String.format("delete from %s where %s = #{entityId}", getTableName(clazz), primaryKey.getColumn());
	}

	public static String getDeleteWithCompanyIdSQL(Class<?> clazz) {
		PrimaryKey primaryKey = getPrimaryKey(clazz);
		MybatiesAssertUtil.assertNotNull(primaryKey, clazz + " 缺少主键!");  
		return String.format("delete from %s where %s = #{entityId} and company_id=#{company_id}", getTableName(clazz), primaryKey.getColumn());
	}

	private static PrimaryKey ID = null;

	private static PrimaryKey getPrimaryKey(Class<?> clazz) {
		// 目前主键约定固定为KEY
		if (ID == null) {
			ID = new PrimaryKey();
			ID.setColumn("id");
			ID.setAttrName("id");
		}
		return ID;
	}

	public static String getUpdateSQL(Class<?> clazz) {
		return gentUpdateSQLWithCompanyId(clazz, false);
	}

	public static String getUpdateSQLWithCompanyId(Class<?> clazz) {
		return gentUpdateSQLWithCompanyId(clazz, true);
	}

	private static String gentUpdateSQLWithCompanyId(Class<?> clazz, boolean companyIdCondition) {
		Map<String, String> columns = getColumnAndProperties(clazz);
		PrimaryKey key = getPrimaryKey(clazz);
		MybatiesAssertUtil.assertNotNull(key, clazz + " 缺少主键!");  
		MybatiesAssertUtil.assertNotNull(columns, clazz + " 获取不到字段关系!");  
		
		StringBuffer setContent = new StringBuffer();
		for (String column : columns.keySet()) {
			if (column.equals(key.getColumn())) {
				continue;
			}
			String attrName = columns.get(column);
			if ("gmtCreate".equals(attrName) || "createUid".equals(attrName) || "ownerId".equals(attrName)) {
				continue;
			}
			if ("gmtWrite".equals(attrName)) {
				setContent.append(column).append("=").append("UNIX_TIMESTAMP(),");
			} else {
				setContent.append(column).append("=").append("#{").append(attrName).append("},");
			}
		}
		if (setContent.length() > 0) {
			setContent.deleteCharAt(setContent.length() - 1);
		}
		if (companyIdCondition) {
			return String.format("update %s set %s where %s=#{%s}  and company_id = #{companyId}", getTableName(clazz), setContent.toString(), key.getColumn(), key.getAttrName());
		}

		return String.format("update %s set %s where %s=#{%s} ", getTableName(clazz), setContent.toString(), key.getColumn(), key.getAttrName());
	}

	public static String getGetSQL(Class<?> clazz) {
		PrimaryKey key = getPrimaryKey(clazz);
		if (key == null) {
			throw new RuntimeException(clazz + " 缺少主键!");
		}
		return String.format("select * from %s where %s = #{%s} and not del and env=#{env}", getTableName(clazz), key.getColumn(), key.getAttrName());
	}

	public static String getGetSQLByCompanyId(Class<?> clazz) {
		PrimaryKey key = getPrimaryKey(clazz);
		MybatiesAssertUtil.assertNotNull(key, clazz + " 缺少主键!");   
		return String.format("select * from %s where %s = #{%s}  and  company_id =#{companyId} and not del", getTableName(clazz), key.getColumn(), key.getAttrName());
	}

	public static String getListSQL(Class<?> clazz) {
		PrimaryKey key = getPrimaryKey(clazz);
		MybatiesAssertUtil.assertNotNull(key, clazz + " 缺少主键!");   
		return String.format("select * from %s order by  gmt_create asc", getTableName(clazz));
	}

	public static String getListByCompanySQL(Class<?> clazz) {
		PrimaryKey key = getPrimaryKey(clazz);
		MybatiesAssertUtil.assertNotNull(key, clazz + " 缺少主键!");   
		return String.format("select * from %s  where  company_id = #{companyId} and not del order by  gmt_create asc", getTableName(clazz));
	}

	@Setter
	@Getter
	public static class ExecuteSql {
		private String sql;
		private List<Object> param;
	}
 

	public static ExecuteSql genSelectSqlSkipNullProperties(String companyId, BaseDO entity, String orderColumn, OrderTypeEnum orderType, Integer limit, Integer offset) throws Exception {
		Class<?> clazz = entity.getClass();
		PrimaryKey key = getPrimaryKey(clazz);
		MybatiesAssertUtil.assertNotNull(key, clazz + " 缺少主键!");   

		ExecuteSql sql = new ExecuteSql();
		sql.setParam(new ArrayList<>());
		StringBuilder selectSql = new StringBuilder();
		selectSql.append("select *  from  ").append(getTableName(clazz));

		// 添加companyId条件
		boolean needWhereKeyword = true;
		if (MybatiesStringUtil.isNotEmpty(companyId)) {
			selectSql.append(WHERE);
			needWhereKeyword = false;
			selectSql.append(BLOCK).append("company_id=").append("#{companyId} and not del");
			sql.getParam().add(companyId);
		}

		// 添加查询模版的条件
		List<ColumnProp> columns = MyTableColumnParser.parseSkipNull(clazz, entity);
		for (ColumnProp column : columns) {
			if (needWhereKeyword) {
				selectSql.append(WHERE);
				needWhereKeyword = false;
			} else {
				selectSql.append(SPLIT_AND);
			}
			selectSql.append(column.getColumn()).append("=#{obj." + column.getProp() + "}");
			sql.getParam().add(column.getVal());
		}

		// 添加排序与分页参数
		if (MybatiesStringUtil.isNotEmpty(orderColumn) && orderType != null) {
			selectSql.append(" order by ").append(orderColumn).append(" ").append(orderType.getCode());
			//selectSql.append(" order by #{sortColumn}  #{sortType}");// .append("#{orderColumn}");
			//sql.getParam().add(orderColumn);
			//sql.getParam().add(orderType.getCode());
		}

		selectSql.append(BLOCK).append(" limit  #{limit} offset #{offset}");
		sql.getParam().add(limit);
		sql.getParam().add(offset);
		sql.setSql(selectSql.toString());
		return sql;
	}

	public static ExecuteSql genCountSqlSkipNullProperties(String companyId, BaseDO entity) throws Exception {
		Class<?> clazz = entity.getClass();
		PrimaryKey key = getPrimaryKey(clazz);
		MybatiesAssertUtil.assertNotNull(key, clazz + " 缺少主键!");   

		ExecuteSql sql = new ExecuteSql();
		sql.setParam(new ArrayList<>());
		StringBuilder selectSql = new StringBuilder();
		boolean needWhereKeyword = true;
		selectSql.append("select count(1) as total_Count  from  ").append(getTableName(clazz));

		// 添加companyId条件
		if (MybatiesStringUtil.isNotEmpty(companyId)) {
			selectSql.append(WHERE);
			needWhereKeyword = false;
			selectSql.append(BLOCK).append("company_id=").append("#{companyId} and not del");
			sql.getParam().add(companyId);
		}

		// 添加查询模版的条件
		List<ColumnProp> columns = MyTableColumnParser.parseSkipNull(clazz, entity);
		for (ColumnProp column : columns) {
			if (needWhereKeyword) {
				selectSql.append(WHERE);
				needWhereKeyword = false;
			} else {
				selectSql.append(SPLIT_AND);
			}
			selectSql.append(column.getColumn()).append("=#{obj." + column.getProp() + "}");
			sql.getParam().add(column.getVal());
		}

		sql.setSql(selectSql.toString());
		return sql;
	}

	public static String getDeleteLogicalWithCompanyIdSQL(Class<?> clazz) {
		PrimaryKey primaryKey = getPrimaryKey(clazz);
		MybatiesAssertUtil.assertNotNull(primaryKey, clazz + " 缺少主键!");   
		return String.format("update %s set del=true where %s = #{entityId} and company_id=#{company_id}", getTableName(clazz), primaryKey.getColumn());
	}

}

class PrimaryKey {
	private String column;
	private String attrName;

	public String getColumn() {
		return column;
	}

	public void setColumn(String column) {
		this.column = column;
	}

	public String getAttrName() {
		return attrName;
	}

	public void setAttrName(String attrName) {
		this.attrName = attrName;
	}

}