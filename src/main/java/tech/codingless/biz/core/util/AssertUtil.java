package tech.codingless.biz.core.util;

import java.util.Collection;

import org.springframework.util.CollectionUtils;

public class AssertUtil {

	public static void assertTrue(boolean bool) {
		if (!bool) {
			throw new MyException("期待true 但是实际为false");
		}
	}

	public static void assertFail(String code) {
		throw new MyException(code);
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
		if (StringUtil.isEmpty(obj)) {
			assertFail(code);
		}

	}

	public static void assertTrue(boolean bool, String code) {
		if (!bool) {
			throw new MyException(code);
		}
	}

	public static void assertFalse(boolean bool, String code) {
		if (bool) {
			throw new MyException(code);
		}

	}

	public static void assertTrue(boolean bool, RuntimeException e) {
		if (!bool) {
			throw e;
		}

	}

	public static void assertNotEmpty(Collection collection, String code) {
		if (CollectionUtils.isEmpty(collection)) {
			throw new MyException(code);
		}
	}

}
