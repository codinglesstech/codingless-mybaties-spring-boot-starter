package tech.codingless.core.plugs.mybaties3.strategy;

import tech.codingless.core.plugs.mybaties3.data.BaseDO;

/**
 * ID创建器
 * 
 * @author WangHongYan
 *
 */
public interface IdCreator {
	
	/**
	 * 生成一个新的主键ID
	 * @param clazz
	 * @param data
	 * @return
	 */
	String generateId(BaseDO data);

}
