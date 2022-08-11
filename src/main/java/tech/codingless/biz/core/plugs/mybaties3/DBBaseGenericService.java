package tech.codingless.biz.core.plugs.mybaties3;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import tech.codingless.biz.core.plugs.mybaties3.condition.ColumnSelector;
import tech.codingless.biz.core.plugs.mybaties3.condition.QueryConditionWrapper;
import tech.codingless.biz.core.plugs.mybaties3.data.UpdateObject;

/**
 * common CRUD method for all sql entity service
 * @author 王鸿雁
 * @version 2.0.0
 * @param <T>
 */
public interface DBBaseGenericService<T extends BaseDO> {

	/**
	 * insert a new row, you can set your  data id, system will auto create with ObjectId if not. If the id exist in database, the method will throw Exception
	 * @param data
	 * @return
	 */
	boolean create(T data);
	/**
	 * insert a new row with company id
	 * 
	 * @param data
	 * @return
	 */
	boolean create(String companyId, T data);

	/**
	 * batch create
	 * 
	 * @param list
	 * @return
	 */
	boolean create(List<T> list);

	/**
	 * batch create with company id
	 * @param companyId
	 * @param list
	 * @return
	 */
	boolean create(String companyId, List<T> list);

	/**
	 * is deprecated ,please use updateSkipNull  
	 * @param data
	 * @return
	 */
	@Deprecated
	boolean update(T data);
 
	/**
	 * <p>
	 * 修改只有不为空的才会更新，
	 * 
	 * @param companyId
	 * @param entity
	 * @param ver       版本不能为空
	 * @return
	 */
	boolean updateNotNull(String companyId, T data, Long ver);

	boolean updateSkipNull(String companyId, T data, Long ver);
	boolean updateSkipNull(T data, Long ver);

	/**
	 * 批量更新，当缓存中达到batchSize指定的数量时，执行更新，否则只是加入缓存
	 * @author 王鸿雁
	 * @param companyId
	 * @param data
	 * @param ver
	 * @param batchSize
	 * @return
	 *
	 */
	int batchUpdateAppend(String companyId, T data, Long ver, int batchSize);
	/**
	 * 立即执行所有缓存中的数据并更新
	 * @author 王鸿雁
	 * @param companyId
	 * @param data
	 * @param ver
	 * @return
	 *
	 */
	int batchUpdateExecute(Class<T> clazz);
	
	/**
	 * 批量更新
	 * @author 王鸿雁
	 * @param updateList
	 * @return
	 *
	 */
	int batchUpdate(List<UpdateObject> updateList);
	
	/**
	 * 修改对象，条件是主键及 companyId
	 * 
	 * @param entity
	 * @param companyId
	 * @return
	 */
	boolean update(T data, String companyId);

	/**
	 * 不管这个数据的所有者是谁,只根据ID进行修改
	 * <h4 style="color:red">注意:调这该接口的时候场景要非常小心,请确保不会出现数据权限问题</h4>
	 * 
	 * @param entity
	 * @return
	 */
	@Deprecated
	boolean updateSkipCheckOwner(T entity);

	/**
	 * 根据ID获取一条数据
	 * 
	 * @param clazz
	 * @param id
	 * @return
	 */
	T get(Class<T> clazz, String id);
	T get(String id);

	/**
	 * 根据companyId, id 过滤对像
	 * 
	 * @param clazz
	 * @param id
	 * @param companyId
	 * @return
	 */
	T get(Class<T> clazz, String id, String companyId);
	T get(String id, String companyId);

	List<T> get(Class<T> clazz, String companyId, Collection<String> idList);
	List<T> get(String companyId, Collection<String> idList);
	/**
	 * 指定返回的列 
	 * @param clazz
	 * @param companyId
	 * @param idList
	 * @param columns
	 * @return
	 *
	 */
	List<T> get(Class<T> clazz, String companyId, Collection<String> idList, Collection<String> columns);
	List<T> get(String companyId, Collection<String> idList, Collection<String> columns);

 


	/**
	 * 物理删除，小时使用。推荐大多数场合下从产品上不设置删除功能，如果设置了删除功能应使用逻辑删除
	 * 
	 * @author 王鸿雁
	 * @param clazz
	 * @param id
	 * @param companyId
	 * @return
	 *
	 */
	boolean deletePhysical(Class<T> clazz, String id, String companyId);
	boolean deletePhysical(String id, String companyId);

	/**
	 * 逻辑删除
	 * 
	 * @author 王鸿雁
	 * @param clazz
	 * @param id
	 * @param companyId
	 * @return
	 *
	 */
	boolean deleteLogical(Class<T> clazz, String id, String companyId);
	boolean deleteLogical(String id, String companyId);
	/**
	 * 批量逻辑删除
	 * @author 王鸿雁
	 * @param clazz
	 * @param idList
	 * @param companyId
	 * @return
	 *
	 */
	int deleteLogical(Class<T> clazz, Collection<String> idList, String companyId);
	int deleteLogical(Collection<String> idList, String companyId);

	/**
	 * 获得一张表的所有数据
	 * 
	 * @param clazz
	 * @return
	 */
	List<T> list(Class<T> clazz);
	List<T> list();

	List<T> list(Class<T> clazz, String companyId);
	List<T> list(String companyId);

	/**
	 * 分页
	 * 
	 * @author 王鸿雁
	 * @param clazz
	 * @param companyId
	 * @param param
	 * @param orderColumn
	 * @param orderType
	 * @return
	 *
	 */
	PageRollResult<T> rollPage(Class<T> clazz, String companyId, T param, String orderColumn, OrderTypeEnum orderType, Integer size, Integer page);
	PageRollResult<T> rollPage(String companyId, T param, String orderColumn, OrderTypeEnum orderType, Integer size, Integer page);

	PageRollResult<?> rollPage(String selectId, Map<String, Object> param, int size, int page);

	/**
	 * 通过例子查找，最多返回指定条数
	 * 
	 * @author 王鸿雁
	 * @param clazz
	 * @param companyId
	 * @param example
	 * @param size
	 * @return
	 *
	 */
	List<T> findByExample(Class<T> clazz, String companyId, T example, Integer size); 
	List<T> findByExample(String companyId, T example, Integer size); 
	List<T> findByExample(Class<T> clazz, T example, Integer size); 
	List<T> findByExample(T example, Integer size); 

	/**
	 * 通过例子查找一个，多于一个结果会报错
	 * 
	 * @author 王鸿雁
	 * @param clazz
	 * @param companyId
	 * @param example
	 * @return
	 *
	 */
	T findOneByExample(Class<T> clazz, String companyId, T example);
	T findOneByExample(String companyId, T example);

	/**
	 * 
	 * 跳过 Sharding Jdbc执行原始的查询
	 *
	 */
	<E> List<E> noShardingList(String statement, Object parameter);
	/**
	 * 获得实体类的全名 
	 * @return
	 *
	 */
	String getEntityClassName();
	
	/**
	 * 查询
	 * @param selectId
	 * @param param
	 * @param offset
	 * @param limit
	 * @return
	 */
	List<Map<String,?>> select(String selectId,Map<String,?> param,int offset,int limit);
	List<Map<String,?>> select(String selectId,String param,int offset,int limit);
	
	/**
	 * 多条件，指定返回列，可排序的单表查询
	 * @param columns
	 * @param wrapper
	 * @param sortColumn
	 * @param orderType
	 * @param offset
	 * @param limit
	 * @return
	 */
	List<T> select(ColumnSelector<T > columns, QueryConditionWrapper<T> wrapper, SerializableFunction<T, Object> sortColumn, OrderTypeEnum orderType,  int offset, int limit);
	

	public long count(QueryConditionWrapper<T> wrapper);
}
