package tech.codingless.core.plugs.mybaties3.util;

import tech.codingless.core.plugs.mybaties3.MybatiesException;

public class MybatiesAssertUtil {

	public static void assertTrue(boolean bool) {
		if (!bool) {
			throw new MybatiesException("期待true 但是实际为false");
		}
	}

	public static void assertFail(String code) {
		throw new MybatiesException(code);
	}

	public static void assertNotNull(Object obj) {
		if (obj == null) {
			assertFail("obj为空!");
		}

	}

	public static void assertNotNull(Object obj, String code) {
		if (obj == null) {
			assertFail(code);
		}

	}

	public static void assertNotEmpty(String obj, String code) {
		if (MybatiesStringUtil.isEmpty(obj)) {
			assertFail(code);
		}

	}

	public static void assertTrue(boolean bool, String code) {
		if (!bool) {
			throw new MybatiesException(code);
		}
	}

	public static void assertFalse(boolean bool, String code) {
		if (bool) {
			throw new MybatiesException(code);
		}

	}

	public static void assertTrue(boolean bool, RuntimeException e) {
		if (!bool) {
			throw e;
		}

	}

}
