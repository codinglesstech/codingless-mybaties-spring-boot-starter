package tech.codingless.core.plugs.mybaties3;

public interface SqlMapperService {

	/**
	 * 卸载SQL
	 * 
	 * @param sqlId sql语句ID
	 * @return 是否卸载成功
	 */
	boolean unload(String sqlId);

	void unloadByXml(String xml);

}
