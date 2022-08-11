package tech.codingless.biz.core.plugs.mybaties3;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
public @interface MyComment {

	/**
	 * 注释
	 * 
	 * @return
	 */
	String value();
}
