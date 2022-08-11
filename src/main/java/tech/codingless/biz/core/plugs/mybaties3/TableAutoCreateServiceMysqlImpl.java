package tech.codingless.biz.core.plugs.mybaties3;

import java.lang.reflect.Field;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import javax.sql.DataSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import tech.codingless.biz.core.plugs.mybaties3.annotation.MyColumn;
import tech.codingless.biz.core.plugs.mybaties3.annotation.MyComment;
import tech.codingless.biz.core.plugs.mybaties3.annotation.MyTable;
import tech.codingless.biz.core.plugs.mybaties3.data.BaseDO;
import tech.codingless.biz.core.plugs.mybaties3.enums.DbNameConstant;
import tech.codingless.biz.core.plugs.mybaties3.util.MybatiesStringUtil;

@Service
public class TableAutoCreateServiceMysqlImpl implements TableAutoCreateService {
	private final static Logger LOG = LoggerFactory.getLogger(TableAutoCreateServiceMysqlImpl.class);
	private static String TABLE_NAME = "uni_";
	private static String SPLIT_WORDS = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
	private String url = "";
	private String user = "";
	private String pwd = "";
	private String dataSourceUrl = null;
	private String dbName = "";
	private static ConcurrentHashMap<String, Boolean> EXIST_TABLE_NAME = new ConcurrentHashMap<>();
	private static ConcurrentHashMap<String, Boolean> EXIST_COLUMN_NAME = new ConcurrentHashMap<>();

	@Autowired(required = false)
	DataSourceFactory dataSourceFactory;

	private List<BaseDO> doList;

	@Override
	public void setPassword(String password) {
		this.pwd = password;

	}

	@Override
	public void setUrl(String url) {
		this.url = url;
		// 从JDBC url中提取数据库名称
		dbName = url.split("/")[3].split("[\\?]")[0];
		DbNameConstant.DB_NAME = dbName;
	}

	@Override
	public void setUsername(String username) {
		this.user = username;

	}

	@Override
	public boolean create() {
		if (CollectionUtils.isEmpty(doList)) {
			LOG.info("没有发现实体类，不用创建任何表格!");
			return false;
		}
		// 这里是为了解析出dbName
		getConn();
		// 加载已存在的表名
		query(String.format("SELECT table_name FROM information_schema.TABLES WHERE table_schema = '%s'", dbName)).forEach(row -> {
			EXIST_TABLE_NAME.put(row.get("table_name").toLowerCase(), true);
		});

		// 加载已存在的字段
		query(String.format("select table_name,column_name  from information_schema.columns where table_schema= '%s'", dbName)).forEach(row -> {
			EXIST_COLUMN_NAME.put(row.get("table_name").toLowerCase() + "/" + row.get("column_name").toLowerCase(), true);
		});

		doList = doList.stream().filter(item -> item.getClass().getAnnotation(MyTable.class) != null).collect(Collectors.toList());
		doList.forEach(item -> {
			try {
				LOG.info("解析实体类:" + item.getClass().getName());
				createTable(item);
				parserObject(item);
			} catch (Exception e) {
				LOG.error("解析实体类", e);
			}
		});

		return true;
	}

	private void createTable(Object obj) {
		String tableName = getTableName(obj);
		if (EXIST_TABLE_NAME.containsKey(tableName)) {
			return;
		}
		String ddl = String.format("CREATE TABLE %s (id VARCHAR(64) PRIMARY KEY)", tableName);
		executeDDL(ddl);

	}

	private void executeDDL(String ddl) {
		Connection conn = getConn();
		try {
			Statement stmt = conn.createStatement();
			LOG.info(ddl);
			stmt.executeUpdate(ddl);
		} catch (Exception e) {
			e.printStackTrace();
			LOG.error("CREATE_DDL_FAIL", e);
		} finally {

		}

	}

	Connection conn = null;

	private Connection getConn() {
		if (conn != null) {
			return conn;
		}
		try {
			Class.forName("com.mysql.cj.jdbc.Driver");
			if (dataSourceFactory != null) {
				DataSource dataSource = dataSourceFactory.make();
				conn = dataSource.getConnection();
				dataSourceUrl = conn.getMetaData().getURL();
				this.setUrl(dataSourceUrl);
				return conn;
			}

			conn = DriverManager.getConnection(url, user, pwd);
			return conn;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;

	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	private List<Map<String, String>> query(String sql) {
		List list = new ArrayList();
		Connection conn = getConn();
		try {
			Statement stmt = conn.createStatement();
			ResultSet rs = stmt.executeQuery(sql);
			ResultSetMetaData rsmd = rs.getMetaData();
			int columnCount = rsmd.getColumnCount();
			while (rs.next()) {
				Map map = new HashMap();
				list.add(map);
				for (int i = 0; i < columnCount; i++) {
					String columnName = rsmd.getColumnName(i + 1).toLowerCase();
					String value = rs.getString(i + 1);
					map.put(columnName, value);
				}
			}
		} catch (Exception e) {
			LOG.error("query error", e);
		} finally {
//			try {
//				conn.close();
//			} catch (SQLException e) {
//				e.printStackTrace();
//			}
		}
		return list;
	}

	private String getTableName(Object obj) { 
		return getTableName(obj.getClass());
	}
	
	public static String getTableName(Class<?> clazz) {
		MyTable myTable = clazz.getAnnotation(MyTable.class);
		String tableName = MybatiesStringUtil.isEmpty(myTable.prefix()) ? TABLE_NAME : myTable.prefix().trim() + "_" + change2dbFormat(clazz.getSimpleName());
		tableName = tableName.replace("_D_O", "").toLowerCase();
		return tableName;
	}

	private void parserObject(Object obj) {
		 

		// 如果父类，非Object,非BaseDO的情况下，拿父类的属性 
		Class<?> parentClazz = obj.getClass().getSuperclass();
		if (!parentClazz.equals(Object.class) && !parentClazz.equals(BaseDO.class)) {
			createColumn(obj, parentClazz.getDeclaredFields());
		}
		createColumn(obj, BaseDO.class.getDeclaredFields());
		createColumn(obj, obj.getClass().getDeclaredFields());

	}

	/**
	 * 通过属性创建字段
	 * 
	 * @param obj
	 * @param fields
	 */
	private void createColumn(Object obj, Field[] fields) {
		if (fields == null) {
			return;
		}
		String tableName = getTableName(obj); 
		StringBuilder ddls = new StringBuilder();
		boolean needCreateColumn=false;
		for (Field field : fields) {
			String columnName = change2dbFormat(field.getName()).toLowerCase();
			if (columnExist(tableName, columnName)) {
				continue;
			}
			 

			Class<?> typeClazz = field.getType();
			MyColumn myColumn = field.getAnnotation(MyColumn.class);
			MyComment myComment = field.getAnnotation(MyComment.class); 
			String typeDef = detectedColumnTypeByClassType(typeClazz, columnName); 
			if(myColumn!=null&&MybatiesStringUtil.isNotEmpty(myColumn.type())) {
				typeDef = myColumn.type();
			}
			
			
			String defaultValue = myColumn!=null?myColumn.defaultValue():"";
			
			String commentSql = "";
			if (myComment!=null&& MybatiesStringUtil.isNotEmpty(myComment.value())) { 
				commentSql = "  COMMENT '" + myComment.value().replaceAll("'", "") + "'";

			} 
			// 自增
			String autoIncrementSql = "";
			if (myColumn!=null && myColumn.autoIncrement()) {
				autoIncrementSql = "not null auto_increment ";
			}
			if (myColumn!=null) {
				if (myColumn.autoIncrement()) {
					autoIncrementSql = "not null auto_increment ";
				}
				if(myColumn.autoIncrement()||myColumn.createIndex()) {
					autoIncrementSql += ", add  key(" + columnName + ")"; 
				}
			} 
			
			String ddl = String.format("ALTER table %s ADD COLUMN %s %s %s %s %s", tableName, columnName, typeDef, (MybatiesStringUtil.isNotEmpty(defaultValue) ? ("default " + defaultValue) : ""), commentSql,
					autoIncrementSql);
			LOG.info(ddl);
			ddls.append(ddl).append(";");
			needCreateColumn=true;
		}
		
		if(needCreateColumn) { 
			executeDDL(ddls.toString());
		}

	}

	/**
	 * 通过属性类型，来确定字段类型及长度
	 * 
	 * @param typeClazz
	 * @return
	 */
	private String detectedColumnTypeByClassType(Class<?> typeClazz, String columnName) {
		String typeName = typeClazz.getName();
		String typeDef = null;
		switch (typeName) {
		case "int": {
			typeDef = "INTEGER";
			break;
		}
		case "java.lang.Integer": {
			typeDef = "INTEGER";
			break;
		}
		case "long": {
			typeDef = "numeric(32,0)";
			break;
		}
		case "java.lang.Long": {
			typeDef = "numeric(32,0)";
			break;
		}
		case "double": {
			typeDef = "double";
			break;
		}
		case "java.lang.Double": {
			typeDef = "double";
			break;
		}
		case "float": {
			typeDef = "float4";
			break;
		}
		case "java.lang.Float": {
			typeDef = "float4";
			break;
		} 
		case "boolean": {
			typeDef = "bool";
			break;
		}
		case "java.lang.Boolean": {
			typeDef = "bool";
			break;
		}
		case "java.lang.String": {
			typeDef = "id".equals(columnName) ? "VARCHAR(64)" : "VARCHAR(64)";
			break;
		}
		case "java.util.Date": {
			typeDef = "DATETIME";
			break;
		}
		case "java.math.BigDecimal": {
			typeDef = "numeric(32,2)";
			break;
		}
		default:
			typeDef = "VARCHAR(1024)";

		} 
		return typeDef;
	}

	/**
	 * 将属性转成数据库字段
	 * 
	 * @param prop
	 * @return
	 *
	 */
	public static String change2dbFormat(String prop) {
		StringBuffer sb = new StringBuffer(prop);
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
 

	private boolean columnExist(String tableName, String columnName) {
		// ID为主键，创建表的时候就生成了，所以不需要单独创建ID字段
		if ("id".equalsIgnoreCase(columnName)) {
			return true;
		}
		return EXIST_COLUMN_NAME.containsKey(tableName.toLowerCase() + "/" + columnName.toLowerCase());

	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	public boolean setDOList(Collection<BaseDO> collection) {
		if (CollectionUtils.isEmpty(collection)) {
			return false;
		}
		this.doList = new ArrayList();
		doList.addAll(collection);
		return true;
	}

	@Override
	public void closeConn() {
		if (conn != null) {
			try {
				conn.close();
				conn = null;
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}

	}

}
