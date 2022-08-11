package tech.codingless.core.plugs.mybaties3.annotation;

public enum OrderTypeEnum {

	DESC("DESC", "DESC"), ASC("ASC", "ASC"),;

	private String code;
	private String name;

	private OrderTypeEnum(String code, String name) {
		this.code = code;
		this.name = name;
	}

	public String getCode() {
		return code;
	}

	public String getName() {
		return name;
	}
}
