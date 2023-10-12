package tech.codingless.core.plugs.mybaties3;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import com.alibaba.fastjson2.JSON;

import tech.codingless.core.plugs.mybaties3.annotation.OrderTypeEnum;
import tech.codingless.core.plugs.mybaties3.condition.ColumnSelector;
import tech.codingless.core.plugs.mybaties3.condition.QueryConditionWrapper;
import tech.codingless.core.plugs.mybaties3.data.BaseDO;
import tech.codingless.core.plugs.mybaties3.data.DataEnvProperties;
import tech.codingless.core.plugs.mybaties3.data.PageRollResult;
import tech.codingless.core.plugs.mybaties3.data.UpdateObject;
import tech.codingless.core.plugs.mybaties3.helper.ColumnHelper;
import tech.codingless.core.plugs.mybaties3.strategy.IdCreator;
import tech.codingless.core.plugs.mybaties3.util.DataEnvUtil;
import tech.codingless.core.plugs.mybaties3.util.DataSessionEnv;
import tech.codingless.core.plugs.mybaties3.util.MybatiesAssertUtil;
import tech.codingless.core.plugs.mybaties3.util.MybatiesStringUtil;
import tech.codingless.core.plugs.mybaties3.util.ObjectUtil;
import tech.codingless.core.plugs.mybaties3.util.SnowFlakeNumberUtil;

public class DBBaseGenericServiceImpl<T extends BaseDO> implements DBBaseGenericService<T> {
	@Autowired
	protected GenericUpdateDao<T> updateDao;
	@Autowired
	protected GenericQueryDao<T> queryDao;
	@SuppressWarnings("rawtypes")
	@Autowired
	protected GenericQueryDao queryGenericDao;
	@Autowired(required = false)
	protected IdCreator idcreator;

	private String namespace;

	private void generateId(T data) {
		if (idcreator != null) {
			// 允许用户实现自己的主键生成策略
			String newId = idcreator.generateId(data);
			if (MybatiesStringUtil.isNotEmpty(newId)) {
				data.setId(newId);
				return;
			}
		}

		// ObjectId objectId = new ObjectId();
		// data.setId(objectId.toHexString());
		// 默认数据级别
		if (data.getDataLevel() == null) {
			data.setDataLevel(1);
		}
		// 默认采用雪花算法生成ID
		data.setId(Long.toString(SnowFlakeNumberUtil.nextId()));
	}

	/**
	 * 
	 * 
	 * @return 获得对应Mapper的Name Spance
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
		bindEnvInfo(data);
		return updateDao.createEntity(data) == 1;
	}

	void bindEnvInfo(T data) {
		data.setDel(false);
		data.setEnv(DataEnvUtil.getEvn());
		if (MybatiesStringUtil.isEmpty(data.getId())) {
			generateId(data);
		}
		data.setCreateUid(ObjectUtil.valid(data.getCreateUid(), DataEnvProperties.getOwnerId()));
		data.setOwnerId(ObjectUtil.valid(data.getOwnerId(), DataEnvProperties.getOwnerId()));
		data.setGroupId(ObjectUtil.valid(data.getGroupId(), DataEnvProperties.getGroupId()));
		data.setWriteUid(ObjectUtil.valid(data.getWriteUid(), DataEnvProperties.getOptUserId()));
		data.setCompanyId(ObjectUtil.valid(data.getCompanyId(), DataEnvProperties.getCompanyId()));
		data.setVer(data.getVer() != null ? data.getVer() : 1L);
	}

	@Transactional
	@Override
	public long execinsert(String xmlInsertSql, Map<String, Object> param) {
		return updateDao.execinsert(xmlInsertSql, param);
	}

	@Transactional
	@Override
	public int upinsert(List<T> list) {
		if (list == null || list.isEmpty()) {
			return 0;
		}
		list.forEach(item -> {
			bindEnvInfo(item);
		});
		return updateDao.upinsert(list);
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
		if (DataEnvProperties.getOptUserId() == null || !DataEnvProperties.getOptUserId().equals(data.getOwnerId())) {
			return false;
		}
		data.setWriteUid(DataEnvProperties.getOptUserId());
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
		return updateDao.updateSkipNullBatchAppend(companyId, data, ver, batchSize);
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
		return updateDao.deleteLogicalWithCompanyId(clazz, idList, companyId);
	}

	@Override
	public List<T> list(Class<T> clazz) {
		return queryDao.list(clazz);
	}

	@Override
	public T get(Class<T> clazz, String entityId) {
		return queryDao.getEntityV2(clazz, entityId, DataEnvProperties.getCompanyId());
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

		// 检查列字段的合法性
		if (!CollectionUtils.isEmpty(columns)) {
			MybatiesAssertUtil.assertTrue(columns.stream().filter(column -> ColumnHelper.isIncorrectColumn(column)).count() == 0, "HAS_INCORRECT_COLUMN");
		}

		return queryDao.findEntityList(clazz, companyId, idList, columns);
	}

	@Transactional
	@Override
	public boolean create(List<T> list) {
		list.forEach(item -> {
			if (MybatiesStringUtil.isEmpty(item.getId())) {
				generateId(item);
			}
			item.setCreateUid(DataEnvProperties.getOptUserId());
			item.setOwnerId(DataEnvProperties.getOptUserId());
			item.setWriteUid(DataEnvProperties.getOptUserId());
			item.setDel(false);
			if (MybatiesStringUtil.isEmpty(item.getCompanyId())) {
				item.setCompanyId(DataEnvProperties.getCompanyId());
			}
			if (item.getVer() == null) {
				item.setVer(1L);
			}
			item.setEnv(DataEnvProperties.getEnv());
		});
		return updateDao.createEntityList(list) == list.size();
	}

	@Transactional
	@Override
	public boolean create(String companyId, List<T> list) {
		list.forEach(item -> {
			item.setCompanyId(companyId);
			item.setEnv(DataEnvProperties.getEnv());
		});
		return create(list);
	}

	@Transactional
	@Override
	public List<T> list(Class<T> clazz, String companyId) {
		return queryDao.list(clazz, companyId);
	}

	@Override
	public PageRollResult<?> rollPage(String selectId, Map<String, Object> param, int size, int page) {
		return queryDao.rollPage(namespace(), selectId, param, size, page);
	}

	@Override
	public PageRollResult<T> rollPage(ColumnSelector<T> columns, QueryConditionWrapper<T> wrapper, SerializableFunction<T, Object> sortColumn, OrderTypeEnum orderType, Integer size, Integer page) {

		int limit = size == null ? 100 : size;
		int offset = page == null ? 0 : (page - 1) * size;
		List<T> list = select(columns, wrapper, sortColumn, orderType, offset, limit);
		long rows = count(wrapper);

		PageRollResult<T> result = new PageRollResult<>();
		result.setList(list);
		result.setCurrentPage(page);
		result.setPageSize(limit);
		result.setTotalPage((int) Math.ceil(rows / limit));
		result.setTotalCount((int) rows);
		return result;
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
		if (MybatiesStringUtil.isEmpty(companyId)) {
			return Collections.emptyList();
		}
		example.setCompanyId(companyId);
		return this.findByExample(clazz, example, size);
	}

	@Override
	public List<T> findByExample(Class<T> clazz, T example, Integer size) {
		return queryDao.findByExample(clazz, null, example, null, null, size, 1);
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
		return queryGenericDao.select(selectId, MybatiesStringUtil.isEmpty(param) ? new HashMap<String, Object>() : JSON.parseObject(param), offset, limit);
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<T> select(ColumnSelector<T> columns, QueryConditionWrapper<T> wrapper, SerializableFunction<T, Object> orderColumn, OrderTypeEnum orderType, int offset, int limit) {

		wrapper.isFalse(BaseDO::isDel);
		wrapper.eq(BaseDO::getEnv, DataEnvProperties.getEnv());
		return queryGenericDao.select(getEntityClass(), columns, wrapper, orderColumn, orderType, offset, limit);
	}

	@SuppressWarnings("unchecked")
	@Override
	public long count(QueryConditionWrapper<T> wrapper) {
		wrapper.isFalse(BaseDO::isDel);
		wrapper.eq(BaseDO::getEnv, DataEnvProperties.getEnv());
		return queryGenericDao.count(getEntityClass(), wrapper);
	}

}
