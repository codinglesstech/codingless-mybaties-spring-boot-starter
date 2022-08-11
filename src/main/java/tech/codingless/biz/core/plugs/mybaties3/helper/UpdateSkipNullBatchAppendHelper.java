package tech.codingless.biz.core.plugs.mybaties3.helper;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

import org.mybatis.spring.MyBatisSystemException;
import org.springframework.util.CollectionUtils;

import tech.codingless.biz.core.plugs.mybaties3.BaseDO;
import tech.codingless.biz.core.plugs.mybaties3.CommonSQLHelper;
import tech.codingless.biz.core.plugs.mybaties3.MyBatiesService;
import tech.codingless.biz.core.plugs.mybaties3.data.UpdateObject;

/**
 * 
 * 批量更新
 * 
 * @author 王鸿雁
 * @version 2021年10月23日
 */
public class UpdateSkipNullBatchAppendHelper {

	private static final ConcurrentHashMap<Class<?>, ConcurrentLinkedQueue<UpdateObject>> CACHE = new ConcurrentHashMap<>();

	public static int updateSkipNullBatchAppend(MyBatiesService myBatiesService, String companyId, BaseDO data, Long ver, int batchSize) {

		// 来了一个对象，先保存到缓存中
		UpdateObject updateObject = new UpdateObject();
		updateObject.setUpdateDO(data);
		updateObject.setVer(ver);
		updateObject.setCompanyId(companyId);
		updateObject.setId(data.getId());

		Class<?> clazz = data.getClass();
		if (!CACHE.containsKey(clazz)) {
			CACHE.put(clazz, new ConcurrentLinkedQueue<>());
		}

		ConcurrentLinkedQueue<UpdateObject> queue = CACHE.get(clazz);

		queue.add(updateObject);
		if (batchSize > 0 && queue.size() < batchSize) {
			return 0;
		}

		return updateSkipNullBatchExecute(myBatiesService, clazz);

	}

	public static int updateSkipNullBatchExecute(MyBatiesService myBatiesService, Class<?> clazz) {
		if (!CACHE.containsKey(clazz)) {
			return 0;
		}
		ConcurrentLinkedQueue<UpdateObject> queue = CACHE.get(clazz);
		if (queue.isEmpty()) {
			return 0;
		}
		// 超出批量大小时执行保存动作
		List<UpdateObject> list = new ArrayList<>();
		while (!queue.isEmpty()) {
			UpdateObject obj = queue.poll();
			if (obj != null) {
				list.add(obj);
			}
		}
		if (CollectionUtils.isEmpty(list)) {
			return 0;
		} 
		return updateSkipNullBatchExecute(myBatiesService,list); 
	}

	public static int updateSkipNullBatchExecute(MyBatiesService myBatiesService, List<UpdateObject> updateList) {
		if (CollectionUtils.isEmpty(updateList)) {
			return 0;
		}

		Class<?> clazz = updateList.get(0).getUpdateDO().getClass();
		String sqlKey = "AUTOSQL.UPDATE_BATCH_" + CommonSQLHelper.getTableName(clazz);
		try {
			return myBatiesService.update(sqlKey, updateList);
		} catch (MyBatisSystemException e) {
			AutoUpdateBatchHelper.genBatchUpdateSql(myBatiesService.getConfiguration(), "AUTOSQL", sqlKey, clazz);
			return myBatiesService.update(sqlKey, updateList);
		}
	}

}
