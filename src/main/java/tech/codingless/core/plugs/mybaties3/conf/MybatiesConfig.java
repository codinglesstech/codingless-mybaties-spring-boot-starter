package tech.codingless.core.plugs.mybaties3.conf;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import tech.codingless.core.plugs.mybaties3.CommScriptGeneter;
import tech.codingless.core.plugs.mybaties3.DBInitSpringListener;
import tech.codingless.core.plugs.mybaties3.GenericQueryDAOImpl;
import tech.codingless.core.plugs.mybaties3.GenericUpdateDAOImpl;
import tech.codingless.core.plugs.mybaties3.MyBatiesServiceDefaultImpl;
import tech.codingless.core.plugs.mybaties3.MybatiesImportSelector;
import tech.codingless.core.plugs.mybaties3.TableAutoCreateServiceMysqlImpl;

 
@Configuration
public class MybatiesConfig {
 
	
	@Bean("tech.codingless.core.plugs.mybaties3.conf.DataBaseConf")
	public DataBaseConf initDataBaseConf() {
		return new DataBaseConf();
	}
	
	
	@Bean("tech.codingless.core.plugs.mybaties3.GenericQueryDAOImpl")
	public GenericQueryDAOImpl<?> initGenericQueryDAOImpl() {
		return new GenericQueryDAOImpl<>();
	}

	@Bean("tech.codingless.core.plugs.mybaties3.GenericUpdateDAOImpl")
	public GenericUpdateDAOImpl<?> initGenericUpdateDAOImpl() {
		return new GenericUpdateDAOImpl<>();
	}
	
	@Bean("tech.codingless.core.plugs.mybaties3.MybatiesImportSelector")
	public MybatiesImportSelector initMybatiesImportSelector() {
		return new MybatiesImportSelector();
	}

	@Bean("tech.codingless.core.plugs.mybaties3.DBInitSpringListener")
	public DBInitSpringListener initDBInitSpringListener() {
		return new DBInitSpringListener();
	}

	@Bean("tech.codingless.core.plugs.mybaties3.TableAutoCreateServiceMysqlImpl")
	public TableAutoCreateServiceMysqlImpl initTableAutoCreateServiceMysqlImpl() {
		return new TableAutoCreateServiceMysqlImpl();
	}

	@Bean("tech.codingless.core.plugs.mybaties3.CommScriptGeneter")
	public CommScriptGeneter initCommScriptGeneter() {
		return new CommScriptGeneter();
	}

	@Bean("tech.codingless.core.plugs.mybaties3.MyBatiesServiceDefaultImpl")
	public MyBatiesServiceDefaultImpl initMyBatiesServiceDefaultImpl() {
		return new MyBatiesServiceDefaultImpl();
	}

	@Autowired
	private MyBatiesServiceDefaultImpl mybatiesService;
	 

	@Bean("MyTransactionManager")
	public Object initDataSourceTransactionManager() {
		return mybatiesService.initSessionAndTransaction(); 
	}




}
