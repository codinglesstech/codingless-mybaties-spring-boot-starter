package tech.codingless.core.plugs.mybaties3.util;

public class DataSessionEnv {
	 

	/**
	 * 清理线程变量
	 */
	public static void clean() {  
		CURRENT_COMPANY_ID.remove(); 
		CURRENT_ENV.remove();
		CURRENT_USER_ID.get();
	}

	 
	/**
	 * 当前登录的用户ID
	 */
	public static ThreadLocal<String> CURRENT_USER_ID = new ThreadLocal<String>(); 
	public static ThreadLocal<String> CURRENT_COMPANY_ID = new ThreadLocal<String>();
	/**
	 * 1:代表生产环境，2：测试环境
	 */
	public static ThreadLocal<Integer> CURRENT_ENV = new ThreadLocal<Integer>();  

 

}
