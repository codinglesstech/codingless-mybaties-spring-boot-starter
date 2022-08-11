package tech.codingless.biz.core.plugs.mybaties3.util;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

import tech.codingless.biz.core.plugs.mybaties3.BaseDO;

public class BeanUtil {
	private static final String GET = "get";
	private static final String IS = "is";
	private static final Map<String, String> NOT_MATCH_METHOD = new HashMap<String, String>();
	static {
		NOT_MATCH_METHOD.put("getGmtCreate", "getGmtCreate");
		NOT_MATCH_METHOD.put("getGmtWrite", "getGmtWrite");
		NOT_MATCH_METHOD.put("getClass", "getClass");
		NOT_MATCH_METHOD.put("getId", "getId");
	}

	/**
	 * 比较两个实体类一般值是否一致
	 * 
	 * @param skuDO1
	 * @param skuDO2
	 * @return
	 */
	public static boolean compare(BaseDO skuDO1, BaseDO skuDO2) {
		if (skuDO1 == null || skuDO2 == null) {
			return false;
		}
		try {
			Object val1, val2;
			for (Method method : skuDO1.getClass().getMethods()) {
				if (!method.getName().startsWith(GET) && !method.getName().startsWith(IS)) {
					continue;
				}
				if (NOT_MATCH_METHOD.containsKey(method.getName())) {
					continue;
				}
				System.out.println(method.getName());
				val1 = method.invoke(skuDO1);
				val2 = method.invoke(skuDO2);
				if (val1 == val2) {
					continue;
				}
				if (val1 == null && val2 == null) {
					continue;
				}
				if (val1 == null || val2 == null) {
					return false;
				}
				if (!val1.equals(val2)) {
					return false;
				}
			}
			return true;
		} catch (Exception e) {
			return false;
		}
	}

}
