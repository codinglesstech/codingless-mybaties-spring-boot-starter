package tech.codingless.core.plugs.mybaties3.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.springframework.context.annotation.Import;

import tech.codingless.core.plugs.mybaties3.DBInitSpringListener;
import tech.codingless.core.plugs.mybaties3.GenericQueryDAOImpl;
import tech.codingless.core.plugs.mybaties3.GenericUpdateDAOImpl;
import tech.codingless.core.plugs.mybaties3.MyBatiesServiceDefaultImpl;
import tech.codingless.core.plugs.mybaties3.MybatiesImportSelector;
import tech.codingless.core.plugs.mybaties3.TableAutoCreateServiceMysqlImpl;
import tech.codingless.core.plugs.mybaties3.conf.DataBaseConf;

/**
 * Enable Codingless Mybaties FrameWork
 * @author WangHongYan
 *
 */ 
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Documented  
@Inherited
@Import({MybatiesImportSelector.class,
	DBInitSpringListener.class,
	TableAutoCreateServiceMysqlImpl.class, 
	GenericUpdateDAOImpl.class,
	MyBatiesServiceDefaultImpl.class,
	DataBaseConf.class,
	GenericQueryDAOImpl.class}) 
public @interface EnableCodinglessMybaties {

}
