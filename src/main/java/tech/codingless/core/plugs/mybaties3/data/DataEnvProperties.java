package tech.codingless.core.plugs.mybaties3.data;

import tech.codingless.core.plugs.mybaties3.enums.DataEnvEnums;

/**
 * 数据环境
 * 
 * @author ASUS
 *
 */
public class DataEnvProperties {

	private static ThreadLocal<String> COMPANY_ID = new ThreadLocal<>();
	private static ThreadLocal<String> OWNER_ID = new ThreadLocal<>();
	private static ThreadLocal<String> OPT_USER_ID = new ThreadLocal<>();
	private static ThreadLocal<String> OPT_USER_NAME = new ThreadLocal<>();
	private static ThreadLocal<DataEnvEnums> DATA_ENV = new ThreadLocal<>();
	private static ThreadLocal<String> GROUP_ID = new ThreadLocal<>();
	private static ThreadLocal<Integer> DATA_LEVEL = new ThreadLocal<>();
	private static ThreadLocal<Boolean> IS_DEL = new ThreadLocal<>();

	private static ThreadLocal<String> DATA_SOURCE = new ThreadLocal<>();// 数据源
	private static ThreadLocal<String> DATA_BASE = new ThreadLocal<>();// 数据库
	private static String defaultDataSourceId = "default";
	private static String defaultDataBaseName = "unibiz";

	public static String getDefaultDataSourceId() {
		return defaultDataSourceId;
	}

	public static void setDefaultDataBaseName(String defaultDataBaseName) {
		DataEnvProperties.defaultDataBaseName = defaultDataBaseName;
	}

	public static String getDefaultDataBaseName() {
		return defaultDataBaseName;
	}

	/**
	 * 设置公司ID
	 * 
	 * @param companyId
	 */
	public static void setCompanyId(String companyId) {
		COMPANY_ID.set(companyId);
	}

	public static String getCompanyId() {
		return COMPANY_ID.get();
	}

	/**
	 * 设置所有者ID
	 * 
	 * @param ownerId
	 */
	public static void setOwnerId(String ownerId) {
		OWNER_ID.set(ownerId);

	}

	public static String getOwnerId() {
		return OWNER_ID.get();
	}

	/**
	 * 操作用户
	 * 
	 * @param optUserId
	 */
	public static void setOptUserId(String optUserId) {
		OPT_USER_ID.set(optUserId);

	}

	public static String getOptUserId() {
		return OPT_USER_ID.get();
	}

	public static void setOptUserName(String optUserName) {
		OPT_USER_NAME.set(optUserName);

	}

	/**
	 * 设置数据环境
	 * 
	 * @param env
	 */
	public static void setEnv(DataEnvEnums env) {
		DATA_ENV.set(env);
	}

	public static int getEnv() {
		DataEnvEnums env = DATA_ENV.get();
		return env == null ? DataEnvEnums.PRODUCT_EVN.getType() : env.getType();
	}

	/**
	 * 设置组ID
	 * 
	 * @param groupId
	 */
	public static void setGroupId(String groupId) {
		GROUP_ID.set(groupId);

	}

	public static String getGroupId() {
		return GROUP_ID.get();
	}

	/**
	 * 设置数据级别
	 * 
	 * @param dataLevel
	 */
	public static void setDataLevel(int dataLevel) {
		DATA_LEVEL.set(dataLevel);
	}

	public static int getDataLevel() {
		Integer level = DATA_LEVEL.get();
		return level == null ? 1 : level;
	}

	/**
	 * 设置是否删除
	 * 
	 * @param del
	 */
	public static void setDel(boolean del) {
		IS_DEL.set(del);
	}

	/**
	 * 清理数据环境
	 */
	public static void clear() {
		COMPANY_ID.remove();
		OWNER_ID.remove();
		OPT_USER_ID.remove();
		OPT_USER_NAME.remove();
		DATA_ENV.remove();
		GROUP_ID.remove();
		DATA_LEVEL.remove();
		IS_DEL.remove();
		DATA_SOURCE.remove();
		DATA_BASE.remove();
	}

	/**
	 * 设置数据源
	 * 
	 * @param dataSource
	 */
	public static void setDataSource(String dataSource) {
		DATA_SOURCE.set(dataSource);
	}

	public static String getDataSource() {
		return DATA_SOURCE.get();
	}

	/**
	 * 设置数据库
	 * 
	 * @param database
	 */
	public static void setDatabase(String database) {
		DATA_BASE.set(database);
	}

	public static String getDatabase() {
		return DATA_BASE.get();
	}
}
