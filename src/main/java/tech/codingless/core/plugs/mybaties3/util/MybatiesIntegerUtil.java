package tech.codingless.core.plugs.mybaties3.util;

public class MybatiesIntegerUtil {

	public static int get(Integer val, int defaultval) {
		return val != null ? val : defaultval;
	}

}
