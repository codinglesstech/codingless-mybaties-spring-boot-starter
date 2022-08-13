package tech.codingless.core.plugs.mybaties3.annotation;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import org.apache.ibatis.type.StringTypeHandler;

@Retention(RetentionPolicy.RUNTIME)
public @interface MyColumn {

	/**
	 * 字段类型
	 * 
	 * @return
	 */
	String type() default "";

	/**
	 * 字段名
	 * 
	 * @return
	 */
	String name() default "";

	/**
	 * 虚拟字段，不会在数据库中创建
	 * 
	 * @return
	 */
	boolean virtual() default false;

	/**
	 * 默认值
	 * 
	 * @return
	 */
	String defaultValue() default "";

	/**
	 * 主键
	 * 
	 * @return
	 */
	boolean key() default false;

	/**
	 * 是否自增
	 * 
	 * <pre>
	 * alter table xxx_table_name  modify xxx_column_name int(11) not null auto_increment, add   key(xxx_column_name);
	 * </pre>
	 *  
	 * @return
	 *
	 */
	boolean autoIncrement() default false;
	/**
	 * 是否创建索引
	 * ALTER TABLE `table_name` ADD INDEX index_name ( `column` ) 
	 * @return
	 *
	 */
	boolean createIndex() default false;
	
	/**
	 * 充许自定义Handler 
	 * @return
	 *
	 */
	Class<?> typeHandler() default StringTypeHandler.class;
	
	/**
	 * 只读，除首次可写入外，其余更新均会忽略
	 * @return
	 */
	boolean readonly() default false;

}
