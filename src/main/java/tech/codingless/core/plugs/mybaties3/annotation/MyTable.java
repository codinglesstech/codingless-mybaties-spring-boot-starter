package tech.codingless.core.plugs.mybaties3.annotation;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import org.springframework.stereotype.Component;
 

/**
 * 用以自动生成表及字段的标识
 * 
 * @author why
 *
 */   
@Component
@Retention(RetentionPolicy.RUNTIME)
public @interface MyTable {

	/** 
	 * @return 表前缀
	 */
	String prefix() default "uni";
}
