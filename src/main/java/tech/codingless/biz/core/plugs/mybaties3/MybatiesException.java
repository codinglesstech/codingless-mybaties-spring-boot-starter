package tech.codingless.biz.core.plugs.mybaties3;

public class MybatiesException extends RuntimeException {
	private static final long serialVersionUID = 1L;

	public MybatiesException(String code) {
		super(code);
	}

}
