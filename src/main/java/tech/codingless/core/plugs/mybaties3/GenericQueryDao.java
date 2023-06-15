package tech.codingless.core.plugs.mybaties3;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import tech.codingless.core.plugs.mybaties3.annotation.OrderTypeEnum;
import tech.codingless.core.plugs.mybaties3.condition.ColumnSelector;
import tech.codingless.core.plugs.mybaties3.condition.QueryCondition;
import tech.codingless.core.plugs.mybaties3.condition.QueryConditionWrapper;
import tech.codingless.core.plugs.mybaties3.data.BaseDO;
import tech.codingless.core.plugs.mybaties3.data.PageRollResult;

public interface GenericQueryDao<T extends BaseDO> {

	Object selectOneRow(String sqlId, Object param);

	T selectOne(String sqlId, Object param);

	List<T> selectList(String sqlId, Object param);

	@Deprecated
	T getEntity(Class<T> clazz, String id);

	T getEntity(Class<T> clazz, String id, String companyId);

	List<T> list(Class<T> clazz);

	List<T> list(Class<T> clazz, String companyId);

	List<T> findByExample(Class<T> clazz, ColumnSelector<T> columns, T example, String orderColumn, OrderTypeEnum orderType, Integer size, Integer offset);

	PageRollResult<?> rollPage(String namespace, String sqlId, Map<String, Object> param, Integer size, Integer page);

	List<T> findEntityList(Class<T> clazz, String companyId, Collection<String> idList);

	List<T> findEntityList(Class<T> clazz, String companyId, Collection<String> idList, Collection<String> columns);

	Map<String, Object> selectOneNative(String prepareSql, List<Object> param);

	List<T> select(Class<? extends BaseDO> clazz, Collection<String> columns, Collection<QueryCondition> conditions, int offset, int limit);

	int count(Class<? extends BaseDO> clazz, Collection<String> columns, Collection<QueryCondition> conditions);

	List<Map<String, ?>> select(String selectId, Map<String, Object> param, int offset, int limit);

	List<T> select(Class<T> entityClass, ColumnSelector<T> columns, QueryConditionWrapper<T> wrapper, SerializableFunction<T, Object> sortColumn, OrderTypeEnum orderType, int offset, int limit);

	long count(Class<T> entityClass, QueryConditionWrapper<T> wrapper);

	T getEntityV2(Class<T> clazz, String id, String companyId);
	List<T> listV2(Class<T> clazz, Collection<String> idList, String companyId);

}
