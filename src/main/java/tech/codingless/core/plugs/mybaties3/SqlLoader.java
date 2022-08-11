package tech.codingless.core.plugs.mybaties3;

/**
 * SQL语句加载器
 * @author 王鸿雁
 *
 */
public interface SqlLoader { 
	/**
	 * 根据sqlId，查找SQL语句
	 * @param sqlId
	 * @return
	 */
	public String load(String namespace,String sqlId);
	/**
	 * 优先级，最小优先级越低
	 * @return
	 */
	int priority();
	String name();
}
