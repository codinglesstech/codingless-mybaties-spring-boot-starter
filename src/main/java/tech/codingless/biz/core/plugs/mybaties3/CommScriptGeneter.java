package tech.codingless.biz.core.plugs.mybaties3;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import tech.codingless.biz.core.plugs.mybaties3.data.BaseDO;

@Component
public class CommScriptGeneter {

	private List<BaseDO> tables;

	@SuppressWarnings("rawtypes")
	@Autowired
	private GenericUpdateDAOImpl updateScriptGen; 
 
	public void doGenCommScript() {
		if (tables == null || tables.isEmpty()) {
			return;
		}
		for (BaseDO table : tables) {
			updateScriptGen.genAutoSqlForCreate(table);
			updateScriptGen.genAutoSqlForUpdate(table);
			 
			 
		}
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	public void setDOList(Collection<BaseDO> list) {
		tables = new ArrayList();
		this.tables.addAll(list);

	}

}
