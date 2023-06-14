package tech.codingless.core.plugs.mybaties3.util;

public class ObjectUtil {

	/**
	 * The First Valid Value
	 * 
	 * @param v1
	 * @param v2
	 * @return The First Valid Value
	 */
	public static String valid(String v1, String v2) {
		return MybatiesStringUtil.isNotEmpty(v1) ? v1 : v2;
	}

}
