package tech.codingless.biz.core.plugs.mybaties3.condition;

import java.util.ArrayList;
import java.util.List;

import tech.codingless.biz.core.plugs.mybaties3.BaseDO;
import tech.codingless.biz.core.reflect.SerializableFunction;

public class ColumnSelector<T extends BaseDO> {

	List<SerializableFunction<T, Object>> columns = new ArrayList<>(); 
	
	public List<SerializableFunction<T, Object>> getColumns() {
		return columns;
	}
	/** 
	 * @param column
	 * @return
	 */
	public ColumnSelector<T> include(SerializableFunction<T, Object> column) {  
		this.columns.add(column);
		return this;
	}
	
	/**
	 * 包括ID,及VER版本
	 * @return
	 */
	public ColumnSelector<T> includeId() {  
		this.include(BaseDO::getId);
		this.include(BaseDO::getVer);
		return this;
	}
	
}
