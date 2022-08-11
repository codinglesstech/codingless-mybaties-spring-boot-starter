package tech.codingless.biz.core.plugs.mybaties3.helper;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.ibatis.type.JdbcType;
import org.apache.ibatis.type.StringTypeHandler;

import lombok.Data;
import tech.codingless.biz.core.plugs.mybaties3.CommonSQLHelper;
import tech.codingless.biz.core.plugs.mybaties3.annotation.MyColumn;
import tech.codingless.biz.core.plugs.mybaties3.annotation.MyComment;
import tech.codingless.biz.core.plugs.mybaties3.util.MybatiesStringUtil;

public class MyTableColumnParser {

	//生成字段，更新，等需要跳过的属性
	private final static ConcurrentHashMap<String,Boolean> SKIP_PROPS = new ConcurrentHashMap<>();
	static {
		SKIP_PROPS.put("id", true);
		SKIP_PROPS.put("gmtCreate", true);
		SKIP_PROPS.put("gmtWrite", true);
		SKIP_PROPS.put("del", true);
		SKIP_PROPS.put("ownerId", true); 
		SKIP_PROPS.put("createUid", true);
		SKIP_PROPS.put("companyId", true);
		SKIP_PROPS.put("ver", true); 
	}
	
	
	@Data
	public static class ColumnProp {
		private String column;
		private String prop;
		private Class<?> javaTypeClass; 
		private Class<?> typeHandler; 
		private JdbcType jdbcType;
		private Object val;
		private String comment;
		private boolean virturl;
		private boolean readonly;
	}

	/**
	 * 解析出字段与属性的对应关系
	 * 
	 * @author 王鸿雁
	 * @param clazz
	 * @return
	 *
	 */
	public static List<ColumnProp> parse(Class<?> clazz) {
		List<ColumnProp> list = new ArrayList<>();
		for (Method method : clazz.getMethods()) {
			String methodName = method.getName();
			if (methodName.equals("getClass")) {
				continue;
			}
			if (!methodName.startsWith("get") && !methodName.startsWith("is")) {
				continue;
			}
			String attrName = new String();
			if (methodName.startsWith("get")) {
				attrName = methodName.substring(3);
			}
			if (methodName.startsWith("is")) {
				attrName = methodName.substring(2);
			}
			attrName = attrName.substring(0, 1).toLowerCase() + attrName.substring(1);
			String columnName = null;
			ColumnProp columnProp = new ColumnProp();
			try {
				Field filed = clazz.getDeclaredField(attrName);
				

				MyComment myComment = filed.getAnnotation(MyComment.class);
				if(myComment!=null) {
					columnProp.setComment(myComment.value());
				}
				
				MyColumn myColumn = filed.getAnnotation(MyColumn.class);
				if(myColumn!=null&&myColumn.virtual()) {
					continue;
				} 
				if (myColumn != null && MybatiesStringUtil.isNotEmpty(myColumn.name())) {
					columnName = myColumn.name();
				}
				if(myColumn!=null) {
					if(!StringTypeHandler.class.getName().equals(myColumn.typeHandler().getName())) {
						columnProp.setTypeHandler(myColumn.typeHandler());
					}
					columnProp.setReadonly(myColumn.readonly());
					columnProp.setVirturl(myColumn.virtual()); 
					String type=myColumn.type().toUpperCase().trim();
					if(type.startsWith("VARCHAR")) {
						columnProp.setJdbcType(JdbcType.VARCHAR);
					}else if(type.startsWith("INT")) {
						columnProp.setJdbcType(JdbcType.INTEGER);
					}else if(type.startsWith("DECIMAL")) {
						columnProp.setJdbcType(JdbcType.DECIMAL);
					}else {
						columnProp.setJdbcType(JdbcType.VARCHAR);
					}
				}
			} catch (Exception e1) {

			}
			if (MybatiesStringUtil.isEmpty(columnName)) {
				columnName = CommonSQLHelper.change2dbFormat(attrName);
			}
			columnProp.setColumn(columnName);
			columnProp.setProp(attrName);
			columnProp.setJavaTypeClass(method.getReturnType()); 
			list.add(columnProp);

		}
		return list;
	}

	/**
	 * 是否默认支持的类型
	 * @param clazz
	 * @return
	 *
	 */
	public static boolean isDefaultSupportType(Class<?> clazz) {
		if(clazz.getName().startsWith("java.")) {
			return true;
		}
		if(clazz.getName().equalsIgnoreCase("boolean")) {
			return true;
		} 
		return false;
	}

	/**
	 * 解析字段，跳过空值属性，虚拟属性,只读属性
	 * @author 王鸿雁
	 * @param clazz
	 * @return
	 * @throws InvocationTargetException 
	 * @throws IllegalArgumentException 
	 * @throws Exception 
	 *
	 */
	public static List<ColumnProp> parseSkipNull(Class<?> clazz,Object entity) throws Exception {
		List<ColumnProp> list = new ArrayList<>();
		for (Method method : clazz.getMethods()) {
			String methodName = method.getName();
			if (needSkipMethodName(methodName)) {
				continue;
			}
			String attrName = methodName2attrName(methodName);
			if (needSkipProperties(attrName)) {
				continue;
			}
			Object val = method.invoke(entity);
			if (val == null) {
				continue;
			}

			String columnName = null;
			try {
				Field filed = clazz.getDeclaredField(attrName);
				MyColumn myColumn = filed.getAnnotation(MyColumn.class);
				if (myColumn != null) { 
					if(myColumn.readonly()) {
						//只读字段不允许修改
						continue;
					} 
					columnName = MybatiesStringUtil.isNotEmpty(myColumn.name()) ? myColumn.name() : columnName;
				}
			} catch (Exception e) {

			}
			
			ColumnProp columnProp = new ColumnProp();
			if (MybatiesStringUtil.isEmpty(columnName)) {
				columnName = CommonSQLHelper.change2dbFormat(attrName);
			}
			columnProp.setColumn(columnName);
			columnProp.setProp(attrName);
			columnProp.setJavaTypeClass(method.getReturnType()); 
			columnProp.setVal(val);
			list.add(columnProp);
			
			 
		}
		return list;
	}
	
	public static boolean needSkipMethodName(String methodName) {
		return methodName.equals("getClass") || (!methodName.startsWith("get") && !methodName.startsWith("is"));
	}
	public static boolean needSkipProperties(String propName) { 
		return SKIP_PROPS.containsKey(propName); 
	}
	public static String methodName2attrName(String methodName) {
		String pName = new String();
		if (methodName.startsWith("get")) {
			pName = methodName.substring(3);
		} else if (methodName.startsWith("is")) {
			pName = methodName.substring(2);
		}
		pName = pName.substring(0, 1).toLowerCase() + pName.substring(1);
		return pName;
	}
}
