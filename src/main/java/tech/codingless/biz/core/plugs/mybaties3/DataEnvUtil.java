package tech.codingless.biz.core.plugs.mybaties3;

import tech.codingless.biz.core.util.SessionUtil;

public class DataEnvUtil {
	/**
	 * 1:生产环境，2：测试环境
	 * 
	 * @return
	 */
	public static int getEvn() { 
		return SessionUtil.CURRENT_ENV.get()!=null?SessionUtil.CURRENT_ENV.get():1;
	}

	public static boolean isProductEnv() {
		return SessionUtil.CURRENT_ENV.get() == null || SessionUtil.CURRENT_ENV.get() == 1;
	}

	public static boolean isTestEnv() {
		return SessionUtil.CURRENT_ENV.get() != null && SessionUtil.CURRENT_ENV.get() == 2;
	}

	/**
	 * 激活当前线程为生产环境
	 */
	public static void enableProductEnv() {
		SessionUtil.CURRENT_ENV.set(1);
	}
	
	/**
	 * 激活当前线程为测试环境
	 */
	public static void enableTestEnv() {
		SessionUtil.CURRENT_ENV.set(2);
	}
}
