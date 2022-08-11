package tech.codingless.biz.core.plugs.mybaties3;

import java.util.concurrent.ConcurrentHashMap;
/**
 * 为了保证创建SQL语句的线程安全
 * @author 王鸿雁
 *
 */
public class ConcurrentSqlCreatorLocker {
	protected static final ConcurrentHashMap<String, Boolean> SQL_GEN_SUCCESS = new ConcurrentHashMap<>();
	protected static final ConcurrentHashMap<String, Object> LOCKER_MAP = new ConcurrentHashMap<>();
	private static final Object LOCKER = new Object();

	 
	/**
	 * 这个SQL是否存在
	 * 
	 * @param key
	 * @return
	 */
	public static boolean isTheSqlExist(String key) {
		return SQL_GEN_SUCCESS.containsKey(key);
	}
	
	public static boolean notExist(String key) {
		return !SQL_GEN_SUCCESS.containsKey(key);
	}
	 
	public static void put(String key) {
		SQL_GEN_SUCCESS.put(key, true);
	}

	/**
	 * 获得一把锁
	 * 
	 * @param key
	 * @return
	 */
	public static Object getLocker(String key) { 
		if (LOCKER_MAP.containsKey(key)) {
			return LOCKER_MAP.get(key);
		}
		synchronized (LOCKER) {
			if (LOCKER_MAP.containsKey(key)) {
				return LOCKER_MAP.get(key);
			}
			LOCKER_MAP.put(key, new Object());

		}
		return LOCKER_MAP.get(key);
	 

	}

	public static void remove(String key) {
		SQL_GEN_SUCCESS.remove(key);
		
	}

}
