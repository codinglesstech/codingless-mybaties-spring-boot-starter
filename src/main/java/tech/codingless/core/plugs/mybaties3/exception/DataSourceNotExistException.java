package tech.codingless.core.plugs.mybaties3.exception;

public class DataSourceNotExistException extends RuntimeException {
	private static final long serialVersionUID = 1L;

	public DataSourceNotExistException(String msg) {
		super(msg);
	}
}
