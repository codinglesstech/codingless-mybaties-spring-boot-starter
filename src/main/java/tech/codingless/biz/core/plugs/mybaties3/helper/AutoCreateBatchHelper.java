package tech.codingless.biz.core.plugs.mybaties3.helper;

import java.io.ByteArrayInputStream;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.HashMap;

import org.apache.ibatis.builder.xml.XMLMapperBuilder;
import org.apache.ibatis.session.Configuration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import tech.codingless.biz.core.plugs.mybaties3.CommonSQLHelper;
import tech.codingless.biz.core.plugs.mybaties3.annotation.MyColumn;
import tech.codingless.biz.core.plugs.mybaties3.util.MybatiesStringUtil;

 

public class AutoCreateBatchHelper {
	private static final Logger LOG = LoggerFactory.getLogger(AutoCreateBatchHelper.class);
	private static final String QUOTATION = "\"";
	private static final String XML_VERSION = "<?xml version=\"1.0\" encoding=\"UTF-8\" ?>";
	private static final String XML_DOCTYPE = "<!DOCTYPE mapper PUBLIC \"-//mybatis.org//DTD Mapper 3.0//EN\" \"http://mybatis.org/dtd/mybatis-3-mapper.dtd\">";

	public static void genBatchCreateSql(Configuration configuration, String namespace, String sqlKey, Class<?> clazz) {		
		//添加typeHandlers  
		MyTypeHanderRegistHelper.regist(configuration,clazz);
		//生产批量创建 sqlmap 模版
		StringBuffer columnBuilder = new StringBuffer();
		StringBuffer valueBuilder = new StringBuffer();
		StringBuffer batchSqlBuilder = new StringBuffer();
		for (Method method : clazz.getMethods()) {
			String methodName = method.getName();
			if (MyTableColumnParser.needSkipMethodName(methodName)) {
				continue;
			}
			String attrName = MyTableColumnParser.methodName2attrName(methodName);
			if ("gmtWrite".equals(attrName) || "gmtCreate".equals(attrName)) {
				continue;
			}
			String columnName = null;
			try {
				Field filed = clazz.getDeclaredField(attrName);
				MyColumn myColumn = filed.getAnnotation(MyColumn.class);
				if(myColumn!=null&&myColumn.virtual()) {
					continue;
				}
				if (myColumn != null && MybatiesStringUtil.isNotEmpty(myColumn.name())) {
					columnName = myColumn.name();
				}
			} catch (Exception e1) {
			}
			if (MybatiesStringUtil.isEmpty(columnName)) {
				columnName = CommonSQLHelper.change2dbFormat(attrName);
			}
			columnBuilder.append(columnName).append(",");
			valueBuilder.append("#{item.").append(attrName).append("},");
		}
		columnBuilder.append("gmt_Write").append(","); 
		columnBuilder.append("gmt_Create");
		valueBuilder.append("now(),");
		valueBuilder.append("now()");

		batchSqlBuilder.append(XML_VERSION);
		batchSqlBuilder.append(XML_DOCTYPE);
		batchSqlBuilder.append("<mapper namespace=").append(QUOTATION).append(namespace).append(QUOTATION).append(">"); 
		batchSqlBuilder.append("<insert id=").append(QUOTATION).append(sqlKey).append(QUOTATION);
		batchSqlBuilder.append("  parameterType=\"list\" >");
		batchSqlBuilder.append("INSERT INTO ").append(CommonSQLHelper.getTableName(clazz));
		batchSqlBuilder.append(" (");
		batchSqlBuilder.append(columnBuilder.toString());
		batchSqlBuilder.append(")VALUES <foreach collection=\"list\" separator=\",\" item=\"item\">(");
		batchSqlBuilder.append(valueBuilder.toString());
		batchSqlBuilder.append(")</foreach></insert>");
		batchSqlBuilder.append("</mapper> "); 
		try { 
			XMLMapperBuilder selectMapperBuilder = new XMLMapperBuilder(new ByteArrayInputStream(batchSqlBuilder.toString().getBytes("utf-8")), configuration, sqlKey, new HashMap<>());
			selectMapperBuilder.parse();
		} catch (UnsupportedEncodingException e) {
			LOG.error("AutoCreateBatchHelper", e);
		}
	}

}
