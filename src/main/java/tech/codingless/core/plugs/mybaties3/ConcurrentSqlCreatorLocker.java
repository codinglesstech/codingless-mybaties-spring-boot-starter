package tech.codingless.core.plugs.mybaties3;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import lombok.extern.slf4j.Slf4j;
import tech.codingless.core.plugs.mybaties3.data.DataEnvProperties;

/**
 * 为了保证创建SQL语句的线程安全
 * 
 * @author 王鸿雁
 *
 */
@Slf4j
public class ConcurrentSqlCreatorLocker {
	protected static final ConcurrentHashMap<String, Boolean> SQL_GEN_SUCCESS = new ConcurrentHashMap<>();
	protected static final ConcurrentHashMap<String, Object> LOCKER_MAP = new ConcurrentHashMap<>();
	private static final Object LOCKER = new Object();

	public static int clearDatabaseLocker(String databaseId) {

		List<String> keys = new ArrayList<>();
		String prefix = databaseId + ".";

		Iterator<String> it = SQL_GEN_SUCCESS.keys().asIterator();
		while (it.hasNext()) {
			String key = it.next();
			if (key.startsWith(prefix)) {
				keys.add(key);
			}
		}

		keys.forEach(key -> {
			SQL_GEN_SUCCESS.remove(key);
		});
		log.info("Removed Sql Locker:{}, databaseId:{}", keys.size(), databaseId);
		return keys.size();
	}

	public static boolean isTheSqlExist(String key) {
		key = DataEnvProperties.getDataSource() + "." + key;
		return SQL_GEN_SUCCESS.containsKey(key);
	}

	public static boolean notExist(String key) {
		key = DataEnvProperties.getDataSource() + "." + key;
		return !SQL_GEN_SUCCESS.containsKey(key);
	}

	public static void put(String key) {
		key = DataEnvProperties.getDataSource() + "." + key;
		SQL_GEN_SUCCESS.put(key, true);
	}

	/**
	 * 获得一把锁
	 * 
	 * @param key the locker
	 * @return locked object
	 */
	public static Object getLocker(String key) {
		key = DataEnvProperties.getDataSource() + "." + key;
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
		key = DataEnvProperties.getDataSource() + "." + key;
		SQL_GEN_SUCCESS.remove(key);

	}

}
