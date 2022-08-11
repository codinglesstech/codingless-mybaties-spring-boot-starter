package tech.codingless.biz.core.plugs.mybaties3;

public interface SqlMapperService {

	/**
	 * 卸载SQL
	 * 
	 * @param sqlId
	 * @return
	 */
	boolean unload(String sqlId);

	void unloadByXml(String xml);

}
