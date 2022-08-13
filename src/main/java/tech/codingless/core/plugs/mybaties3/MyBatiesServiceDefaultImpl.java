package tech.codingless.core.plugs.mybaties3;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.sql.DataSource;

import org.apache.commons.dbcp2.BasicDataSource;
import org.apache.ibatis.session.Configuration;
import org.apache.ibatis.session.SqlSessionFactory;
import org.mybatis.spring.SqlSessionFactoryBean;
import org.mybatis.spring.SqlSessionTemplate;
import org.mybatis.spring.transaction.SpringManagedTransactionFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.core.annotation.Order;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.stereotype.Service;

import tech.codingless.core.plugs.mybaties3.conf.DataBaseConf;
import tech.codingless.core.plugs.mybaties3.helper.PrepareParameterHelper;
import tech.codingless.core.plugs.mybaties3.strategy.DataSourceCreator;
import tech.codingless.core.plugs.mybaties3.util.MybatiesAssertUtil;
import tech.codingless.core.plugs.mybaties3.util.MybatiesStringUtil;

/**
 * 对 @SqlSessionTemplate 进行了一层封装
 * 
 * @author 王鸿雁
 * 
 */
@Order(1)
@Service
public final class MyBatiesServiceDefaultImpl implements MyBatiesService {
	private static final Logger LOG = LoggerFactory.getLogger(GenericUpdateDAOImpl.class);

	private SqlSessionTemplate noShardingSession;

	private SqlSessionTemplate session;
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

		return session.update(statement, parameter);
	}

	@Override
	public int insert(String statement, Object parameter) {
		return session.insert(statement, parameter);
	}

	@Override
	public int delete(String statement, Object parameter) {
		return session.delete(statement, parameter);
	}

	@Override
	public Configuration getConfiguration() {
		return session.getConfiguration();
	}

	@Override
	public <T> T selectOne(String statement, Object parameter) {
		return session.selectOne(statement, parameter);
	}

	@Override
	public <E> List<E> selectList(String statement, Object parameter) {
		return session.selectList(statement, parameter);
	}

	@Override
	public <E> List<E> selectList(String statement) {
		return session.selectList(statement);
	}

	@Override
	public String init() {
		return "ok";
	}

	public Object initSessionAndTransaction() {

		try {
			LOG.info("尝试创建事务管理器!");

			DataSource dataSource = null;
			if (dataSourceFactory != null) {
				dataSource = dataSourceFactory.make();
				LOG.info("通过[" + dataSourceFactory + "]创建数据源：" + dataSource);
			}
			if (dataSource == null) {

				// 加载用户安装目录下数据库配置信息
				if (conf != null && MybatiesStringUtil.isNotEmpty(conf.getUrl(), conf.getUsername(), conf.getPassword())) {
					BasicDataSource basicDataSource = new BasicDataSource();
					basicDataSource.setDriverClassName("com.mysql.cj.jdbc.Driver");
					basicDataSource.setUrl(conf.getUrl());
					basicDataSource.setUsername(conf.getUsername());
					basicDataSource.setPassword(conf.getPassword());
					basicDataSource.setMaxIdle(20);
					basicDataSource.setMinIdle(3);
					basicDataSource.setMaxTotal(20);
					basicDataSource.setMaxWaitMillis(10000);
					basicDataSource.setInitialSize(3);
					basicDataSource.setRemoveAbandonedOnBorrow(true);
					basicDataSource.setRemoveAbandonedTimeout(180);
					dataSource = basicDataSource;
					LOG.info("创建数据源：" + dataSource);
				}

			}

			if (dataSource == null) {
				/**
				 * 当数据库配置不存在的时候<br>
				 * 这是一个假的事管理器，只是为了启动的时候不报错而创建的!
				 */
				return new MyEmptyDataSourceTransactionManager();
			}

			DataSourceTransactionManager dataSourceTransactionManager = new DataSourceTransactionManager();
			session = null;
			dataSourceTransactionManager.setDataSource(dataSource);

			PathMatchingResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
			Resource[] xDefSqlMapper = sqlmapLoaderFactory != null ? sqlmapLoaderFactory.sqlMapperResource() : null;
			List<Resource> mergeSqlMappers = new ArrayList<>();
			// Resource[] resourcesList =
			// resolver.getResources("classpath*:tech/codingless/biz/**/*Mapper.xml");
			// mergeSqlMappers.addAll(Arrays.asList(resourcesList));

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
			SqlSessionFactoryBean sqlSessionFactoryBean = new SqlSessionFactoryBean();
			sqlSessionFactoryBean.setDataSource(dataSource);
			sqlSessionFactoryBean.setMapperLocations(mergedResourceList);
			sqlSessionFactoryBean.setTransactionFactory(new SpringManagedTransactionFactory());

			FactoryBean<SqlSessionFactory> factoryBean = sqlSessionFactoryBean;
			session = new SqlSessionTemplate(factoryBean.getObject());
			LOG.info("初始化事务管理器(添加到Spring容器中)： " + dataSourceTransactionManager);

			if (mybatiesDataSourceFactory != null) {
				DataSource noShardingDatasource = mybatiesDataSourceFactory.make();
				SqlSessionFactoryBean noShardingFb = new SqlSessionFactoryBean();
				noShardingFb.setDataSource(noShardingDatasource);
				noShardingFb.setMapperLocations(mergedResourceList);
				noShardingFb.setTransactionFactory(new SpringManagedTransactionFactory());
				FactoryBean<SqlSessionFactory> noShardingBean = noShardingFb;
				noShardingSession = new SqlSessionTemplate(noShardingBean.getObject());
				LOG.info("初始化 No-Sharding Session:" + noShardingSession);
			}

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
			PreparedStatement ps = session.getConnection().prepareStatement(sql);
			PrepareParameterHelper.bindParam(ps, param);
			return ps.executeUpdate();
		} catch (SQLException e) {
			LOG.error("执行SQL出错", e);
			return 0;
		} finally {

			try {
				session.getConnection().close();
			} catch (SQLException e) {
				LOG.error("close Connection ", e);
			}
		}
	}

	@Override
	public <E> List<E> selectListNoSharding(String statement, Object parameter) {
		MybatiesAssertUtil.assertNotNull(noShardingSession, "NO-SHARDING-SESSION-NOT-EXIST:未分片数据源不存在");
		return noShardingSession.selectList(statement, parameter);
	}

}
