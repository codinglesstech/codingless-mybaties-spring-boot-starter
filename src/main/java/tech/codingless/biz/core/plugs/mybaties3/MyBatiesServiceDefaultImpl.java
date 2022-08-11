package tech.codingless.biz.core.plugs.mybaties3;

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
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.Bean;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.stereotype.Service;
import org.springframework.web.context.ConfigurableWebApplicationContext;

import tech.codingless.biz.core.plugs.mybaties3.conf.DataBaseConf;
import tech.codingless.biz.core.plugs.mybaties3.helper.PrepareParameterHelper;
import tech.codingless.biz.core.util.AssertUtil;
import tech.codingless.biz.core.util.StringUtil;

/**
 * 对 @SqlSessionTemplate 进行了一层封装
 * 
 * @author 王鸿雁
 * 
 */
@Service
public class MyBatiesServiceDefaultImpl implements MyBatiesService, ApplicationListener<ContextRefreshedEvent> {
	private static final Logger LOG = LoggerFactory.getLogger(GenericUpdateDAOImpl.class);

	private SqlSessionTemplate noShardingSession;

	private SqlSessionTemplate session;
	@Autowired
	ConfigurableWebApplicationContext context;

	@Autowired(required = false)
	DataSourceFactory dataSourceFactory;

	@Autowired(required = false)
	MybatiesDataSourceFactory mybatiesDataSourceFactory;

	@Autowired(required = false)
	SqlmapLoaderFactory sqlmapLoaderFactory;

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
		LOG.info("初始化数据访问服务[MyBatiesServiceDefaultImpl]");
		initMaster();
		return "ok";
	}

	/**
	 * 将这事务管理器添加到Spring上下文中
	 * 
	 * @return
	 */
	@SuppressWarnings("resource")
	@Bean(name = "MyTransactionManager")
	public Object addTransactionManagerToSpringContext() {

		try {
			LOG.info("尝试创建事务管理器!");

			DataSource dataSource = null;
			if (dataSourceFactory != null) {
				dataSource = dataSourceFactory.make();
				LOG.info("通过[" + dataSourceFactory + "]创建数据源：" + dataSource);
			}
			if (dataSource == null) {

				//加载用户安装目录下数据库配置信息 
				DataBaseConf.Conf conf= DataBaseConf.get(); 
				if(StringUtil.isNotEmpty(conf.getUrl(),conf.getUsername(),conf.getPassword())) {
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

//			Resource testMapper = new FileSystemResource(
//					"/opt/work/myframework/src/main/java/com/unimall/myframework/test/test-Mapper.xml");

			PathMatchingResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
			Resource[] resourcesList = resolver.getResources("classpath*:tech/codingless/biz/**/*Mapper.xml");

			Resource[] xDefSqlMapper = sqlmapLoaderFactory != null ? sqlmapLoaderFactory.sqlMapperResource() : null;

			List<Resource> mergeSqlMappers = new ArrayList<>();
			mergeSqlMappers.addAll(Arrays.asList(resourcesList));
			if (xDefSqlMapper != null) {
				mergeSqlMappers.addAll(Arrays.asList(xDefSqlMapper));

			}

			/*
			 * mergeSqlMappers.forEach(sql -> { try { LOG.info("加载SQL Mapper:" +
			 * sql.getFile().getAbsolutePath()); } catch (IOException e) {
			 * LOG.error("加载sqlmapper出错", e); } });
			 */

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
			
			LOG.info("事务管理器:"+dataSourceTransactionManager);
			return dataSourceTransactionManager;
		} catch (Exception e) {
			LOG.error("创建事务", e);
		}
		return null;
	}

	public String initMaster() {
		LOG.info("初始化Master");
		try {

			String url = "";
			String username = "";
			String password = "";

			session = null;
			BasicDataSource dataSource = new BasicDataSource();
			dataSource.setDriverClassName("com.mysql.cj.jdbc.Driver");
			dataSource.setUrl(url);
			dataSource.setUsername(username);
			dataSource.setPassword(password);
			dataSource.setMaxIdle(20);
			dataSource.setMinIdle(3);
			dataSource.setMaxTotal(20);
			dataSource.setMaxWaitMillis(10);
			dataSource.setInitialSize(3);
			dataSource.setRemoveAbandonedOnBorrow(true);
			dataSource.setRemoveAbandonedTimeout(180);
			System.out.println(dataSource.getMaxTotal());

			DataSourceTransactionManager dataSourceTransactionManager = this.context.getBean(DataSourceTransactionManager.class);
			if (dataSourceTransactionManager == null) {
				LOG.warn("事务管理器没有添加到Spring中!");
				dataSourceTransactionManager = new DataSourceTransactionManager();
			}
			dataSourceTransactionManager.setDataSource(dataSource);

			LOG.warn("将数据源:" + dataSource + ",添加到事务管理器中:" + dataSourceTransactionManager);
//			Resource testMapper = new FileSystemResource(
//					"/opt/work/myframework/src/main/java/com/unimall/myframework/test/test-Mapper.xml");
			PathMatchingResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
			Resource[] resourcesList = resolver.getResources("classpath*:tech/codingless/biz/**/*Mapper.xml");
			Resource[] xDefSqlMapper = sqlmapLoaderFactory != null ? sqlmapLoaderFactory.sqlMapperResource() : null;

			List<Resource> mergeSqlMappers = new ArrayList<>();
			mergeSqlMappers.addAll(Arrays.asList(resourcesList));
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
			LOG.info("SqlSessionTemplate 初始化完成: " + session);
			return "ok";
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	@Override
	public void onApplicationEvent(ContextRefreshedEvent event) {
		LOG.info("尝试初始化数据库连接");
		if (event.getSource() == null) {
			return;
		}
		if ("mysql/master".equalsIgnoreCase(event.getSource().toString())) {
			initMaster();
		}

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
		AssertUtil.assertNotNull(noShardingSession, "NO-SHARDING-SESSION-NOT-EXIST:未分片数据源不存在");
		return noShardingSession.selectList(statement, parameter);
	}

}
