package tech.codingless.core.plugs.mybaties3.helper;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.mybatis.spring.MyBatisSystemException;

import tech.codingless.core.plugs.mybaties3.MyBatiesService;

public class AutoDeleteBatchHelper {

	public static int deleteLogical(MyBatiesService myBatiesService, Class<?> clazz, Collection<String> idList, String companyId) {

		String sqlKey = "AUTOSQL.DELETE_LOGICAL_BATCH_" + CommonSQLHelper.getTableName(clazz);
		Map<String, Object> param = new HashMap<>(6);
		param.put("idList", idList);
		param.put("companyId", companyId);
		try {

			return myBatiesService.update(sqlKey, param);
		} catch (MyBatisSystemException e) {
			AutoDeleteLogicalBatchHelper.genSql(myBatiesService.getConfiguration(), "AUTOSQL", sqlKey, clazz);
			return myBatiesService.update(sqlKey, param);
		}
	}

}
