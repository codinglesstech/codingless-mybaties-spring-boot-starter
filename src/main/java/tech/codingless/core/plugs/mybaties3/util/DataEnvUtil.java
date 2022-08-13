package tech.codingless.core.plugs.mybaties3.util;

public class DataEnvUtil {
	/**
	 * 
	 * 
	 * @return 1:生产环境，2：测试环境
	 */
	public static int getEvn() { 
		return DataSessionEnv.CURRENT_ENV.get()!=null?DataSessionEnv.CURRENT_ENV.get():1;
	}

	public static boolean isProductEnv() {
		return DataSessionEnv.CURRENT_ENV.get() == null || DataSessionEnv.CURRENT_ENV.get() == 1;
	}

	public static boolean isTestEnv() {
		return DataSessionEnv.CURRENT_ENV.get() != null && DataSessionEnv.CURRENT_ENV.get() == 2;
	}

	/**
	 * 激活当前线程为生产环境
	 */
	public static void enableProductEnv() {
		DataSessionEnv.CURRENT_ENV.set(1);
	}
	
	/**
	 * 激活当前线程为测试环境
	 */
	public static void enableTestEnv() {
		DataSessionEnv.CURRENT_ENV.set(2);
	}
}
