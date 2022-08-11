package tech.codingless.biz.core.plugs.mybaties3;

import java.io.ByteArrayInputStream;
import java.io.FileInputStream;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.dbcp2.BasicDataSource;
import org.apache.commons.io.IOUtils;
import org.apache.ibatis.builder.MapperBuilderAssistant;
import org.apache.ibatis.builder.SqlSourceBuilder;
import org.apache.ibatis.builder.xml.XMLMapperBuilder;
import org.apache.ibatis.builder.xml.XMLMapperEntityResolver;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.mapping.ResultMap;
import org.apache.ibatis.mapping.ResultMapping;
import org.apache.ibatis.mapping.SqlCommandType;
import org.apache.ibatis.mapping.SqlSource;
import org.apache.ibatis.mapping.StatementType;
import org.apache.ibatis.parsing.XNode;
import org.apache.ibatis.parsing.XPathParser;
import org.apache.ibatis.scripting.xmltags.XMLLanguageDriver;
import org.mybatis.spring.MyBatisSystemException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import com.alibaba.fastjson.JSON;

import tech.codingless.biz.core.plugs.mybaties3.condition.ColumnSelector;
import tech.codingless.biz.core.plugs.mybaties3.condition.QueryCondition;
import tech.codingless.biz.core.plugs.mybaties3.condition.QueryConditionWrapper;
import tech.codingless.biz.core.plugs.mybaties3.condition.QueryConditionWrapperParser;
import tech.codingless.biz.core.plugs.mybaties3.conf.DataBaseConf;
import tech.codingless.biz.core.plugs.mybaties3.helper.AutoFindByIdBatchHelper;
import tech.codingless.biz.core.plugs.mybaties3.helper.AutoFindByIdHelper;
import tech.codingless.biz.core.plugs.mybaties3.helper.AutoGetHelper;
import tech.codingless.biz.core.plugs.mybaties3.helper.AutoRollPageSelectSqlHelper;
import tech.codingless.biz.core.plugs.mybaties3.helper.AutoSelectByConditionSqlHelper;
import tech.codingless.biz.core.plugs.mybaties3.helper.MyTableColumnParser;
import tech.codingless.biz.core.plugs.mybaties3.helper.MyTypeHanderRegistHelper;
import tech.codingless.biz.core.plugs.mybaties3.helper.PrepareParameterHelper;
import tech.codingless.biz.core.reflect.ReflectionUtil;
import tech.codingless.biz.core.reflect.SerializableFunction;
import tech.codingless.biz.core.util.MD5Util;
import tech.codingless.biz.core.util.StringUtil;

@Component
public class GenericQueryDAOImpl<T extends BaseDO> implements GenericQueryDao<T> {
	private static final String NAMESPACE="AUTOSQL";
	private static final Logger LOG = LoggerFactory.getLogger(GenericQueryDAOImpl.class);
	private static Map<String, ResultMap> RESULT_MAPS = new HashMap<>();
	protected MyBatiesService myBatiesService;
	protected BasicDataSource basicDataSource;
 
	@Autowired
	protected void setMyBatiesService(MyBatiesService myBatiesService) {
		LOG.info("注入数据访问服务:" + myBatiesService);
		this.myBatiesService = myBatiesService;
		DataBaseConf.Conf conf = DataBaseConf.get();
		if (StringUtil.isNotEmpty(conf.getUrl(), conf.getUsername(), conf.getPassword())) {
			basicDataSource = new BasicDataSource();
			basicDataSource.setDriverClassName("com.mysql.cj.jdbc.Driver");
			basicDataSource.setUrl(conf.getUrl());
			basicDataSource.setUsername(conf.getUsername());
			basicDataSource.setPassword(conf.getPassword());
			basicDataSource.setMaxIdle(30);
			basicDataSource.setMinIdle(3);
			basicDataSource.setMaxTotal(30);
			basicDataSource.setMaxWaitMillis(10);
			basicDataSource.setInitialSize(3);
			basicDataSource.setRemoveAbandonedOnBorrow(true);
			basicDataSource.setRemoveAbandonedTimeout(180);
			LOG.info("生成BasicDataSource：" + basicDataSource);
		}
	}

	@Override
	public T selectOne(String sqlId, Object parameter) {
		return myBatiesService.selectOne(sqlId, parameter);
	}
	@Override
	public Object selectOneRow(String sqlId, Object param) { 
		return myBatiesService.selectOne(sqlId, param);
	}

	@Override
	public List<T> selectList(String sqlId, Object param) {
		return myBatiesService.selectList(sqlId, param);
	}
 

	@Override
	public T getEntity(Class<T> clazz, String id) {
		String sqlKey = "AUTOSQL.GET_" + CommonSQLHelper.getTableName(clazz);  

		Map<String, Object> param = new HashMap<String, Object>(6);
		param.put("id", id); 
		param.put("env", DataEnvUtil.getEvn());
		try {
			return myBatiesService.selectOne(sqlKey, param);
		} catch (MyBatisSystemException e) {

			if (ConcurrentSqlCreatorLocker.notExist(sqlKey)) {
				synchronized (ConcurrentSqlCreatorLocker.getLocker(sqlKey)) {
					if (ConcurrentSqlCreatorLocker.notExist(sqlKey)) { 
						AutoGetHelper.genAutoSqlForGet(clazz,false,myBatiesService.getConfiguration()); 
						ConcurrentSqlCreatorLocker.put(sqlKey);
					}
				}
			}

			return myBatiesService.selectOne(sqlKey, param);
		}
	}

	@Override
	public T getEntity(Class<T> clazz, String id, String companyId) {
		String namespace="AUTOSQL";
		String sqlKey = "GET_BYCOMPANYID_" + CommonSQLHelper.getTableName(clazz);
		String sqlFullKey = namespace+"."+sqlKey;
		Map<String, Object> param = new HashMap<String, Object>(6);
		param.put("id", id);
		param.put("companyId", companyId);
		param.put("env", DataEnvUtil.getEvn());
		try {
			return myBatiesService.selectOne(sqlFullKey, param);
		} catch (MyBatisSystemException e) {

			if (ConcurrentSqlCreatorLocker.notExist(sqlFullKey)) {
				synchronized (ConcurrentSqlCreatorLocker.getLocker(sqlFullKey)) {
					if (ConcurrentSqlCreatorLocker.notExist(sqlFullKey)) { 
						//AutoGetHelper.genAutoSqlForGet(clazz,false,myBatiesService.getConfiguration());  
						AutoFindByIdHelper.genGetSql(myBatiesService.getConfiguration(), namespace, sqlKey, clazz);
						ConcurrentSqlCreatorLocker.put(sqlFullKey);
					}
				}
			}
			return myBatiesService.selectOne(sqlFullKey, param);
		}
	}

	@Override
	public List<T> findEntityList(Class<T> clazz, String companyId, Collection<String> idList) {
		return findEntityList(clazz, companyId, idList, null);
	}

	@Override
	public List<T> findEntityList(Class<T> clazz, String companyId, Collection<String> idList, Collection<String> columns) {
		String sqlKey = "AUTOSQL.findEntityList_" + CommonSQLHelper.getTableName(clazz);
		try {
			Map<String, Object> param = new HashMap<>();
			param.put("companyId", companyId);
			param.put("idList", idList);
			param.put("columns", columns);
			return myBatiesService.selectList(sqlKey, param);
		} catch (MyBatisSystemException e) {

			if (ConcurrentSqlCreatorLocker.notExist(sqlKey)) {
				synchronized (ConcurrentSqlCreatorLocker.getLocker(sqlKey)) {
					if (ConcurrentSqlCreatorLocker.notExist(sqlKey)) {
						AutoFindByIdBatchHelper.genBatchGetSql(myBatiesService.getConfiguration(), "AUTOSQL", sqlKey, clazz);
						ConcurrentSqlCreatorLocker.put(sqlKey);
					}
				}
			}

			Map<String, Object> param = new HashMap<>();
			param.put("companyId", companyId);
			param.put("idList", idList);
			param.put("columns", columns);
			return myBatiesService.selectList(sqlKey, param);
		}
	}

	@Override
	public List<T> list(Class<T> clazz) {
		String sqlKey = "AUTOSQL.LIST_" + CommonSQLHelper.getTableName(clazz);
		try {
			return myBatiesService.selectList(sqlKey);
		} catch (MyBatisSystemException e) {

			if (ConcurrentSqlCreatorLocker.notExist(sqlKey)) {
				synchronized (ConcurrentSqlCreatorLocker.getLocker(sqlKey)) {
					if (ConcurrentSqlCreatorLocker.notExist(sqlKey)) {
						genAutoSqlForList(clazz);
						ConcurrentSqlCreatorLocker.put(sqlKey);
					}
				}
			}

			return myBatiesService.selectList(sqlKey);
		}
	}

	@Override
	public List<T> select(Class<? extends BaseDO> clazz, Collection<String> columns, Collection<QueryCondition> conditions, int offset, int limit) {
		String tableName = CommonSQLHelper.getTableName(clazz);
		MyTypeHanderRegistHelper.regist(myBatiesService.getConfiguration(), clazz);
		String sqlKey = "AUTOSQL.selectByConditions_" + tableName;

		Map<String, Object> param = new HashMap<>();
		param.put("columns", columns);
		param.put("conditions", conditions);
		param.put("offset", offset);
		param.put("limit", limit);
		try {
			return myBatiesService.selectList(sqlKey, param);
		} catch (MyBatisSystemException e) {

			if (ConcurrentSqlCreatorLocker.notExist(sqlKey)) {
				synchronized (ConcurrentSqlCreatorLocker.getLocker(sqlKey)) {
					if (ConcurrentSqlCreatorLocker.notExist(sqlKey)) {
						AutoSelectByConditionSqlHelper.gen(myBatiesService.getConfiguration(), "AUTOSQL", sqlKey, clazz);
						ConcurrentSqlCreatorLocker.put(sqlKey);
					}
				}
			}

			return myBatiesService.selectList(sqlKey, param);
		}
	}

	@Override
	public int count(Class<? extends BaseDO> clazz, Collection<String> columns, Collection<QueryCondition> conditions) {
		String tableName = CommonSQLHelper.getTableName(clazz);
		MyTypeHanderRegistHelper.regist(myBatiesService.getConfiguration(), clazz);
		String sqlKey = "AUTOSQL.countByConditions_" + tableName;

		Map<String, Object> param = new HashMap<>();
		param.put("columns", columns);
		param.put("conditions", conditions);
		try {
			return myBatiesService.selectOne(sqlKey, param);
		} catch (MyBatisSystemException e) {

			if (ConcurrentSqlCreatorLocker.notExist(sqlKey)) {
				synchronized (ConcurrentSqlCreatorLocker.getLocker(sqlKey)) {
					if (ConcurrentSqlCreatorLocker.notExist(sqlKey)) {
						AutoSelectByConditionSqlHelper.genCount(myBatiesService.getConfiguration(), "AUTOSQL", sqlKey, clazz);
						ConcurrentSqlCreatorLocker.put(sqlKey);
					}
				}
			}

			return myBatiesService.selectOne(sqlKey, param);
		}
	}

	private void genAutoSqlForList(Class<T> clazz) {

		MyTypeHanderRegistHelper.regist(myBatiesService.getConfiguration(), clazz);

		String sqlKey = "AUTOSQL.LIST_" + CommonSQLHelper.getTableName(clazz);
		SqlSourceBuilder sqlSourceBuilder = new SqlSourceBuilder(myBatiesService.getConfiguration());
		String listSQL = CommonSQLHelper.getListSQL(clazz);
		SqlSource sqlSource = sqlSourceBuilder.parse(listSQL, clazz, null);
		MappedStatement.Builder builder = new MappedStatement.Builder(myBatiesService.getConfiguration(), sqlKey, sqlSource, SqlCommandType.SELECT);
		List<ResultMapping> mappingList = new ArrayList<ResultMapping>();
		List<ResultMap> resultMapList = new ArrayList<ResultMap>();
		// 设置返回值绑定
		for (Method method : clazz.getMethods()) {
			String methodName = method.getName();
			if (methodName.equals("getClass")) {
				continue;
			}
			if (!methodName.startsWith("get") && !methodName.startsWith("is")) {
				continue;
			}
			String attrName = new String();
			if (methodName.startsWith("get")) {
				attrName = methodName.substring(3);
			}
			if (methodName.startsWith("is")) {
				attrName = methodName.substring(2);
			}
			attrName = attrName.substring(0, 1).toLowerCase() + attrName.substring(1);
			String columnName = null;
			try {
				Field filed = clazz.getDeclaredField(attrName);
				MyColumn myColumn = filed.getAnnotation(MyColumn.class);
				if (myColumn != null && StringUtil.isNotEmpty(myColumn.name())) {
					columnName = myColumn.name();
				}
			} catch (Exception e1) {

			}
			if (StringUtil.isEmpty(columnName)) {
				columnName = CommonSQLHelper.change2dbFormat(attrName);
			}
			ResultMapping.Builder mappingBuilder = new ResultMapping.Builder(myBatiesService.getConfiguration(), attrName);
			mappingBuilder.javaType(method.getReturnType());
			mappingBuilder.column(columnName);
			mappingList.add(mappingBuilder.build());
		}

		ResultMap.Builder mapBuilder = new ResultMap.Builder(myBatiesService.getConfiguration(), "AUTOSQL.LIST_MAP_" + clazz.getSimpleName(), clazz, mappingList);

		resultMapList.add(mapBuilder.build());
		builder.resultMaps(resultMapList);
		myBatiesService.getConfiguration().addMappedStatement(builder.build());

	}

	@Override
	public List<T> list(Class<T> clazz, String companyId) {
		String sqlKey = "AUTOSQL.LIST_BY_COMPANY_" + CommonSQLHelper.getTableName(clazz);
		try {
			return myBatiesService.selectList(sqlKey, companyId);
		} catch (MyBatisSystemException e) {

			if (ConcurrentSqlCreatorLocker.notExist(sqlKey)) {
				synchronized (ConcurrentSqlCreatorLocker.getLocker(sqlKey)) {
					if (ConcurrentSqlCreatorLocker.notExist(sqlKey)) {
						genAutoSqlForListByCompanyId(clazz);
						ConcurrentSqlCreatorLocker.put(sqlKey);
					}
				}
			}

			return myBatiesService.selectList(sqlKey, companyId);
		}
	}

	private void genAutoSqlForListByCompanyId(Class<T> clazz) {
		String sqlKey = "AUTOSQL.LIST_BY_COMPANY_" + CommonSQLHelper.getTableName(clazz);
		SqlSourceBuilder sqlSourceBuilder = new SqlSourceBuilder(myBatiesService.getConfiguration());
		String listSQL = CommonSQLHelper.getListByCompanySQL(clazz);
		SqlSource sqlSource = sqlSourceBuilder.parse(listSQL, clazz, null);
		MappedStatement.Builder builder = new MappedStatement.Builder(myBatiesService.getConfiguration(), sqlKey, sqlSource, SqlCommandType.SELECT);
		List<ResultMapping> mappingList = new ArrayList<ResultMapping>();
		List<ResultMap> resultMapList = new ArrayList<ResultMap>();
		// 设置返回值绑定
		for (Method method : clazz.getMethods()) {
			String methodName = method.getName();
			if (methodName.equals("getClass")) {
				continue;
			}
			if (!methodName.startsWith("get") && !methodName.startsWith("is")) {
				continue;
			}
			String attrName = new String();
			if (methodName.startsWith("get")) {
				attrName = methodName.substring(3);
			}
			if (methodName.startsWith("is")) {
				attrName = methodName.substring(2);
			}
			attrName = attrName.substring(0, 1).toLowerCase() + attrName.substring(1);
			String columnName = null;
			try {
				Field filed = clazz.getDeclaredField(attrName);
				MyColumn myColumn = filed.getAnnotation(MyColumn.class);
				if (myColumn != null && StringUtil.isNotEmpty(myColumn.name())) {
					columnName = myColumn.name();
				}
			} catch (Exception e1) {

			}
			if (StringUtil.isEmpty(columnName)) {
				columnName = CommonSQLHelper.change2dbFormat(attrName);
			}
			ResultMapping.Builder mappingBuilder = new ResultMapping.Builder(myBatiesService.getConfiguration(), attrName);
			mappingBuilder.javaType(method.getReturnType());
			mappingBuilder.column(columnName);
			mappingList.add(mappingBuilder.build());
		}

		ResultMap.Builder mapBuilder = new ResultMap.Builder(myBatiesService.getConfiguration(), "AUTOSQL.LIST_BY_COMPANY_MAP" + clazz.getSimpleName(), clazz, mappingList);

		resultMapList.add(mapBuilder.build());
		builder.resultMaps(resultMapList);
		myBatiesService.getConfiguration().addMappedStatement(builder.build());

	}

	@SuppressWarnings("rawtypes")
	private ResultMap createResultMap(Class clazz, String key) {

		MyTypeHanderRegistHelper.regist(myBatiesService.getConfiguration(), clazz);

		if (RESULT_MAPS.containsKey(key)) {
			return RESULT_MAPS.get(key);
		}
		// 设置返回值绑定
		List<ResultMapping> mappingList = new ArrayList<ResultMapping>();
		for (Method method : clazz.getMethods()) {
			String methodName = method.getName();
			if (MyTableColumnParser.needSkipMethodName(methodName)) {
				continue;
			}
			String attrName = MyTableColumnParser.methodName2attrName(methodName);
			String columnName = null;
			try {
				Field filed = clazz.getDeclaredField(attrName);
				MyColumn myColumn = filed.getAnnotation(MyColumn.class);
				if (myColumn != null && StringUtil.isNotEmpty(myColumn.name())) {
					columnName = myColumn.name();
				}
			} catch (Exception e1) {
			}
			if (StringUtil.isEmpty(columnName)) {
				columnName = CommonSQLHelper.change2dbFormat(attrName);
			}
			ResultMapping.Builder mappingBuilder = new ResultMapping.Builder(myBatiesService.getConfiguration(), attrName);
			mappingBuilder.javaType(method.getReturnType());
			mappingBuilder.column(columnName);
			mappingList.add(mappingBuilder.build());
		}
		ResultMap.Builder mapBuilder = new ResultMap.Builder(myBatiesService.getConfiguration(), key, clazz, mappingList);
		return mapBuilder.build();
	}

	@Override
	public PageRollResult<T> rollPage(Class<T> clazz, String companyId, T param, String orderColumn, OrderTypeEnum orderType, Integer size, Integer page) {
		size = size == null ? 20 : size;
		page = page == null ? 1 : page;
		size = size > 500 ? 500 : size < 1 ? 1 : size;
		Integer limit = size;
		Integer offset = (page - 1) * size;
		CommonSQLHelper.ExecuteSql selectSql = null;
		CommonSQLHelper.ExecuteSql countSql = null;
		BaseDO base = param;
		String selectSqlKey = null;
		String countSqlKey = null;
		PageRollResult<T> result = new PageRollResult<>();
		Map<String, Object> queryParam = new HashMap<>();
		
		base.setEnv(DataEnvUtil.getEvn());
		queryParam.put("companyId", companyId);
		queryParam.put("obj", param);
		queryParam.put("limit", limit);
		queryParam.put("offset", offset);
		queryParam.put("sortColumn", orderColumn);
		queryParam.put("sortType", orderType != null ? orderType.getCode() : null);
		try {
			selectSql = CommonSQLHelper.genSelectSqlSkipNullProperties(companyId, base, orderColumn, orderType, limit, offset);
			selectSqlKey = "AUTOSQL.rollpage_list_" + base.getClass().getName() + "_" + MD5Util.md5Hex(selectSql.getSql());
			countSql = CommonSQLHelper.genCountSqlSkipNullProperties(companyId, base);
			countSqlKey = "AUTOSQL.rollpage_count_" + base.getClass().getName() + "_" + MD5Util.md5Hex(selectSql.getSql());

			result.setList(myBatiesService.selectList(selectSqlKey, queryParam));
			RowCount rowCount = myBatiesService.selectOne(countSqlKey, queryParam);
			result.setTotalCount(rowCount != null ? rowCount.getTotalCount() : 0);
		} catch (Exception e) {

			SqlSourceBuilder sqlSourceBuilder = new SqlSourceBuilder(myBatiesService.getConfiguration());

			if (ConcurrentSqlCreatorLocker.notExist(selectSqlKey)) {
				synchronized (ConcurrentSqlCreatorLocker.getLocker(selectSqlKey)) {
					if (ConcurrentSqlCreatorLocker.notExist(selectSqlKey)) {

						SqlSource selectSource = sqlSourceBuilder.parse(selectSql.getSql(), queryParam.getClass(), null);
						MappedStatement.Builder selectBuilder = new MappedStatement.Builder(myBatiesService.getConfiguration(), selectSqlKey, selectSource, SqlCommandType.SELECT);
						myBatiesService.getConfiguration().addMappedStatement(selectBuilder.build());
						selectBuilder.resultMaps(Arrays.asList(createResultMap(clazz, "AUTOSQL.GET_MAP_LIST_" + clazz.getSimpleName())));

						ConcurrentSqlCreatorLocker.put(selectSqlKey);
					}
				}
			}

			if (ConcurrentSqlCreatorLocker.notExist(countSqlKey)) {
				synchronized (ConcurrentSqlCreatorLocker.getLocker(countSqlKey)) {
					if (ConcurrentSqlCreatorLocker.notExist(countSqlKey)) {

						SqlSource countSource = sqlSourceBuilder.parse(countSql.getSql(), queryParam.getClass(), null);
						MappedStatement.Builder countBuilder = new MappedStatement.Builder(myBatiesService.getConfiguration(), countSqlKey, countSource, SqlCommandType.SELECT);
						myBatiesService.getConfiguration().addMappedStatement(countBuilder.build());
						countBuilder.resultMaps(Arrays.asList(createResultMap(RowCount.class, "AUTOSQL.rowCount")));

						ConcurrentSqlCreatorLocker.put(countSqlKey);
					}
				}
			}

			result.setList(myBatiesService.selectList(selectSqlKey, queryParam));
			RowCount rowCount = myBatiesService.selectOne(countSqlKey, queryParam);
			result.setTotalCount(rowCount != null ? rowCount.getTotalCount() : 0);
		}
		result.setPageSize(size);
		result.setCurrentPage(page);

		result.setTotalPage(result.getTotalCount() == 0 ? 0 : (int) Math.ceil(result.getTotalCount() * 1.0 / size));
		return result;
	}

	@Override
	public PageRollResult<?> rollPage(String namespance, String id, Map<String, Object> param, Integer size, Integer page) {
		size = size == null ? 20 : size;
		page = page == null ? 1 : page;
		size = size > 500 ? 500 : size < 1 ? 1 : size;
		Integer limit = size;
		Integer offset = (page - 1) * size;

		PageRollResult<?> result = new PageRollResult<>();
		param.put("_limit_", limit);
		param.put("_offset_", offset);
		String sqlId = namespance.concat(id);
		String selectKey = id + "__selectRollPage__";
		String countKey = id + "__count__";

		try {
			result.setList(myBatiesService.selectList(namespance.concat(selectKey), param));
			Integer rows = myBatiesService.selectOne(namespance.concat(countKey), param);
			result.setTotalCount(rows != null ? rows : 0);
		} catch (Exception e) {

			MappedStatement stmt = myBatiesService.getConfiguration().getMappedStatement(sqlId);
			try {
				String xmlpath = stmt.getResource();
				xmlpath = xmlpath.substring(xmlpath.indexOf("[") + 1, xmlpath.indexOf("]"));
				if (xmlpath.startsWith("jar:")) {
					xmlpath = xmlpath.substring(4);
				}
				String xml = null;
				if (xmlpath.contains("classes!")) {
					String classpath = xmlpath.split("classes!")[1];
					xml = IOUtils.toString(this.getClass().getResourceAsStream(classpath));
				} else {
					xml = IOUtils.toString(new FileInputStream(xmlpath));
				}
				AutoRollPageSelectSqlHelper.genSelectMapper(namespance, id, selectKey, countKey, myBatiesService.getConfiguration(), xml);

			} catch (Exception e2) {
				LOG.error("", e2);
			}

			result.setList(myBatiesService.selectList(namespance.concat(selectKey), param));
			Integer rows = myBatiesService.selectOne(namespance.concat(countKey), param);
			result.setTotalCount(rows != null ? rows : 0);
		}

		result.setPageSize(size);
		result.setCurrentPage(page);
		result.setTotalPage(result.getTotalCount() == 0 ? 0 : (int) Math.ceil(result.getTotalCount() * 1.0 / size));
		return result;
	}

	@Override
	public <E> List<E> noShardingList(String statement, Object parameter) {
		return myBatiesService.selectListNoSharding(statement, parameter);
	}

	@Override
	public Map<String, Object> selectOneNative(String prepareSql, List<Object> params) {

		try (Connection conn = basicDataSource.getConnection()) {
			PreparedStatement pstmt = conn.prepareStatement(prepareSql);
			PrepareParameterHelper.bindParam(pstmt, params);
			ResultSet rs = pstmt.executeQuery();
			ResultSetMetaData rsmd = rs.getMetaData();
			if (rs.next()) {
				Map<String, Object> row = new HashMap<>();
				for (int i = 0; i < rsmd.getColumnCount(); i++) {
					String columnName = rsmd.getColumnName(i + 1);
					Object val = rs.getObject(i + 1);
					row.put(columnName.toLowerCase(), val);
				}
				return row;
			}

			LOG.info("SQL:{},PARAM:{}", prepareSql, !CollectionUtils.isEmpty(params) ? JSON.toJSONString(params) : "NONE");
		} catch (SQLException e) {
			LOG.error("insertNative", e);
		}
		return null;

	}

	private List<SqlLoader> sqlLoaders;

	@Autowired(required = false)
	public void registSqlLoader(List<SqlLoader> sqlLoaders) {
		LOG.info("加载了sqlLoaders");
		this.sqlLoaders = sqlLoaders;
		sqlLoaders.forEach(sqlLoader -> {
			LOG.info("sqlLoader:{}", sqlLoader);
		});
	}

	@Override
	public List<Map<String, ?>> select(String selectId, Map<String, Object> param, int offset, int limit) {
		return select2(selectId, param, offset, limit);
	}

	private List<Map<String, ?>> select2(String selectId, Map<String, Object> param, int offset, int limit) {
		String namespace = selectId.split("[.]")[0];
		String id = selectId.split("[.]")[1]; 
		String sysSelectId = "external."+namespace+"." + selectId.trim().replace(".", "-");
		String realSelectId = selectId.trim().replace(".", "-"); 
		String realnamespace="external."+namespace;
		if (param == null) {
			param = new HashMap<>();
			param.put("_offset_", offset);
			param.put("_limit_", limit);
		}
		try {

			return myBatiesService.selectList(sysSelectId, param);
		} catch (MyBatisSystemException e) {
			if (sqlLoaders == null) {
				return Collections.emptyList();
			}
			// lookup for select id
			if (ConcurrentSqlCreatorLocker.notExist(sysSelectId)) {
				synchronized (ConcurrentSqlCreatorLocker.getLocker(sysSelectId)) {
					if (ConcurrentSqlCreatorLocker.notExist(sysSelectId)) {
						String xml = null;

						String[] selectIdSplit = new StringBuilder(selectId).reverse().toString().split("[.]", 2);
						//String namespace = new StringBuilder(selectIdSplit[1]).reverse().toString();
						//String id = new StringBuilder(selectIdSplit[0]).reverse().toString();

						for (SqlLoader sqlLoader : sqlLoaders) {
							xml = sqlLoader.load(namespace, id);
							if (StringUtil.isNotEmpty(xml)) {
								LOG.info("found sql by loader:{}, selectId:{}, sql->{}", sqlLoader.name(), selectId, xml);
								continue;
							}
						}
						if (StringUtil.isEmpty(xml)) {
							return Collections.emptyList();
						}
						// has found sql,and regist to mybaties
						try {
							XPathParser xpath = new XPathParser(new ByteArrayInputStream(xml.getBytes("utf-8")), true, myBatiesService.getConfiguration().getVariables(),
									new XMLMapperEntityResolver());
							List<XNode> selects = xpath.evalNode("/mapper").evalNodes("select");
							XNode xnode = selects.get(0);// xpath.evalNodes("select|insert|update|delete");
							XMLLanguageDriver xmlLanguageDriver = new XMLLanguageDriver();
							SqlSource sqlsource = xmlLanguageDriver.createSqlSource(myBatiesService.getConfiguration(), xnode, HashMap.class);

							MapperBuilderAssistant mapperBuilder = new MapperBuilderAssistant(myBatiesService.getConfiguration(), sysSelectId);
							mapperBuilder.setCurrentNamespace(realnamespace);
							mapperBuilder.addMappedStatement(realSelectId, sqlsource, StatementType.PREPARED, SqlCommandType.SELECT, null, null, null, HashMap.class, null, HashMap.class, null, false,
									false, false, null, null, null, null, xmlLanguageDriver);

							ConcurrentSqlCreatorLocker.put(sysSelectId);
						} catch (Exception e1) {
							e1.printStackTrace();
						}

					}
				}

			}
			return myBatiesService.selectList(sysSelectId, param);
		}
	}

	private List<Map<String, ?>> select1(String selectId, Map<String, Object> param, int offset, int limit) {
		

		String namespace = selectId.split("[.]")[0];
		String id = selectId.split("[.]")[1]; 
		String sysSelectId = "external."+namespace+"." + selectId.trim().replace(".", "-");
		if (param == null) {
			param = new HashMap<>();
			param.put("_offset_", offset);
			param.put("_limit_", limit);
		}
		try {

			return myBatiesService.selectList(sysSelectId, param);
		} catch (MyBatisSystemException e) {
			if (sqlLoaders == null) {
				return Collections.emptyList();
			}
			// lookup for select id
			if (ConcurrentSqlCreatorLocker.notExist(sysSelectId)) {
				synchronized (ConcurrentSqlCreatorLocker.getLocker(sysSelectId)) {
					if (ConcurrentSqlCreatorLocker.notExist(sysSelectId)) {
						String sql = null; 
						for (SqlLoader sqlLoader : sqlLoaders) {
							sql = sqlLoader.load(namespace, id);
							if (StringUtil.isNotEmpty(sql)) {
								LOG.info("found sql by loader:{}, selectId:{}, sql->{}", sqlLoader.name(), selectId, sql);
								continue;
							}
						}
						if (StringUtil.isEmpty(sql)) {
							return Collections.emptyList();
						}
						// has found sql,and regist to mybaties
						try {
							XMLMapperBuilder selectMapperBuilder = new XMLMapperBuilder(new ByteArrayInputStream(sql.toString().getBytes("utf-8")), myBatiesService.getConfiguration(), sysSelectId,
									new HashMap<>());
							selectMapperBuilder.parse();

						} catch (UnsupportedEncodingException e1) {
							LOG.error("select", e1);
						}
						ConcurrentSqlCreatorLocker.put(sysSelectId);
					}
				}

			}
			return myBatiesService.selectList(sysSelectId, param);
		}
	}

	
	@Override
	public List<T> select(Class<T> entityClass,ColumnSelector<T> columns, QueryConditionWrapper<T> wrapper, SerializableFunction<T, Object> orderColumn, OrderTypeEnum orderType, int offset, int limit) {  
		String sql = QueryConditionWrapperParser.parse(entityClass, columns, wrapper) ;
		
		if(orderColumn!=null&&orderType!=null) {
			sql +=" order by #{_order_column_} #{_order_type_} ";
			Field filed = ReflectionUtil.findField(orderColumn); 
			wrapper.getContext().put("_order_column_", filed.getName());
			wrapper.getContext().put("_order_type_", orderType.getCode());
		}
		
		sql +=" limit #{_limit_} offset #{_offset_} ";
		String sqlId = "select_"+entityClass.getSimpleName()+"_"+StringUtil.md5(sql);
		String sqlKey= NAMESPACE.concat(".").concat(sqlId);
	  
		wrapper.getContext().put("_limit_", limit);
		wrapper.getContext().put("_offset_", offset);
 
		try {
			return myBatiesService.selectList(sqlKey, wrapper.getContext());
			
		}catch( MyBatisSystemException e) {  
			if(ConcurrentSqlCreatorLocker.notExist(sqlKey)) {
				synchronized (ConcurrentSqlCreatorLocker.getLocker(sqlKey)) {
					if(ConcurrentSqlCreatorLocker.notExist(sqlKey)) {
						try { 
							String xml = QueryConditionWrapperParser.toXml(entityClass,NAMESPACE,columns, sqlId,sql);
							XMLMapperBuilder selectMapperBuilder = new XMLMapperBuilder(new ByteArrayInputStream(xml.toString().getBytes("utf-8")), myBatiesService.getConfiguration(), sqlKey,
									new HashMap<>());
							selectMapperBuilder.parse();
							ConcurrentSqlCreatorLocker.put(sqlKey);
						}catch(Throwable e1) {
							LOG.error("select error",e); 
						}
					} 
				} 
			} 
			return myBatiesService.selectList(sqlKey, wrapper.getContext());
		} 
		 
	}
	
	@Override
	public long count(Class<T> entityClass, QueryConditionWrapper<T> wrapper) {
		String sql = QueryConditionWrapperParser.parseCount(entityClass, wrapper) ;
		 
		String sqlKey = "count_"+entityClass.getSimpleName()+"_"+StringUtil.md5(sql);
		String namespace="AUTOSQL";
		String namespace2="AUTOSQL."; 
 
		try {
			return myBatiesService.selectOne(namespace2.concat(sqlKey), wrapper.getContext());
			
		}catch( MyBatisSystemException e) {  
			if(ConcurrentSqlCreatorLocker.notExist(sqlKey)) {
				synchronized (ConcurrentSqlCreatorLocker.getLocker(sqlKey)) {
					if(ConcurrentSqlCreatorLocker.notExist(sqlKey)) {
						try { 
							String xml = QueryConditionWrapperParser.toCountXml(entityClass,namespace,sqlKey,sql);
							XMLMapperBuilder selectMapperBuilder = new XMLMapperBuilder(new ByteArrayInputStream(xml.toString().getBytes("utf-8")), myBatiesService.getConfiguration(), sqlKey,
									new HashMap<>());
							selectMapperBuilder.parse();
							
						}catch(Throwable e1) {
							LOG.error("select error",e); 
						}
					} 
				} 
			} 
			return myBatiesService.selectOne(namespace2.concat(sqlKey), wrapper.getContext());
		}  
	}
	
	
}
