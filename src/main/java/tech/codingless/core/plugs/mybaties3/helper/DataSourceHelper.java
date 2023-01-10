package tech.codingless.core.plugs.mybaties3.helper;

import java.util.Collection;
import java.util.concurrent.ConcurrentHashMap;

import javax.sql.DataSource;

import org.apache.commons.dbcp2.BasicDataSource;
import org.apache.ibatis.session.SqlSessionFactory;
import org.mybatis.spring.SqlSessionFactoryBean;
import org.mybatis.spring.SqlSessionTemplate;
import org.mybatis.spring.transaction.SpringManagedTransactionFactory;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.core.io.Resource;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import tech.codingless.core.plugs.mybaties3.data.DataEnvProperties;
import tech.codingless.core.plugs.mybaties3.exception.DataSourceNotExistException;
import tech.codingless.core.plugs.mybaties3.util.LockerUtil;
import tech.codingless.core.plugs.mybaties3.util.LockerUtil.Locker;
import tech.codingless.core.plugs.mybaties3.util.MybatiesStringUtil;

@Slf4j
public class DataSourceHelper {

	private static Resource[] resources;

	@Data
	public static class JdbcProperties {
		private String url;
		private String user;
		private String password;
		private int maxIdle;
		private int minIdle;
		private int maxTotal;
		private long maxWaitMillis;
		private int initialSize;
		private boolean removeAbandonedOnBorrow;
	}

	private static ConcurrentHashMap<String, JdbcProperties> JDBC_SETTING = new ConcurrentHashMap<>();
	private static ConcurrentHashMap<String, DataSource> DATA_SOURCE_CACHE = new ConcurrentHashMap<>();

	public static void setSqlmapResource(Resource[] resource) {
		resources = resource;
	}

	/**
	 * 获得一个数据源
	 * 
	 * @param dataSourceId
	 * @return
	 */
	public static DataSource getDataSource(String dataSourceId) {
		dataSourceId = dataSourceId.toLowerCase();
		DataSource dataSource = DATA_SOURCE_CACHE.get(dataSourceId);
		if (dataSource != null) {
			return dataSource;
		}

		JdbcProperties jdbc = JDBC_SETTING.get(dataSourceId);
		if (jdbc == null) {
			throw new DataSourceNotExistException(
					String.format("Thread ( %s ) Can't Found DataSource With Company:%s, DataSourceId:%s", Thread.currentThread().getName(), DataEnvProperties.getCompanyId(), dataSourceId));
		}

		Locker locker = LockerUtil.getLocker("dataSourceId:" + dataSourceId);
		try {
			locker.lock();
			dataSource = DATA_SOURCE_CACHE.get(dataSourceId);
			if (dataSource != null) {
				return dataSource;
			}
			BasicDataSource basicDataSource = new BasicDataSource();
			basicDataSource.setDriverClassName("com.mysql.cj.jdbc.Driver");
			basicDataSource.setUrl(jdbc.getUrl());
			basicDataSource.setUsername(jdbc.getUser());
			basicDataSource.setPassword(jdbc.getPassword());
			basicDataSource.setMaxIdle(jdbc.getMaxIdle() == 0 ? 10 : jdbc.getMaxIdle());
			basicDataSource.setMinIdle(jdbc.getMinIdle() == 0 ? 3 : jdbc.getMinIdle());
			basicDataSource.setMaxTotal(jdbc.getMaxTotal() == 0 ? 20 : jdbc.getMaxTotal());
			basicDataSource.setMaxWaitMillis(jdbc.getMaxWaitMillis() == 0 ? 10000 : jdbc.getMaxWaitMillis());
			basicDataSource.setInitialSize(jdbc.getInitialSize() == 0 ? 3 : jdbc.getInitialSize());
			basicDataSource.setRemoveAbandonedOnBorrow(jdbc.isRemoveAbandonedOnBorrow());
			DATA_SOURCE_CACHE.put(dataSourceId, basicDataSource);
			return basicDataSource;

		} finally {
			locker.unlock();
		}
	}

	private static ConcurrentHashMap<String, SqlSessionTemplate> SQL_SESSION_CACHE = new ConcurrentHashMap<>();

	public static SqlSessionTemplate getSqlSessionTemplate(String dataSourceId) {
		dataSourceId = dataSourceId.toLowerCase();
		SqlSessionTemplate session = SQL_SESSION_CACHE.get(dataSourceId);
		if (session != null) {
			return session;
		}

		DataSource dataSource = getDataSource(dataSourceId);
		if (dataSource == null) {
			throw new DataSourceNotExistException(
					String.format("Thread ( %s ) Can't Found DataSource With Company:%s, DataSourceId:%s", Thread.currentThread().getName(), DataEnvProperties.getCompanyId(), dataSourceId));
		}

		Locker locker = LockerUtil.getLocker("dataSourceId:" + dataSourceId);

		try {
			locker.lock();

			session = SQL_SESSION_CACHE.get(dataSourceId);
			if (session != null) {
				return session;
			}
			SqlSessionFactoryBean sqlSessionFactoryBean = new SqlSessionFactoryBean();
			sqlSessionFactoryBean.setDataSource(dataSource);
			if (resources != null) {
				sqlSessionFactoryBean.setMapperLocations(resources);
			}
			sqlSessionFactoryBean.setTransactionFactory(new SpringManagedTransactionFactory());
			FactoryBean<SqlSessionFactory> factoryBean = sqlSessionFactoryBean;
			session = new SqlSessionTemplate(factoryBean.getObject());
			SQL_SESSION_CACHE.put(dataSourceId, session);
			return session;
		} catch (Exception e) {
			log.error("Create Sql Session Error", e);
		} finally {
			locker.unlock();
		}
		return null;
	}

	public static void init(String dataSourceId, JdbcProperties jdbcProperties) {
		dataSourceId = dataSourceId.toLowerCase();
		JDBC_SETTING.remove(dataSourceId);
		DATA_SOURCE_CACHE.remove(dataSourceId);
		SQL_SESSION_CACHE.remove(dataSourceId);
		JDBC_SETTING.put(dataSourceId.toLowerCase(), jdbcProperties);
	}

	/**
	 * <pre>
	 * 根据数据上下文获取数据源
	 * DataEnvProperties.getDataSource()
	 * </pre>
	 * 
	 * @return
	 */
	public static SqlSessionTemplate getSqlSessionTemplate() {
		String dataSourceId = DataEnvProperties.getDataSource();
		return MybatiesStringUtil.isNotEmpty(dataSourceId) ? getSqlSessionTemplate(dataSourceId) : null;
	}

	/**
	 * <pre>
	 * 根据上下文获得数据源
	 * DataEnvProperties.getDataSource()
	 * </pre>
	 * 
	 * @return
	 */
	public static DataSource getDataSource() {
		String dataSourceId = DataEnvProperties.getDataSource();
		if (MybatiesStringUtil.isEmpty(dataSourceId) && "main".equalsIgnoreCase(Thread.currentThread().getName())) {
			// 暂时以这种方法兼容一个错误，junit测试后可能会自动清理线程变量，所以会出错
			// 原理：main主线程即使用 default环境
			dataSourceId = DataEnvProperties.getDefaultDataSourceId();
		}
		DataSource datasource = MybatiesStringUtil.isNotEmpty(dataSourceId) ? getDataSource(dataSourceId) : null;
		if (datasource == null) {
			throw new DataSourceNotExistException(
					String.format("Thread ( %s ) Can't Found DataSource With Company:%s, DataSourceId:%s", Thread.currentThread().getName(), DataEnvProperties.getCompanyId(), dataSourceId));
		}
		return datasource;
	}

	public static Collection<DataSource> listAllDataSource() {
		return DATA_SOURCE_CACHE.values();
	}

}
