package tech.codingless.core.plugs.mybaties3;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.dbcp2.BasicDataSource;
import org.apache.ibatis.builder.SqlSourceBuilder;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.mapping.SqlCommandType;
import org.apache.ibatis.mapping.SqlSource;
import org.mybatis.spring.MyBatisSystemException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import com.alibaba.fastjson2.JSON;

import tech.codingless.core.plugs.mybaties3.conf.ColumnNameConstant;
import tech.codingless.core.plugs.mybaties3.conf.DataBaseConf;
import tech.codingless.core.plugs.mybaties3.data.BaseDO;
import tech.codingless.core.plugs.mybaties3.data.UpdateObject;
import tech.codingless.core.plugs.mybaties3.helper.AutoCreateBatchHelper;
import tech.codingless.core.plugs.mybaties3.helper.AutoDeleteBatchHelper;
import tech.codingless.core.plugs.mybaties3.helper.AutoUpdateHelper;
import tech.codingless.core.plugs.mybaties3.helper.AutoUpinsertBatchHelper;
import tech.codingless.core.plugs.mybaties3.helper.CommonSQLHelper;
import tech.codingless.core.plugs.mybaties3.helper.MyTypeHanderRegistHelper;
import tech.codingless.core.plugs.mybaties3.helper.PrepareParameterHelper;
import tech.codingless.core.plugs.mybaties3.helper.UpdateSkipNullBatchAppendHelper;
import tech.codingless.core.plugs.mybaties3.util.DataSessionEnv;
import tech.codingless.core.plugs.mybaties3.util.MybatiesStringUtil;

@Component
public class GenericUpdateDAOImpl<T extends BaseDO> implements GenericUpdateDao<T> {
	private static final String NAMESPACE = "AUTOSQL";
	private static final Logger LOG = LoggerFactory.getLogger(GenericUpdateDAOImpl.class);
	protected MyBatiesService myBatiesService;

	protected BasicDataSource basicDataSource;

	@Autowired(required = false)
	private DataBaseConf conf;

	@Autowired
	protected void setMyBatiesService(MyBatiesService myBatiesService) {
		LOG.info("Jnjection Data Access Service: {}", myBatiesService);
		this.myBatiesService = myBatiesService;
		if (conf != null && MybatiesStringUtil.isNotEmpty(conf.getUrl(), conf.getUsername(), conf.getPassword())) {
			basicDataSource = new BasicDataSource();
			basicDataSource.setDriverClassName("com.mysql.cj.jdbc.Driver");
			basicDataSource.setUrl(conf.getUrl());
			basicDataSource.setUsername(conf.getUsername());
			basicDataSource.setPassword(conf.getPassword());
			basicDataSource.setMaxIdle(30);
			basicDataSource.setMinIdle(3);
			basicDataSource.setMaxTotal(30);
			basicDataSource.setMaxWaitMillis(10000);
			basicDataSource.setInitialSize(3);
			basicDataSource.setRemoveAbandonedOnBorrow(true);
			basicDataSource.setRemoveAbandonedTimeout(180);
			LOG.info("Create BasicDataSource : {}", basicDataSource);
		}
	}

	@Override
	public int update(String sqlId, Object param) {
		return myBatiesService.update(sqlId, param);
	}

	@Override
	public int insert(String sqlId, Object param) {
		return myBatiesService.insert(sqlId, param);
	}

	@Override
	public int delete(String sqlId, Object param) {
		return myBatiesService.delete(sqlId, param);
	}

	public void genAutoSqlForCreate(Object entity) {
		try {

			MyTypeHanderRegistHelper.regist(myBatiesService.getConfiguration(), entity.getClass());
			String sqlKey = "AUTOSQL.CREATE_" + CommonSQLHelper.getTableName(entity);
			SqlSourceBuilder sqlSourceBuilder = new SqlSourceBuilder(myBatiesService.getConfiguration());
			SqlSource sqlSource = sqlSourceBuilder.parse(CommonSQLHelper.getInsertSQL(entity), entity.getClass(), null);
			MappedStatement.Builder builder = new MappedStatement.Builder(myBatiesService.getConfiguration(), sqlKey, sqlSource, SqlCommandType.INSERT);
			myBatiesService.getConfiguration().addMappedStatement(builder.build());
			if (LOG.isDebugEnabled()) {
				// LOG.debug("gen script : " + CommonSQLHelper.getInsertSQL(entity));
			}
		} catch (Exception e) {
			LOG.error("genAutoSqlForCreate", e);
		}
	}

	@Override
	public int createEntity(Object entity) {
		String sqlKey = "AUTOSQL.CREATE_" + CommonSQLHelper.getTableName(entity);
		try {
			return myBatiesService.insert(sqlKey, entity);
		} catch (MyBatisSystemException e) {
			if (ConcurrentSqlCreatorLocker.notExist(sqlKey)) {
				synchronized (ConcurrentSqlCreatorLocker.getLocker(sqlKey)) {
					if (ConcurrentSqlCreatorLocker.notExist(sqlKey)) {
						genAutoSqlForCreate(entity);
						ConcurrentSqlCreatorLocker.put(sqlKey);
					}
				}
			}

			return myBatiesService.insert(sqlKey, entity);
		}

	}

	@Override
	public int upinsert(List<T> entityList) {
		String sqlKey = "AUTOSQL.UPINSERT_BATCH_" + CommonSQLHelper.getTableName(entityList.get(0));
		try {
			return myBatiesService.insert(sqlKey, entityList);
		} catch (MyBatisSystemException e) {

			if (ConcurrentSqlCreatorLocker.notExist(sqlKey)) {
				synchronized (ConcurrentSqlCreatorLocker.getLocker(sqlKey)) {
					if (ConcurrentSqlCreatorLocker.notExist(sqlKey)) {
						AutoUpinsertBatchHelper.genBatchCreateSql(myBatiesService.getConfiguration(), "AUTOSQL", sqlKey, entityList.get(0).getClass());
						ConcurrentSqlCreatorLocker.put(sqlKey);
					}
				}
			}

			return myBatiesService.insert(sqlKey, entityList);
		}
	}

	@Override
	public long execinsert(String xmlInsertSql, Map<String, Object> param) {
		return myBatiesService.execinsert(xmlInsertSql, param);
	}

	@Override
	public int createEntityList(List<T> entityList) {
		String sqlKey = "AUTOSQL.CREATE_BATCH_" + CommonSQLHelper.getTableName(entityList.get(0));
		try {
			return myBatiesService.insert(sqlKey, entityList);
		} catch (MyBatisSystemException e) {
			if (ConcurrentSqlCreatorLocker.notExist(sqlKey)) {
				synchronized (ConcurrentSqlCreatorLocker.getLocker(sqlKey)) {
					if (ConcurrentSqlCreatorLocker.notExist(sqlKey)) {
						AutoCreateBatchHelper.genBatchCreateSql(myBatiesService.getConfiguration(), "AUTOSQL", sqlKey, entityList.get(0).getClass());
						ConcurrentSqlCreatorLocker.put(sqlKey);
					}
				}
			}
			return myBatiesService.insert(sqlKey, entityList);
		}
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Override
	public int deleteEntity(Class<T> clazz, String entityId) {
		String sqlKey = "AUTOSQL.DELETE_" + CommonSQLHelper.getTableName(clazz);
		Map p = new HashMap();
		p.put("entityId", entityId);
		p.put("del_user", DataSessionEnv.CURRENT_USER_ID.get());
		// 测试的时候将权限默认为root
		// p.put("del_user", "root");
		try {
			return myBatiesService.delete(sqlKey, p);
		} catch (MyBatisSystemException e) {

			if (ConcurrentSqlCreatorLocker.notExist(sqlKey)) {
				synchronized (ConcurrentSqlCreatorLocker.getLocker(sqlKey)) {
					if (ConcurrentSqlCreatorLocker.notExist(sqlKey)) {
						SqlSourceBuilder sqlSourceBuilder = new SqlSourceBuilder(myBatiesService.getConfiguration());
						String deleteSQL = CommonSQLHelper.getDeleteSQL(clazz);
						LOG.info("[动态生成SQL语句] " + deleteSQL);
						SqlSource sqlSource = sqlSourceBuilder.parse(deleteSQL, Map.class, null);
						MappedStatement.Builder builder = new MappedStatement.Builder(myBatiesService.getConfiguration(), sqlKey, sqlSource, SqlCommandType.DELETE);
						myBatiesService.getConfiguration().addMappedStatement(builder.build());
						ConcurrentSqlCreatorLocker.put(sqlKey);
					}
				}
			}

			return myBatiesService.delete(sqlKey, p);
		}
	}

	@Override
	public int deleteEntityWithCompanyId(Class<T> clazz, String id, String companyId) {
		String sqlKey = "AUTOSQL.DELETE_WIDTH_COMPANY_ID" + CommonSQLHelper.getTableName(clazz);
		Map<String, String> p = new HashMap<>();
		p.put("entityId", id);
		p.put("company_id", companyId);
		try {
			return myBatiesService.delete(sqlKey, p);
		} catch (MyBatisSystemException e) {

			if (ConcurrentSqlCreatorLocker.notExist(sqlKey)) {
				synchronized (ConcurrentSqlCreatorLocker.getLocker(sqlKey)) {
					if (ConcurrentSqlCreatorLocker.notExist(sqlKey)) {
						SqlSourceBuilder sqlSourceBuilder = new SqlSourceBuilder(myBatiesService.getConfiguration());
						String deleteSQL = CommonSQLHelper.getDeleteWithCompanyIdSQL(clazz);
						LOG.info("[动态生成SQL语句] " + deleteSQL);
						SqlSource sqlSource = sqlSourceBuilder.parse(deleteSQL, Map.class, null);
						MappedStatement.Builder builder = new MappedStatement.Builder(myBatiesService.getConfiguration(), sqlKey, sqlSource, SqlCommandType.DELETE);
						myBatiesService.getConfiguration().addMappedStatement(builder.build());
						ConcurrentSqlCreatorLocker.put(sqlKey);
					}
				}
			}
			return myBatiesService.delete(sqlKey, p);
		}
	}

	@Override
	public int deleteLogicalWithCompanyId(Class<T> clazz, String id, String companyId) {
		String sqlKey = "AUTOSQL.DELETE_LOGICAL_WIDTH_COMPANY_ID" + CommonSQLHelper.getTableName(clazz);
		Map<String, String> p = new HashMap<>();
		p.put("entityId", id);
		p.put(ColumnNameConstant.COMPANY_ID, companyId);
		try {
			return myBatiesService.update(sqlKey, p);
		} catch (MyBatisSystemException e) {

			if (ConcurrentSqlCreatorLocker.notExist(sqlKey)) {
				synchronized (ConcurrentSqlCreatorLocker.getLocker(sqlKey)) {
					if (ConcurrentSqlCreatorLocker.notExist(sqlKey)) {
						SqlSourceBuilder sqlSourceBuilder = new SqlSourceBuilder(myBatiesService.getConfiguration());
						String deleteSQL = CommonSQLHelper.getDeleteLogicalWithCompanyIdSQL(clazz);
						LOG.info("[动态生成SQL语句] " + deleteSQL);
						SqlSource sqlSource = sqlSourceBuilder.parse(deleteSQL, Map.class, null);
						MappedStatement.Builder builder = new MappedStatement.Builder(myBatiesService.getConfiguration(), sqlKey, sqlSource, SqlCommandType.DELETE);
						myBatiesService.getConfiguration().addMappedStatement(builder.build());
						ConcurrentSqlCreatorLocker.put(sqlKey);
					}
				}
			}
			return myBatiesService.update(sqlKey, p);
		}
	}

	@Override
	public int deleteLogicalWithCompanyId(Class<T> clazz, Collection<String> idList, String companyId) {
		return AutoDeleteBatchHelper.deleteLogical(myBatiesService, clazz, idList, companyId);
	}

	@Override
	public int updateEntity(BaseDO entiry) {
		String sqlKey = "AUTOSQL.UPDATE_" + CommonSQLHelper.getTableName(entiry);
		try {
			return myBatiesService.update(sqlKey, entiry);
		} catch (MyBatisSystemException e) {

			if (ConcurrentSqlCreatorLocker.notExist(sqlKey)) {
				synchronized (ConcurrentSqlCreatorLocker.getLocker(sqlKey)) {
					if (ConcurrentSqlCreatorLocker.notExist(sqlKey)) {
						genAutoSqlForUpdate(entiry);
						ConcurrentSqlCreatorLocker.put(sqlKey);
					}
				}
			}

			return myBatiesService.update(sqlKey, entiry);
		}
	}

	@Override
	public int updateEntityWithCompanyId(BaseDO entiry, String companyId) {
		String sqlKey = "AUTOSQL.UPDATE_WITH_COMPANY_ID" + CommonSQLHelper.getTableName(entiry);
		entiry.setCompanyId(companyId);
		try {
			return myBatiesService.update(sqlKey, entiry);
		} catch (MyBatisSystemException e) {

			if (ConcurrentSqlCreatorLocker.notExist(sqlKey)) {
				synchronized (ConcurrentSqlCreatorLocker.getLocker(sqlKey)) {
					if (ConcurrentSqlCreatorLocker.notExist(sqlKey)) {
						SqlSourceBuilder sqlSourceBuilder = new SqlSourceBuilder(myBatiesService.getConfiguration());
						String updateSQL = CommonSQLHelper.getUpdateSQLWithCompanyId(entiry.getClass());
						SqlSource sqlSource = sqlSourceBuilder.parse(updateSQL, entiry.getClass(), null);
						MappedStatement.Builder builder = new MappedStatement.Builder(myBatiesService.getConfiguration(), sqlKey, sqlSource, SqlCommandType.UPDATE);
						myBatiesService.getConfiguration().addMappedStatement(builder.build());
						if (LOG.isDebugEnabled()) {
							LOG.info("[动态生成SQL语句] " + updateSQL);
						}
						ConcurrentSqlCreatorLocker.put(sqlKey);
					}
				}
			}

			return myBatiesService.update(sqlKey, entiry);
		}
	}

	@Override
	public int updateNotNull(T data, Long ver) {
		String sqlId = "UPDATE_SKIP_NULLV2_" + CommonSQLHelper.getTableName(data);
		String sqlKey = NAMESPACE.concat(".").concat(sqlId);
		try {
			data.setVer(ver);
			return myBatiesService.update(sqlKey, data);
		} catch (MyBatisSystemException e) {
			if (ConcurrentSqlCreatorLocker.notExist(sqlKey)) {
				synchronized (ConcurrentSqlCreatorLocker.getLocker(sqlKey)) {
					if (ConcurrentSqlCreatorLocker.notExist(sqlKey)) {
						AutoUpdateHelper.genUpdateSkipNullSql(this.myBatiesService.getConfiguration(), NAMESPACE, sqlId, data.getClass());
						ConcurrentSqlCreatorLocker.put(sqlKey);
					}
				}
			}
			return myBatiesService.update(sqlKey, data);
		}
	}

	@Override
	public int updateSkipNullBatchAppend(String companyId, T data, Long ver, int batchSize) {
		return UpdateSkipNullBatchAppendHelper.updateSkipNullBatchAppend(myBatiesService, companyId, data, ver, batchSize);
	}

	@Override
	public int updateSkipNullBatchExecute(Class<T> clazz) {
		return UpdateSkipNullBatchAppendHelper.updateSkipNullBatchExecute(myBatiesService, clazz);
	}

	@Override
	public int updateSkipNullBatchExecute(List<UpdateObject> updateList) {
		return UpdateSkipNullBatchAppendHelper.updateSkipNullBatchExecute(myBatiesService, updateList);
	}

	public void genAutoSqlForUpdate(BaseDO entiry) {
		String sqlKey = "AUTOSQL.UPDATE_" + CommonSQLHelper.getTableName(entiry);
		SqlSourceBuilder sqlSourceBuilder = new SqlSourceBuilder(myBatiesService.getConfiguration());
		String updateSQL = CommonSQLHelper.getUpdateSQL(entiry.getClass());
		SqlSource sqlSource = sqlSourceBuilder.parse(updateSQL, entiry.getClass(), null);
		MappedStatement.Builder builder = new MappedStatement.Builder(myBatiesService.getConfiguration(), sqlKey, sqlSource, SqlCommandType.UPDATE);
		myBatiesService.getConfiguration().addMappedStatement(builder.build());
		if (LOG.isDebugEnabled()) {
			// LOG.debug("[动态生成SQL语句] " + updateSQL);
		}
	}

	@Override
	public int insertNative(String sql, List<Object> params) {
		try (Connection conn = basicDataSource.getConnection()) {
			PreparedStatement pstmt = conn.prepareStatement(sql);
			PrepareParameterHelper.bindParam(pstmt, params);
			pstmt.execute();
			if (LOG.isDebugEnabled()) {
				LOG.info("insertNative:{},PARAM:{}", sql, !CollectionUtils.isEmpty(params) ? JSON.toJSONString(params) : "NONE");
			}
		} catch (SQLException e) {
			LOG.error("insertNative", e);
		}
		return 1;
	}

	@Override
	public int updateNative(String prepareSql, List<Object> params) {
		try (Connection conn = basicDataSource.getConnection()) {
			PreparedStatement pstmt = conn.prepareStatement(prepareSql);
			PrepareParameterHelper.bindParam(pstmt, params);
			int effect = pstmt.executeUpdate();
			if (LOG.isDebugEnabled()) {
				LOG.info("updateNative:{},PARAM:{}", prepareSql, !CollectionUtils.isEmpty(params) ? JSON.toJSONString(params) : "NONE");
			}
			return effect;
		} catch (SQLException e) {
			LOG.error("updateNative", e);
		}
		return 0;
	}

}
