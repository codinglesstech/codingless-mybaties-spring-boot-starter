package tech.codingless.biz.core.plugs.mybaties3.condition;

import java.lang.reflect.Field;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

import org.springframework.util.CollectionUtils;

import tech.codingless.biz.core.plugs.mybaties3.BaseDO;
import tech.codingless.biz.core.plugs.mybaties3.SerializableFunction;
import tech.codingless.biz.core.plugs.mybaties3.TableAutoCreateServiceMysqlImpl;
import tech.codingless.biz.core.plugs.mybaties3.util.ReflectionUtil;

public class QueryConditionWrapper<T extends BaseDO> implements BaseQueryWrapper {
	private boolean skipWithNull = true;
	private QueryConditionRelEnums linkType = QueryConditionRelEnums.AND;
	private LinkedList<BaseQueryWrapper> wrapperList = new LinkedList<>();
	private boolean hasPreCondition;//是否有前置条件
	
	public void setHasPreCondition(boolean hasPreCondition) {
		this.hasPreCondition = hasPreCondition;
	}
	public boolean isHasPreCondition() {
		return hasPreCondition;
	}
	private Map<String,Object> context=new HashMap<>(); 
	public Map<String, Object> getContext() { 
		return context;
	}
	public void setContext(Map<String, Object> context) {
		this.context = context;
	}
	public LinkedList<BaseQueryWrapper> getWrapperList() {
		return wrapperList;
	}

	public QueryConditionRelEnums getLinkType() {
		return linkType;
	}

	public void setLinkType(QueryConditionRelEnums linkType) {
		this.linkType = linkType;
	}

	  
	public QueryConditionWrapper<T> eq(SerializableFunction<T, Object> column, Object value) {
		if(value==null) {
			return this;
		}
		QueryCondition condition = new QueryCondition();
		condition.setType(QueryConditionEnums.EQ);
		Field filed = ReflectionUtil.findField(column); 
		condition.setPropName(filed.getName());
		condition.setColumnName(TableAutoCreateServiceMysqlImpl.change2dbFormat(filed.getName()).toLowerCase());
		condition.setValue(value);
		condition.setHasPreCondition(!wrapperList.isEmpty());
		getContext().put(condition.getPropName(), condition.getValue()); 
		wrapperList.add(condition);
		return this;
	}

	public QueryConditionWrapper<T> isTrue(SerializableFunction<T, Boolean> column) {
		QueryCondition condition = new QueryCondition();
		condition.setType(QueryConditionEnums.IS);
		Field filed = ReflectionUtil.findField(column);
		condition.setPropName(filed.getName());
		condition.setColumnName(TableAutoCreateServiceMysqlImpl.change2dbFormat(filed.getName()).toLowerCase());
		condition.setValue(true);
		condition.setHasPreCondition(!wrapperList.isEmpty());
		getContext().put(condition.getPropName(), condition.getValue()); 
		wrapperList.add(condition);
		return this;
	}

	public QueryConditionWrapper<T> isFalse(SerializableFunction<T, Boolean> column) {
		QueryCondition condition = new QueryCondition();
		condition.setType(QueryConditionEnums.IS);
		Field filed = ReflectionUtil.findField(column);
		condition.setPropName(filed.getName());
		condition.setColumnName(TableAutoCreateServiceMysqlImpl.change2dbFormat(filed.getName()).toLowerCase());
		condition.setValue(false);
		condition.setHasPreCondition(!wrapperList.isEmpty());
		getContext().put(condition.getPropName(), condition.getValue()); 
		wrapperList.add(condition);
		return this;
	}

	public QueryConditionWrapper<T> isNull(SerializableFunction<T, Object> column) {
		QueryCondition condition = new QueryCondition();
		condition.setHasPreCondition(!wrapperList.isEmpty());
		condition.setType(QueryConditionEnums.IS_NULL);
		Field filed = ReflectionUtil.findField(column);
		condition.setPropName(filed.getName());
		condition.setColumnName(TableAutoCreateServiceMysqlImpl.change2dbFormat(filed.getName()).toLowerCase()); 
		wrapperList.add(condition);
		return this;
	}

	public QueryConditionWrapper<T> isNotNull(SerializableFunction<T, Object> column) {
		QueryCondition condition = new QueryCondition();
		condition.setHasPreCondition(!wrapperList.isEmpty());
		condition.setType(QueryConditionEnums.IS_NOT_NULL);
		Field filed = ReflectionUtil.findField(column);
		condition.setPropName(filed.getName());
		condition.setColumnName(TableAutoCreateServiceMysqlImpl.change2dbFormat(filed.getName()).toLowerCase());
		wrapperList.add(condition);
		return this;
	}

	public QueryConditionWrapper<T> between(SerializableFunction<T, Number> column, long min, long max) {
		QueryCondition condition = new QueryCondition();
		condition.setHasPreCondition(!wrapperList.isEmpty());
		condition.setType(QueryConditionEnums.BETWEEN);
		Field filed = ReflectionUtil.findField(column);
		condition.setPropName(filed.getName());
		condition.setColumnName(TableAutoCreateServiceMysqlImpl.change2dbFormat(filed.getName()).toLowerCase());
		condition.setMin(min);
		condition.setMax(max);
		getContext().put(condition.getPropName()+QueryCondition.PROP_NAME_ENDFIX_MIN, condition.getMin()); 
		getContext().put(condition.getPropName()+QueryCondition.PROP_NAME_ENDFIX_MAX, condition.getMax()); 
		wrapperList.add(condition);
		return this;
	}
	

	public QueryConditionWrapper<T> between(String fieldName, long min, long max) {
		QueryCondition condition = new QueryCondition();
		condition.setHasPreCondition(!wrapperList.isEmpty());
		condition.setType(QueryConditionEnums.BETWEEN); 
		condition.setPropName(fieldName);
		condition.setColumnName(TableAutoCreateServiceMysqlImpl.change2dbFormat(fieldName).toLowerCase());
		condition.setMin(min);
		condition.setMax(max);
		getContext().put(condition.getPropName()+QueryCondition.PROP_NAME_ENDFIX_MIN, condition.getMin()); 
		getContext().put(condition.getPropName()+QueryCondition.PROP_NAME_ENDFIX_MAX, condition.getMax()); 
		wrapperList.add(condition);
		return this;
	}

	public QueryConditionWrapper<T> in(SerializableFunction<T, Object> column, Collection<Object> values) {
		if(CollectionUtils.isEmpty(values)) {
			return this;
		}
		QueryCondition condition = new QueryCondition();
		condition.setHasPreCondition(!wrapperList.isEmpty());
		condition.setType(QueryConditionEnums.IN);
		Field filed = ReflectionUtil.findField(column);
		condition.setPropName(filed.getName());
		condition.setColumnName(TableAutoCreateServiceMysqlImpl.change2dbFormat(filed.getName()).toLowerCase());
		condition.setValue(values);
		getContext().put(condition.getPropName(), condition.getValue()); 
		wrapperList.add(condition);
		return this;
	}

	public QueryConditionWrapper<T> lt(SerializableFunction<T, Number> column, Number val) {
		if(val==null) {
			return this;
		}
		QueryCondition condition = new QueryCondition();
		condition.setHasPreCondition(!wrapperList.isEmpty());
		condition.setType(QueryConditionEnums.LT);
		Field filed = ReflectionUtil.findField(column);
		condition.setPropName(filed.getName());
		condition.setColumnName(TableAutoCreateServiceMysqlImpl.change2dbFormat(filed.getName()).toLowerCase());
		condition.setValue(val);
		getContext().put(condition.getPropName()+QueryCondition.PROP_NAME_ENDFIX_LT, condition.getValue()); 
		wrapperList.add(condition);
		return this;
	}

	public QueryConditionWrapper<T> gt(SerializableFunction<T, Number> column, Number val) {
		if(val==null) {
			return this;
		}
		QueryCondition condition = new QueryCondition();
		condition.setHasPreCondition(!wrapperList.isEmpty());
		condition.setType(QueryConditionEnums.GT);
		Field filed = ReflectionUtil.findField(column);
		condition.setPropName(filed.getName());
		condition.setColumnName(TableAutoCreateServiceMysqlImpl.change2dbFormat(filed.getName()).toLowerCase());
		condition.setValue(val);
		getContext().put(condition.getPropName()+QueryCondition.PROP_NAME_ENDFIX_GT, condition.getValue()); 
		wrapperList.add(condition);
		return this;
	}

	public QueryConditionWrapper<T> before(SerializableFunction<T, Date> column, Date val) {
		if(val==null) {
			return this;
		}
		QueryCondition condition = new QueryCondition();
		condition.setHasPreCondition(!wrapperList.isEmpty());
		condition.setType(QueryConditionEnums.BEFORE);
		Field filed = ReflectionUtil.findField(column);
		condition.setPropName(filed.getName());
		condition.setColumnName(TableAutoCreateServiceMysqlImpl.change2dbFormat(filed.getName()).toLowerCase());
		condition.setValue(val);
		getContext().put(condition.getPropName()+QueryCondition.PROP_NAME_ENDFIX_BEFORE, condition.getValue()); 
		wrapperList.add(condition);
		return this;
	}

	public QueryConditionWrapper<T> after(SerializableFunction<T, Date> column, Date val) {
		if(val==null) {
			return this;
		}
		QueryCondition condition = new QueryCondition();
		condition.setHasPreCondition(!wrapperList.isEmpty());
		condition.setType(QueryConditionEnums.AFTER);
		Field filed = ReflectionUtil.findField(column);
		condition.setPropName(filed.getName());
		condition.setColumnName(TableAutoCreateServiceMysqlImpl.change2dbFormat(filed.getName()).toLowerCase());
		condition.setValue(val);
		getContext().put(condition.getPropName()+QueryCondition.PROP_NAME_ENDFIX_AFTER, condition.getValue()); 
		wrapperList.add(condition);
		return this;
	}

	 

	public QueryConditionWrapper<T> and(QueryConditionWrapper<T> subWraper) {
		
		if(!subWraper.getContext().isEmpty()) {
			this.context.putAll(subWraper.getContext());
		} 
		subWraper.setHasPreCondition(true);
		subWraper.setContext(this.context);
		subWraper.setLinkType(QueryConditionRelEnums.AND);
		subWraper.setSkipWithNull(this.skipWithNull);
		wrapperList.add(subWraper);
		return this;

	}

	public QueryConditionWrapper<T> or(QueryConditionWrapper<T> subWraper) {
		if(!subWraper.getContext().isEmpty()) {
			this.context.putAll(subWraper.getContext());
		} 
		subWraper.setHasPreCondition(true);
		subWraper.setContext(this.context);
		subWraper.setLinkType(QueryConditionRelEnums.OR);
		subWraper.setSkipWithNull(this.skipWithNull);
		wrapperList.add(subWraper);
		return this;
	}

	public QueryConditionWrapper<T> setSkipWithNull(boolean skipWithNull) {
		this.skipWithNull = skipWithNull;
		return this;
	}

	public StringBuilder build() {
		this.build(this);
		return this.toSql();
	}
 
	public StringBuilder toSql() {
		StringBuilder builder = new StringBuilder(); 
		if(getWrapperList().isEmpty()) {
			return builder;
		}
		if (QueryConditionRelEnums.AND == linkType && isHasPreCondition()) {
			 builder.append(" and ( ");
		}else if (QueryConditionRelEnums.OR == linkType && isHasPreCondition()) {
			 builder.append(" or ( ");
		}  
		for (BaseQueryWrapper wrapper : getWrapperList()) {
			if (wrapper instanceof QueryCondition) { 
				StringBuilder condition = ((QueryCondition)wrapper).toSql();
				builder.append(condition); 
			} else if (wrapper instanceof QueryConditionWrapper) {
				builder.append(" ").append(((QueryConditionWrapper<?>) wrapper).toSql());
			}
		}
		if(isHasPreCondition()) {
			builder.append(" )"); 
		}
		return builder;
	}
 
	private QueryConditionWrapper<T> build(QueryConditionWrapper<T> wraper) {
		boolean noSubWrapper = wraper.getWrapperList().stream().filter(item -> item instanceof QueryConditionWrapper).count() == 0;
		if (noSubWrapper) {
			return this;
		}

		LinkedList<BaseQueryWrapper> tmpList = new LinkedList<>();
		QueryConditionWrapper<T> tmpWrapper = new QueryConditionWrapper<T>();
		for (BaseQueryWrapper wrapper : wraper.getWrapperList()) {
			if (wrapper instanceof QueryCondition) {
				tmpWrapper.setLinkType(this.linkType);
				tmpWrapper.getWrapperList().add(wrapper);
			} else if (wrapper instanceof QueryConditionWrapper) { 
				((QueryConditionWrapper<?>) wrapper).build();
				if (!tmpWrapper.getWrapperList().isEmpty()) {
					tmpList.add(tmpWrapper);
					tmpWrapper = new QueryConditionWrapper<T>();
				}
				tmpList.add(wrapper);
			}
		}

		if (!tmpWrapper.getWrapperList().isEmpty()) {
			tmpList.add(tmpWrapper);
		}
		wraper.getWrapperList().clear();
		wraper.getWrapperList().addAll(tmpList);
		return this;
	}
}
