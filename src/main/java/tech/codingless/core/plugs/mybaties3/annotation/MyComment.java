package tech.codingless.core.plugs.mybaties3.annotation;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
public @interface MyComment {

	/** 
	 * @return 字段注释
	 */
	String value();
}
