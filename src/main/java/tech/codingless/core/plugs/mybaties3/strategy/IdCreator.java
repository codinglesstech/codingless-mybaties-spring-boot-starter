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
	 * @param data 即将保存的数据
	 * @return 主键ID 
	 */
	String generateId(BaseDO data);

}
