package tech.codingless.core.plugs.mybaties3.condition;

import java.util.ArrayList;
import java.util.List;

import tech.codingless.core.plugs.mybaties3.SerializableFunction;
import tech.codingless.core.plugs.mybaties3.data.BaseDO;

public class ColumnSelector<T extends BaseDO> {

	List<SerializableFunction<T, Object>> columns = new ArrayList<>(); 
	
	public List<SerializableFunction<T, Object>> getColumns() {
		return columns;
	}
	/** 
	 * @param column 包函的字段
	 * @return 选择器本身
	 */
	public ColumnSelector<T> include(SerializableFunction<T, Object> column) {  
		this.columns.add(column);
		return this;
	}
	
	/**
	 * 包括ID,及VER版本
	 * @return 选择器本身 
	 */
	public ColumnSelector<T> includeId() {  
		this.include(BaseDO::getId);
		this.include(BaseDO::getVer);
		return this;
	}
	
}
