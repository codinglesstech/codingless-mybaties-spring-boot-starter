package tech.codingless.biz.core.util;

import java.util.HashMap;
import java.util.Map;

public class SessionUtil {
	/**
	 * access_token --> uid 的前缀
	 */
	public static final String CACHE_KEY_ACCESS_TOKEN_UID_PRE = "ACCESS_TOKEN:UID:";
	/**
	 * uid --> access_token 的前缀
	 */
	public static final String CACHE_KEY_UID_ACCESS_TOKEN_PRE = "UID:ACCESS_TOKEN:";
	private static final String MAP_KEY_CURRENT_COMPNAY_ID = "CURRENT_COMPNAY_ID";
	private static final String MAP_KEY_CURRENT_USER_ID = "CURRENT_USER_ID";

	/**
	 * 清理线程变量
	 */
	public static void clean() {
		RID.remove();
		CURRENT_PLATFORM.remove();
		CURRENT_ACCESS_TOKEN.remove();
		CACHE_KEY.remove();
		CACHE_RESPONSE.remove();
		CACHE_TIME.remove();
		CURRENT_COMPANY_ID.remove();
		CURRENT_RESPONSE.remove(); 
	}

	/**
	 * 请求ID
	 */
	public static ThreadLocal<String> RID = new ThreadLocal<String>();
	/**
	 * 当前平台
	 */
	public static ThreadLocal<String> CURRENT_PLATFORM = new ThreadLocal<String>();
	/**
	 * 当前登录的用户ID
	 */
	public static ThreadLocal<String> CURRENT_USER_ID = new ThreadLocal<String>();
	public static ThreadLocal<String> CURRENT_USER_NAME = new ThreadLocal<String>();
	public static ThreadLocal<String> CURRENT_COMPANY_ID = new ThreadLocal<String>();
	/**
	 * 1:代表生产环境，2：测试环境
	 */
	public static ThreadLocal<Integer> CURRENT_ENV = new ThreadLocal<Integer>();
	/**
	 * 当前授权TOKEN
	 */
	public static ThreadLocal<String> CURRENT_ACCESS_TOKEN = new ThreadLocal<String>();
	/**
	 * cache key
	 */
	public static ThreadLocal<String> CACHE_KEY = new ThreadLocal<String>();
	public static ThreadLocal<String> CACHE_RESPONSE = new ThreadLocal<String>();
	public static ThreadLocal<String> CACHE_READ = new ThreadLocal<String>();
	public static ThreadLocal<Integer> CACHE_TIME = new ThreadLocal<Integer>();
	/**
	 * 当前请求的响应
	 */
	public static ThreadLocal<String> CURRENT_RESPONSE = new ThreadLocal<String>();

	/**
	 * 创建一个指定容量的Map,并且将 CURRENT_COMPNAY_ID, CURRENT_USER_ID 放入其中,一般用户Mybaties的参数
	 * 
	 * @param size
	 * @return
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public static Map<String, Object> newSessionMap(int size) {
		Map map = new HashMap((int) ((size + 2) / 0.75 + 1));
		map.put(MAP_KEY_CURRENT_COMPNAY_ID, CURRENT_COMPANY_ID.get());
		map.put(MAP_KEY_CURRENT_USER_ID, CURRENT_USER_ID.get());
		return map;
	}

}
