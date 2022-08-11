package tech.codingless.biz.core.plugs.mybaties3;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.dbcp2.BasicDataSource;
import org.apache.ibatis.session.SqlSessionFactory;
import org.mybatis.spring.SqlSessionFactoryBean;
import org.mybatis.spring.SqlSessionTemplate;
import org.mybatis.spring.transaction.SpringManagedTransactionFactory;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;

public class MyBatiesTest {

	@SuppressWarnings({ "unchecked", "rawtypes" })
	public static void main(String[] args) throws Exception {

		BasicDataSource dataSource = new BasicDataSource();
		dataSource.setDriverClassName("com.mysql.jdbc.Driver");
		dataSource.setUrl("jdbc:mysql://47.92.194.114:3306/test?useUnicode=true&useSSL=false&characterEncoding=utf-8&allowMultiQueries=true");
		dataSource.setUsername("root");
		dataSource.setPassword("glvdYpdOv3XeuU(b=2bKg102aGO");
		dataSource.setMaxIdle(20);
		dataSource.setMinIdle(3);
		dataSource.setMaxTotal(20);
		dataSource.setMaxWaitMillis(10);
		dataSource.setInitialSize(3);
		dataSource.setRemoveAbandonedOnBorrow(true);
		dataSource.setRemoveAbandonedTimeout(180);
		System.out.println(dataSource.getMaxTotal());

		DataSourceTransactionManager dataSourceTransactionManager = new DataSourceTransactionManager();
		dataSourceTransactionManager.setDataSource(dataSource);

		Resource testMapper = new FileSystemResource("/opt/work/myframework/src/main/java/com/unimall/myframework/test/test-Mapper.xml");

		SqlSessionFactoryBean sqlSessionFactoryBean = new SqlSessionFactoryBean();
		sqlSessionFactoryBean.setDataSource(dataSource);
		sqlSessionFactoryBean.setMapperLocations(new Resource[] { testMapper });
		sqlSessionFactoryBean.setTransactionFactory(new SpringManagedTransactionFactory());

		FactoryBean<SqlSessionFactory> factoryBean = sqlSessionFactoryBean;
		SqlSessionTemplate session = new SqlSessionTemplate(factoryBean.getObject());
		Map<String, String> param = new HashMap();
		param.put("id", "1");
		param.put("v", "v1");
		int effect = session.insert("TEST.insert1", param);

		System.out.println("effect:" + effect);

	 

	}

}
