package tech.codingless.core.plugs.mybaties3;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import javax.sql.DataSource;

import org.apache.ibatis.session.Configuration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.stereotype.Service;

import tech.codingless.core.plugs.mybaties3.conf.DataBaseConf;
import tech.codingless.core.plugs.mybaties3.data.DataEnvProperties;
import tech.codingless.core.plugs.mybaties3.enums.DataEnvEnums;
import tech.codingless.core.plugs.mybaties3.helper.DataSourceHelper;
import tech.codingless.core.plugs.mybaties3.helper.MybatiesExecuteHelper;
import tech.codingless.core.plugs.mybaties3.helper.PrepareParameterHelper;
import tech.codingless.core.plugs.mybaties3.strategy.DataSourceCreator;
import tech.codingless.core.plugs.mybaties3.util.MybatiesAssertUtil;
import tech.codingless.core.plugs.mybaties3.util.MybatiesIntegerUtil;
import tech.codingless.core.plugs.mybaties3.util.MybatiesSqlSourceUtil;
import tech.codingless.core.plugs.mybaties3.util.MybatiesStringUtil;

/**
 * 对 @SqlSessionTemplate 进行了一层封装
 * 
 * @author 王鸿雁
 * 
 */
@Order(Ordered.HIGHEST_PRECEDENCE) // Ordered.LOWEST_PRECEDENCE
@Service
public final class MyBatiesServiceDefaultImpl implements MyBatiesService {
	private static final Logger LOG = LoggerFactory.getLogger(GenericUpdateDAOImpl.class);

	@Autowired
	ApplicationContext context;

	@Autowired(required = false)
	DataSourceCreator dataSourceFactory;

	@Autowired(required = false)
	MybatiesDataSourceFactory mybatiesDataSourceFactory;

	@Autowired(required = false)
	SqlmapLoaderFactory sqlmapLoaderFactory;

	@Autowired(required = false)
	private DataBaseConf conf;

	@Autowired(required = false)
	void initDataBaseConfig(DataBaseConf conf) {
		LOG.info("Init DataBaseConf");
		init();
	}

	public MyBatiesServiceDefaultImpl() {

	}

	@Override
	public int update(String statement, Object parameter) {
		return DataSourceHelper.getSqlSessionTemplate().update(statement, parameter);
	}

	@Override
	public int insert(String statement, Object parameter) {
		return DataSourceHelper.getSqlSessionTemplate().insert(statement, parameter);
	}

	@Override
	public int delete(String statement, Object parameter) {
		return DataSourceHelper.getSqlSessionTemplate().delete(statement, parameter);
	}

	@Override
	public Configuration getConfiguration() {
		return DataSourceHelper.getSqlSessionTemplate().getConfiguration();
	}

	@Override
	public <T> T selectOne(String statement, Object parameter) {
		return DataSourceHelper.getSqlSessionTemplate().selectOne(statement, parameter);
	}

	@Override
	public <E> List<E> selectList(String statement, Object parameter) {
		return DataSourceHelper.getSqlSessionTemplate().selectList(statement, parameter);
	}

	@Override
	public <E> List<E> selectList(String statement) {
		return DataSourceHelper.getSqlSessionTemplate().selectList(statement);
	}

	@Override
	public String init() {
		return "ok";
	}

	public Object initSessionAndTransaction() {

		try {
			LOG.info("尝试创建事务管理器!");

			/**
			 * 主线程，设置默认的系统级别的数据环境
			 */
			DataEnvProperties.setDataSource("default");
			DataEnvProperties.setDatabase("test1");
			DataEnvProperties.setCompanyId("sys");
			DataEnvProperties.setOwnerId("sys");
			DataEnvProperties.setOptUserId("sys");
			DataEnvProperties.setEnv(DataEnvEnums.PRODUCT_EVN);
			DataEnvProperties.setGroupId("sys");
			DataEnvProperties.setDataLevel(99);
			DataEnvProperties.setDel(false);

			// 初始化默认数据库
			DataSource dataSource = null;
			if (conf != null && MybatiesStringUtil.isNotEmpty(conf.getUrl(), conf.getUsername(), conf.getPassword())) {
				DataSourceHelper.JdbcProperties jdbcProperties = new DataSourceHelper.JdbcProperties();
				jdbcProperties.setUrl(conf.getUrl());
				jdbcProperties.setUser(conf.getUsername());
				jdbcProperties.setPassword(conf.getPassword());
				jdbcProperties.setMaxIdle(MybatiesIntegerUtil.get(conf.getMaxIdle(), 20));
				jdbcProperties.setMinIdle(MybatiesIntegerUtil.get(conf.getMinIdle(), 3));
				jdbcProperties.setMaxTotal(MybatiesIntegerUtil.get(conf.getMaxTotal(), 20));
				jdbcProperties.setMaxWaitMillis(MybatiesIntegerUtil.get(conf.getMaxWaitMillis(), 10000));
				jdbcProperties.setInitialSize(MybatiesIntegerUtil.get(conf.getInitialSize(), 3));
				jdbcProperties.setRemoveAbandonedOnBorrow(true);
				DataSourceHelper.init("default", jdbcProperties);
				dataSource = DataSourceHelper.getDataSource("default");
			}

			if (dataSource == null) {
				/**
				 * 当数据库配置不存在的时候<br>
				 * 这是一个假的事管理器，只是为了启动的时候不报错而创建的!
				 */
				return new MyEmptyDataSourceTransactionManager();
			}

			// 合并加载系统sqlmap及用户自定义sqlmap
			PathMatchingResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
			Resource[] xDefSqlMapper = sqlmapLoaderFactory != null ? sqlmapLoaderFactory.sqlMapperResource() : null;
			List<Resource> mergeSqlMappers = new ArrayList<>();
			mergeSqlMappers.addAll(Arrays.asList(resolver.getResources("classpath*:tech/codingless/core/plugs/mybaties3/**/*Mapper.xml")));
			if (MybatiesStringUtil.isNotEmpty(conf.getClasspathMapper())) {
				List.of(conf.getClasspathMapper().split(",")).stream().filter(item -> MybatiesStringUtil.isNotEmpty(item)).forEach(item -> {
					try {
						mergeSqlMappers.addAll(Arrays.asList(resolver.getResources("classpath*:" + item)));
					} catch (Exception e) {
						LOG.error("load mapper fail ", e);
					}
				});
			}

			if (xDefSqlMapper != null) {
				mergeSqlMappers.addAll(Arrays.asList(xDefSqlMapper));
			}
			Resource[] mergedResourceList = mergeSqlMappers.toArray(new Resource[0]);
			DataSourceHelper.setSqlmapResource(mergedResourceList);
			// 自定义事务管理器
			MyDataSourceTransactionManager dataSourceTransactionManager = new MyDataSourceTransactionManager();
			dataSourceTransactionManager.setDataSource(dataSource);

			/*
			 * SqlSessionFactoryBean sqlSessionFactoryBean = new SqlSessionFactoryBean();
			 * sqlSessionFactoryBean.setDataSource(dataSource);
			 * sqlSessionFactoryBean.setMapperLocations(mergedResourceList);
			 * sqlSessionFactoryBean.setTransactionFactory(new
			 * SpringManagedTransactionFactory()); FactoryBean<SqlSessionFactory>
			 * factoryBean = sqlSessionFactoryBean; session = new
			 * SqlSessionTemplate(factoryBean.getObject());
			 */
			// =========
			// DataSourceHelper.setSqlmapResource(mergedResourceList);
			// DataSourceHelper.getSqlSessionTemplate("ds");

			LOG.info("事务管理器:" + dataSourceTransactionManager);
			return dataSourceTransactionManager;
		} catch (Exception e) {
			LOG.error("创建事务", e);
		}
		return null;
	}

	@Override
	public int executeUpdateSql(String sql, List<Object> param) {
		try {
			PreparedStatement ps = DataSourceHelper.getSqlSessionTemplate().getConnection().prepareStatement(sql);
			PrepareParameterHelper.bindParam(ps, param);
			return ps.executeUpdate();
		} catch (SQLException e) {
			LOG.error("执行SQL出错", e);
			return 0;
		} finally {
			try {
				DataSourceHelper.getSqlSessionTemplate().getConnection().close();
			} catch (SQLException e) {
				LOG.error("close Connection ", e);
			}
		}
	}

	@Override
	public long execinsert(String xmlInsertSql, Map<String, Object> param) {
		try {
			return MybatiesExecuteHelper.execinsert(MybatiesSqlSourceUtil.exchangeInsertSqlSource(xmlInsertSql, param), param);
		} catch (Exception e) {
			LOG.error("EXECUTE_INSERT_ERROR", e);
			MybatiesAssertUtil.assertFail("EXECUTE_INSERT_ERROR");
		}
		return -1;
	}

}
