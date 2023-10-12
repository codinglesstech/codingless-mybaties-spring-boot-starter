package tech.codingless.core.plugs.mybaties3;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import tech.codingless.core.plugs.mybaties3.data.BaseDO;
import tech.codingless.core.plugs.mybaties3.data.UpdateObject;

/**
 * 
 * 
 * 更新接口
 * 
 * @author 王鸿雁
 * @version 2021年10月19日
 */
public interface GenericUpdateDao<T> {

	int update(String sqlId, Object param);

	int insert(String sqlId, Object param);

	int delete(String sqlId, Object param);

	int createEntity(Object entity);

	int createEntityList(List<T> entityList);

	int deleteEntity(Class<T> clazz, String entityId);

	int deleteEntityWithCompanyId(Class<T> clazz, String id, String companyId);

	int updateEntity(BaseDO entiry);

	int updateEntityWithCompanyId(BaseDO entiry, String companyId);

	int updateNotNull(T data, Long ver);

	int updateSkipNullBatchAppend(String companyId, T data, Long ver, int batchSize);

	int updateSkipNullBatchExecute(Class<T> clazz);

	int deleteLogicalWithCompanyId(Class<T> clazz, String id, String companyId);

	int deleteLogicalWithCompanyId(Class<T> clazz, Collection<String> idList, String companyId);

	int insertNative(String prepareSql, List<Object> params);

	int updateNative(String prepareSql, List<Object> params);

	int updateSkipNullBatchExecute(List<UpdateObject> updateList);

	int upinsert(List<T> entityList);

	long execinsert(String xmlInsertSql, Map<String, Object> param);

	long execupdate(String xmlUpdateSql, Map<String, Object> param);

	long execdelete(String xmlDeleteSql, Map<String, Object> param);

}
