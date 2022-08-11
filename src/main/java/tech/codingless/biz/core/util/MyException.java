package tech.codingless.biz.core.util;

public class MyException extends RuntimeException {
	private static final long serialVersionUID = 1L;

	public MyException(String code) {
		super(code);
	}

}
