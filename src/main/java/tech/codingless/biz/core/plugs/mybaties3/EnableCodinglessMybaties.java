package tech.codingless.biz.core.plugs.mybaties3;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.springframework.context.annotation.Import;

import tech.codingless.biz.core.plugs.mybaties3.conf.DataBaseConf;

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
	CommScriptGeneter.class,
	GenericUpdateDAOImpl.class,
	MyBatiesServiceDefaultImpl.class,
	DataBaseConf.class}) 
public @interface EnableCodinglessMybaties {

}
