package tech.codingless.biz.core.plugs.mybaties3.helper;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import org.apache.ibatis.builder.SqlSourceBuilder;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.mapping.ResultMap;
import org.apache.ibatis.mapping.ResultMapping;
import org.apache.ibatis.mapping.SqlCommandType;
import org.apache.ibatis.mapping.SqlSource;
import org.apache.ibatis.session.Configuration;

import lombok.extern.slf4j.Slf4j;
import tech.codingless.biz.core.plugs.mybaties3.CommonSQLHelper;
import tech.codingless.biz.core.plugs.mybaties3.MyColumn;
import tech.codingless.biz.core.util.StringUtil;

@Slf4j
public class AutoGetHelper {

	public static void genAutoSqlForGet(Class<?> clazz, boolean userCompanyId, Configuration configuration) {
		MyTypeHanderRegistHelper.regist(configuration, clazz);

		String sqlKey = null;
		if (userCompanyId) {
			sqlKey = "AUTOSQL.GET_BYCOMPANYID_" + CommonSQLHelper.getTableName(clazz);
		} else {
			sqlKey = "AUTOSQL.GET_" + CommonSQLHelper.getTableName(clazz);
		}

		SqlSourceBuilder sqlSourceBuilder = new SqlSourceBuilder(configuration);
		String getSQL = userCompanyId ? CommonSQLHelper.getGetSQLByCompanyId(clazz) : CommonSQLHelper.getGetSQL(clazz);
		SqlSource sqlSource = sqlSourceBuilder.parse(getSQL, clazz, null);
		MappedStatement.Builder builder = new MappedStatement.Builder(configuration, sqlKey, sqlSource, SqlCommandType.SELECT);
		List<ResultMapping> mappingList = new ArrayList<ResultMapping>();
		List<ResultMap> resultMapList = new ArrayList<ResultMap>();
		// 设置返回值绑定
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
			try {
				Field filed = clazz.getDeclaredField(attrName);
				MyColumn myColumn = filed.getAnnotation(MyColumn.class);
				if (myColumn != null && StringUtil.isNotEmpty(myColumn.name())) {
					columnName = myColumn.name();
				}
			} catch (Exception e1) {

			}
			if (StringUtil.isEmpty(columnName)) {
				columnName = CommonSQLHelper.change2dbFormat(attrName);
			}
			ResultMapping.Builder mappingBuilder = new ResultMapping.Builder(configuration, attrName);
			mappingBuilder.javaType(method.getReturnType());
			mappingBuilder.column(columnName);
			mappingList.add(mappingBuilder.build());
		}

		ResultMap.Builder mapBuilder = new ResultMap.Builder(configuration, "AUTOSQL.GET_MAP_" + clazz.getSimpleName(), clazz, mappingList);

		resultMapList.add(mapBuilder.build());
		builder.resultMaps(resultMapList);
		try {
			configuration.addMappedStatement(builder.build());
		} catch (Exception e) {
			log.error("genAutoSqlForGet Fail", e);
		}

	}

}
