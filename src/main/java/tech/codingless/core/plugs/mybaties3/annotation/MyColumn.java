package tech.codingless.core.plugs.mybaties3.annotation;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import org.apache.ibatis.type.StringTypeHandler;

@Retention(RetentionPolicy.RUNTIME)
public @interface MyColumn {

	/**
	 * 字段类型
	 * 
	 * @return 可以是数据库支持的各位类型，比如: varchar(100),Integer...
	 */
	String type() default "";

	/** 
	 * @return 字段名
	 */
	String name() default "";

	/**
	 *  
	 * @return 是否虚拟字段，不会在数据库中创建
	 */
	boolean virtual() default false;

	/**
	 * 
	 * 
	 * @return 默认值
	 */
	String defaultValue() default "";

	/**
	 * 
	 * 
	 * @return 主键
	 */
	boolean key() default false;

	/**
	 * 
	 * 
	 * <pre>
	 * alter table xxx_table_name  modify xxx_column_name int(11) not null auto_increment, add   key(xxx_column_name);
	 * </pre>
	 *  
	 * @return  是否自增
	 *
	 */
	boolean autoIncrement() default false;
	/**
	 * <pre>
	 * ALTER TABLE `table_name` ADD INDEX index_name ( `column` ) 
	 * </pre>
	 * @return 是否创建索引
	 *
	 */
	boolean createIndex() default false;
	
	/**
	 * 
	 * @return 充许自定义Handler 
	 *
	 */
	Class<?> typeHandler() default StringTypeHandler.class;
	
	/** 
	 * @return 只读，除首次可写入外，其余更新均会忽略
	 */
	boolean readonly() default false;

}
