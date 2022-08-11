package tech.codingless.biz.core.util;

import com.alibaba.fastjson.JSON;

/**
 * 目前只是简单的使用 FAST JSON,日后如果需要换，就直接内部换掉
 * 
 * @author 王鸿雁
 *
 */
public class JsonUtil {

	public static String toJSONString(Object obj) {
		return JSON.toJSONString(obj);
	}

	public static <T> T parseObject(String text, Class<T> clazz) {
		return JSON.parseObject(text, clazz);
	}

}
