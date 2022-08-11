package tech.codingless.core.plugs.mybaties3.enums;

public enum DataEnvEnums {
	PRODUCT_EVN(1),//生产环境
	TEST_ENV(2),//测试环境  
	;

	private int type;

	private DataEnvEnums(int type) {
		this.type = type;
	}

	public int getType() {
		return type;
	}
}
