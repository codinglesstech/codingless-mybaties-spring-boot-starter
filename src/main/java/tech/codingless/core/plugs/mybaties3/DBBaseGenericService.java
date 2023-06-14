package tech.codingless.core.plugs.mybaties3;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import tech.codingless.core.plugs.mybaties3.annotation.OrderTypeEnum;
import tech.codingless.core.plugs.mybaties3.condition.ColumnSelector;
import tech.codingless.core.plugs.mybaties3.condition.QueryConditionWrapper;
import tech.codingless.core.plugs.mybaties3.data.BaseDO;
import tech.codingless.core.plugs.mybaties3.data.PageRollResult;
import tech.codingless.core.plugs.mybaties3.data.UpdateObject;

/**
 * common CRUD method for all sql entity service
 * 
 * @author 王鸿雁
 * @param <T> The Entity Need Extends BaseDO
 */
public interface DBBaseGenericService<T extends BaseDO> {

	/**
	 * insert a new row, you can set your data id, system will auto create with
	 * ObjectId if not. If the id exist in database, the method will throw Exception
	 * 
	 * @param data Entity
	 * @return success if true
	 */
	boolean create(T data);
	
	
	/**
	 * 
	 * execute insert if data id is null, otherwise execute insert when data id not exist, update when data id exist in database
	 * 
	 * @param list
	 * @return success if true
	 */
	int upinsert(List<T> list);

	/**
	 * insert a new row with company id
	 * 
	 * @param companyId companyId
	 * @param data      Entity
	 * @return success if true
	 */
	boolean create(String companyId, T data);

	/**
	 * batch create
	 * 
	 * @param list List of Entity
	 * @return success if true
	 */
	boolean create(List<T> list);

	/**
	 * batch create with company id
	 * 
	 * @param companyId companyId
	 * @param list      List of Entity
	 * @return true if create success
	 */
	boolean create(String companyId, List<T> list);

	/**
	 * is deprecated ,please use updateSkipNull
	 * 
	 * @param data Entity
	 * @return true if update success
	 */
	@Deprecated
	boolean update(T data);

	/**
	 * 
	 * @param companyId Company Id
	 * @param data      Entity
	 * @param ver       The version of the old data
	 * @return true if update success
	 */
	boolean updateNotNull(String companyId, T data, Long ver);

	boolean updateSkipNull(String companyId, T data, Long ver);

	boolean updateSkipNull(T data, Long ver);

	/**
	 * 批量更新，当缓存中达到batchSize指定的数量时，执行更新，否则只是加入缓存
	 * 
	 * @param companyId Company Id
	 * @param data      Entity
	 * @param ver       The version of the old data
	 * @param batchSize The Batch Size of rows, execute quickly insert when arrive
	 *                  the size
	 * @return success size
	 */
	int batchUpdateAppend(String companyId, T data, Long ver, int batchSize);

	/**
	 * 立即执行所有缓存中的数据并更新
	 * 
	 * @param clazz The class of entity
	 * @return success size
	 */
	int batchUpdateExecute(Class<T> clazz);

	/**
	 * 批量更新
	 * 
	 * @param updateList Batch update of list
	 * @return success size
	 */
	int batchUpdate(List<UpdateObject> updateList);

	/**
	 * 修改对象，条件是主键及 companyId
	 * 
	 * @param data      Entity
	 * @param companyId Company Id
	 * @return success
	 */
	boolean update(T data, String companyId);

	/**
	 * 不管这个数据的所有者是谁,只根据ID进行修改
	 * 
	 * @param entity Entity
	 * @return success
	 */
	@Deprecated
	boolean updateSkipCheckOwner(T entity);

	/**
	 * 根据ID获取一条数据
	 * 
	 * @param clazz clazz
	 * @param id    The Id of Data
	 * @return data
	 */
	T get(Class<T> clazz, String id);

	T get(String id);

	/**
	 * 根据companyId, id 过滤对像
	 * 
	 * @param clazz     clazz
	 * @param id        The Id of Data
	 * @param companyId Id of company
	 * @return data
	 */
	T get(Class<T> clazz, String id, String companyId);

	T get(String id, String companyId);

	List<T> get(Class<T> clazz, String companyId, Collection<String> idList);

	List<T> get(String companyId, Collection<String> idList);

	List<T> get(Class<T> clazz, String companyId, Collection<String> idList, Collection<String> columns);

	List<T> get(String companyId, Collection<String> idList, Collection<String> columns);

	/**
	 * 物理删除，小时使用。推荐大多数场合下从产品上不设置删除功能，如果设置了删除功能应使用逻辑删除
	 * 
	 * @param clazz     clazz
	 * @param id        id of data
	 * @param companyId Id Of company
	 * @return true if delete success
	 *
	 */
	boolean deletePhysical(Class<T> clazz, String id, String companyId);

	boolean deletePhysical(String id, String companyId);

	/**
	 * 逻辑删除
	 * 
	 * @param clazz     data class
	 * @param id        data id
	 * @param companyId company id
	 * @return true if delete success
	 */
	boolean deleteLogical(Class<T> clazz, String id, String companyId);

	boolean deleteLogical(String id, String companyId);

	/**
	 * 批量逻辑删除
	 * 
	 * @param clazz     data class
	 * @param idList    batch find of id
	 * @param companyId company id
	 * @return true if delete success
	 *
	 */
	int deleteLogical(Class<T> clazz, Collection<String> idList, String companyId);

	int deleteLogical(Collection<String> idList, String companyId);

	/**
	 * 获得一张表的所有数据
	 * 
	 * @param clazz data class
	 * @return data
	 */
	List<T> list(Class<T> clazz);

	List<T> list();

	List<T> list(Class<T> clazz, String companyId);

	List<T> list(String companyId);

	PageRollResult<?> rollPage(String selectId, Map<String, Object> param, int size, int page);

	PageRollResult<T> rollPage(ColumnSelector<T> columns, QueryConditionWrapper<T> wrapper, SerializableFunction<T, Object> sortColumn, OrderTypeEnum orderType, Integer size, Integer page);

	List<T> findByExample(Class<T> clazz, String companyId, T example, Integer size);

	List<T> findByExample(String companyId, T example, Integer size);

	List<T> findByExample(Class<T> clazz, T example, Integer size);

	List<T> findByExample(T example, Integer size);

	/**
	 * 通过例子查找一个，多于一个结果会报错
	 * 
	 * @param clazz     class
	 * @param companyId id of company
	 * @param example   condition template
	 * @return data
	 *
	 */
	T findOneByExample(Class<T> clazz, String companyId, T example);

	T findOneByExample(String companyId, T example);

	String getEntityClassName();

	/**
	 * 查询
	 * 
	 * @param selectId the sql id in sqlmap
	 * @param param    the param
	 * @param offset   from 0
	 * @param limit    return size
	 * @return data
	 */
	List<Map<String, ?>> select(String selectId, Map<String, ?> param, int offset, int limit);

	List<Map<String, ?>> select(String selectId, String param, int offset, int limit);

	/**
	 * 多条件，指定返回列，可排序的单表查询
	 * 
	 * @param columns    the columns you want to return
	 * @param wrapper    condition wrapper
	 * @param sortColumn sort column
	 * @param orderType  order type
	 * @param offset     from 0
	 * @param limit      max of return size
	 * @return data
	 */
	List<T> select(ColumnSelector<T> columns, QueryConditionWrapper<T> wrapper, SerializableFunction<T, Object> sortColumn, OrderTypeEnum orderType, int offset, int limit);

	public long count(QueryConditionWrapper<T> wrapper);
}
