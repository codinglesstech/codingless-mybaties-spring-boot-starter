package tech.codingless.core.plugs.mybaties3;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import com.alibaba.fastjson.JSON;

import tech.codingless.core.plugs.mybaties3.annotation.OrderTypeEnum;
import tech.codingless.core.plugs.mybaties3.condition.ColumnSelector;
import tech.codingless.core.plugs.mybaties3.condition.QueryConditionWrapper;
import tech.codingless.core.plugs.mybaties3.data.BaseDO;
import tech.codingless.core.plugs.mybaties3.data.PageRollResult;
import tech.codingless.core.plugs.mybaties3.data.UpdateObject;
import tech.codingless.core.plugs.mybaties3.helper.ColumnHelper;
import tech.codingless.core.plugs.mybaties3.util.DataEnvUtil;
import tech.codingless.core.plugs.mybaties3.util.DataSessionEnv;
import tech.codingless.core.plugs.mybaties3.util.MybatiesAssertUtil;
import tech.codingless.core.plugs.mybaties3.util.MybatiesStringUtil;
  
 
public class DBBaseGenericServiceImpl<T extends BaseDO> implements DBBaseGenericService<T> {
	@Autowired
	protected GenericUpdateDao<T> updateDao;
	@Autowired
	protected GenericQueryDao<T> queryDao;
	@SuppressWarnings("rawtypes")
	@Autowired
	protected GenericQueryDao queryGenericDao;

	private String namespace;

	/**
	 * 获得对应Mapper的Name Spance
	 * 
	 * @return
	 */
	protected String namespace() {
		if (namespace != null) {
			return namespace;
		}
		Type type = getClass().getGenericSuperclass();
		Type trueType = ((ParameterizedType) type).getActualTypeArguments()[0];
		namespace = trueType.getTypeName() + ".";
		return namespace;
	}
 
	@Transactional
	@Override
	public boolean create(T data) {
		/*
		if(obj instanceof ScriptObjectMirror) {
			Class<?> clazz = getEntityClass();
			obj = (T)ScriptObjectMirrorUtil.to(obj,clazz);
		}
		*/
		 
		data.setDel(false);
		data.setEnv(DataEnvUtil.getEvn());
		if (MybatiesStringUtil.isEmpty(data.getId())) {
			data.generatorKey();
		}
		if(MybatiesStringUtil.isEmpty(data.getCreateUid())) {
			data.setCreateUid(DataSessionEnv.CURRENT_USER_ID.get());
		} 
		if(MybatiesStringUtil.isEmpty(data.getOwnerId())) {
			data.setOwnerId(DataSessionEnv.CURRENT_USER_ID.get()); 
		}
		data.setWriteUid(MybatiesStringUtil.isNotEmpty(data.getWriteUid()) ? data.getWriteUid() : DataSessionEnv.CURRENT_USER_ID.get());
		data.setCompanyId(MybatiesStringUtil.isNotEmpty(data.getCompanyId()) ? data.getCompanyId() : DataSessionEnv.CURRENT_COMPANY_ID.get()); 
		data.setVer(data.getVer() != null ? data.getVer() : 1L); 
		return updateDao.createEntity(data) == 1;
	}

	@Transactional
	@Override
	public boolean create(String companyId, T obj) {
		MybatiesAssertUtil.assertNotEmpty(companyId, "COMPANY_ID_NOT_EXIST");
		BaseDO entiry = obj;
		entiry.setCompanyId(companyId);
		return create(obj);
	}

	@Transactional
	@Override
	public boolean update(T data) { 
		if (DataSessionEnv.CURRENT_USER_ID.get() == null || !DataSessionEnv.CURRENT_USER_ID.get().equals(data.getOwnerId())) {
			return false;
		}
		data.setWriteUid(DataSessionEnv.CURRENT_USER_ID.get());
		boolean success = updateDao.updateEntity(data) == 1;
		return success;

	}

 

	@Transactional
	@Override
	public boolean updateNotNull(String companyId, T data, Long ver) {  
		MybatiesAssertUtil.assertNotEmpty(companyId, "COMPANY_ID_EMPTY");
		data.setCompanyId(companyId); 
		return this.updateSkipNull(data, ver);
	}

	@Transactional
	@Override
	public boolean updateSkipNull(String companyId, T data, Long ver) {
		/*
		if(entity instanceof ScriptObjectMirror) {
			Class<?> clazz = getEntityClass();
			entity = (T)ScriptObjectMirrorUtil.to(entity,clazz);
		} 
		*/
		MybatiesAssertUtil.assertNotEmpty(companyId, "COMPANY_ID_EMPTY");
		data.setCompanyId(companyId); 
		return this.updateSkipNull(data, ver);
	}

	@Transactional
	@Override
	public boolean updateSkipNull(T data, Long ver) {
		MybatiesAssertUtil.assertTrue(updateDao.updateNotNull(data, ver) == 1, "UPDATE_NOT_NULL_FAIL"); 
		return false;
	}
	
	
	@Override
	public int batchUpdateAppend(String companyId, T data, Long ver, int batchSize) {
		return updateDao.updateSkipNullBatchAppend(companyId,data,ver,batchSize);
	}
	
	@Override
	public int batchUpdateExecute(Class<T> clazz) {
		return updateDao.updateSkipNullBatchExecute(clazz);
	}
	
	@Override
	public int batchUpdate(List<UpdateObject> updateList) {
		return updateDao.updateSkipNullBatchExecute(updateList);
	}
	

	@Transactional
	@Override
	public boolean update(T data, String companyId) { 
		data.setWriteUid(DataSessionEnv.CURRENT_USER_ID.get());
		return updateDao.updateEntityWithCompanyId(data, companyId) == 1;
	}

	@Transactional
	@Override
	public boolean updateSkipCheckOwner(T data) { 
		data.setWriteUid(DataSessionEnv.CURRENT_USER_ID.get());
		return updateDao.updateEntity(data) == 1;
	}

  
	@Transactional
	@Override
	public boolean deletePhysical(Class<T> clazz, String id, String companyId) {
		return updateDao.deleteEntityWithCompanyId(clazz, id, companyId) == 1;
	}

	@Transactional
	@Override
	public boolean deleteLogical(Class<T> clazz, String id, String companyId) {
		return updateDao.deleteLogicalWithCompanyId(clazz, id, companyId) == 1;
	}
	
	@Transactional
	@Override
	public int deleteLogical(Class<T> clazz, Collection<String> idList, String companyId) { 
		return updateDao.deleteLogicalWithCompanyId(clazz,idList,companyId);
	}

	@Override
	public List<T> list(Class<T> clazz) {
		return queryDao.list(clazz);
	}

	@Override
	public T get(Class<T> clazz, String entityId) {
		if (MybatiesStringUtil.isNotEmpty(DataSessionEnv.CURRENT_COMPANY_ID.get())) {
			return queryDao.getEntity(clazz, entityId);
		}
		return queryDao.getEntity(clazz, entityId);
	}
	
	@Override
	public T get(String id) { 
		return this.get(getEntityClass(), id);
	}

	@Override
	public T get(Class<T> clazz, String id, String companyId) {
		T t = queryDao.getEntity(clazz, id, companyId);
		return t;
	}

	@Override
	public List<T> get(Class<T> clazz, String companyId, Collection<String> idList) {
		return queryDao.findEntityList(clazz, companyId, idList);
	}
	
	@Override
	public List<T> get(Class<T> clazz, String companyId, Collection<String> idList, Collection<String> columns) {
		
		//检查列字段的合法性
		if(!CollectionUtils.isEmpty(columns)) { 
			MybatiesAssertUtil.assertTrue(columns.stream().filter(column->ColumnHelper.isIncorrectColumn(column)).count()==0, "HAS_INCORRECT_COLUMN"); 
		}
		
		return queryDao.findEntityList(clazz, companyId, idList,columns);
	}

	@Transactional
	@Override
	public boolean create(List<T> list) {
		list.forEach(item -> { 
			if (MybatiesStringUtil.isEmpty(item.getId())) {
				item.generatorKey();
			}
			item.setCreateUid(DataSessionEnv.CURRENT_USER_ID.get());
			item.setOwnerId(DataSessionEnv.CURRENT_USER_ID.get());
			item.setWriteUid(DataSessionEnv.CURRENT_USER_ID.get());
			item.setDel(false);
			if (MybatiesStringUtil.isEmpty(item.getCompanyId())) {
				item.setCompanyId(DataSessionEnv.CURRENT_COMPANY_ID.get());
			}
			if (item.getVer() == null) {
				item.setVer(1L);
			} 
			item.setEnv(DataEnvUtil.getEvn());
		}); 
		return updateDao.createEntityList(list) == list.size();
	}

	@Transactional
	@Override
	public boolean create(String companyId, List<T> list) {
		list.forEach(item -> { 
			item.setCompanyId(companyId);
			item.setEnv(DataEnvUtil.getEvn()); 
		});
		return create(list);
	}

	@Transactional
	@Override
	public List<T> list(Class<T> clazz, String companyId) {
		return queryDao.list(clazz, companyId);
	}

	@Override
	public PageRollResult<T> rollPage(Class<T> clazz, String companyId, T param, String orderColumn, OrderTypeEnum orderType, Integer size, Integer page) {
		return queryDao.rollPage(clazz, companyId, param, orderColumn, orderType, size, page);
	}

	@Override
	public PageRollResult<?> rollPage(String selectId, Map<String, Object> param, int size, int page) {
		return queryDao.rollPage(namespace(), selectId, param, size, page);
	}

	@Override
	public T findOneByExample(Class<T> clazz, String companyId, T example) {
		List<T> list = this.findByExample(clazz, companyId, example, 2);
		MybatiesAssertUtil.assertTrue(list == null || list.size() <= 1, "RESULT_MORE_THEN_ONE");
		if (CollectionUtils.isEmpty(list)) {
			return null;
		}
		return list.get(0);
	}

	@Override
	public List<T> findByExample(Class<T> clazz, String companyId, T example, Integer size) {
		
		PageRollResult<T> result = queryDao.rollPage(clazz, companyId, example, null, null, size, 1);
		return result.getList();
	}
	
	@Override
	public List<T> findByExample(Class<T> clazz, T example, Integer size) {
		PageRollResult<T> result = queryDao.rollPage(clazz, null, example, null, null, size, 1);
		return result.getList();
	}
 
	

	@SuppressWarnings("unchecked")
	@Override
	public <E> List<E> noShardingList(String statement, Object parameter) {
		return queryGenericDao.noShardingList(statement, parameter);
	}
	
	
	@Override
	public String getEntityClassName() {
		Type type = getClass().getGenericSuperclass();
		Type trueType = ((ParameterizedType) type).getActualTypeArguments()[0];
		return trueType.getTypeName();
	}
	 
	@SuppressWarnings("unchecked")
	private Class<T> getEntityClass() {
		Type type = getClass().getGenericSuperclass();
		Type trueType = ((ParameterizedType) type).getActualTypeArguments()[0];
		return (Class<T>) trueType;
	}

	@Override
	public T get(String id, String companyId) { 
		return this.get(getEntityClass(), id, companyId);
	}

	@Override
	public List<T> get(String companyId, Collection<String> idList) { 
		return this.get(getEntityClass(), companyId, idList);
	}

	@Override
	public List<T> get(String companyId, Collection<String> idList, Collection<String> columns) { 
		return this.get(getEntityClass(), companyId, idList, columns);
	}

	@Override
	public boolean deletePhysical(String id, String companyId) { 
		return deletePhysical(getEntityClass(), id, companyId);
	}

	@Override
	public boolean deleteLogical(String id, String companyId) { 
		return deleteLogical(getEntityClass(), id, companyId);
	}

	@Override
	public int deleteLogical(Collection<String> idList, String companyId) { 
		return deleteLogical(getEntityClass(), idList, companyId);
	}

	@Override
	public List<T> list() { 
		return list(getEntityClass());
	}

	@Override
	public List<T> list(String companyId) { 
		return list(getEntityClass(), companyId);
	}

	@Override
	public PageRollResult<T> rollPage(String companyId, T param, String orderColumn, OrderTypeEnum orderType, Integer size, Integer page) { 
		return rollPage(getEntityClass(), companyId, param, orderColumn, orderType, size, page);
	}

	@Override
	public List<T> findByExample(String companyId, T example, Integer size) { 
		return findByExample(getEntityClass(), companyId, example, size);
	}

	@Override
	public List<T> findByExample(T example, Integer size) { 
		return findByExample(getEntityClass(), example, size);
	}

	@Override
	public T findOneByExample(String companyId, T example) { 
		return findOneByExample(getEntityClass(), companyId, example);
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<Map<String, ?>> select(String selectId, Map<String, ?> param, int offset, int limit) {  
		return queryGenericDao.select(selectId, param, offset, limit); 
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<Map<String, ?>> select(String selectId, String param, int offset, int limit) { 
		return queryGenericDao.select(selectId, MybatiesStringUtil.isEmpty(param)?new HashMap<String,Object>():JSON.parseObject(param), offset, limit); 
	}
	
	 
	@SuppressWarnings("unchecked")
	@Override
	public List<T> select(ColumnSelector<T> columns,  QueryConditionWrapper<T> wrapper,SerializableFunction<T, Object> orderColumn, OrderTypeEnum orderType,  int offset, int limit) { 
	 
		wrapper.isFalse(BaseDO::isDel);
		wrapper.eq(BaseDO::getEnv, DataEnvUtil.getEvn()); 
		return queryGenericDao.select(getEntityClass(),columns, wrapper,orderColumn,orderType, offset, limit);
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public long count(QueryConditionWrapper<T> wrapper) {
		wrapper.isFalse(BaseDO::isDel);
		wrapper.eq(BaseDO::getEnv, DataEnvUtil.getEvn());
		return queryGenericDao.count(getEntityClass(),wrapper);
	}
	
}
