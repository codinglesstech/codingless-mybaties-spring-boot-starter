package tech.codingless.core.plugs.mybaties3.helper;

import java.io.ByteArrayInputStream;
import java.io.UnsupportedEncodingException;
import java.util.Collection;
import java.util.HashMap;

import org.apache.ibatis.builder.xml.XMLMapperBuilder;
import org.apache.ibatis.session.Configuration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import tech.codingless.core.plugs.mybaties3.data.BaseDO;
 

public class AutoFindByIdHelper {
	private static final Logger LOG = LoggerFactory.getLogger(AutoFindByIdHelper.class);
	private static final String QUOTATION = "\"";
	private static final String XML_VERSION = "<?xml version=\"1.0\" encoding=\"UTF-8\" ?>";
	private static final String XML_DOCTYPE = "<!DOCTYPE mapper PUBLIC \"-//mybatis.org//DTD Mapper 3.0//EN\" \"http://mybatis.org/dtd/mybatis-3-mapper.dtd\">";

	public static void genGetSql(Configuration configuration, String namespace, String sqlKey, Class<?> clazz, Collection<String> columns) {

		String mapId = "GetByIdResultMap" + clazz.getSimpleName();
		StringBuffer resultMapSb = new StringBuffer();
		resultMapSb.append("<resultMap type=\"").append(clazz.getTypeName()).append("\"");
		resultMapSb.append(" id=\"" + mapId + "\" >");

		// 设置返回值绑定
		MyTableColumnParser.parse(clazz).forEach(columnProp -> {
			resultMapSb.append("<result column=\"").append(columnProp.getColumn()).append("\" property=\"").append(columnProp.getProp()).append("\" />");
		});
		resultMapSb.append("</resultMap>");

		
		StringBuilder columnBuilder = new StringBuilder();
		
		//以支持动态列 
		columnBuilder.append("<if test=\"columns==null or columns.size()==0\"> * </if>");
		columnBuilder.append("<if test=\"columns!=null and columns.size()>0\"> <foreach item=\"column\" collection=\"columns\" index=\"index\"    separator=\",\" > ${column} </foreach> </if>");
		
		
		
		StringBuffer batchSqlBuilder = new StringBuffer();
		batchSqlBuilder.append(XML_VERSION);
		batchSqlBuilder.append(XML_DOCTYPE);
		batchSqlBuilder.append("<mapper namespace=").append(QUOTATION).append(namespace).append(QUOTATION).append(">");
		batchSqlBuilder.append(resultMapSb.toString());
		batchSqlBuilder.append("<select id=").append(QUOTATION).append(sqlKey).append(QUOTATION);
		batchSqlBuilder.append(" resultMap=\"" + mapId + "\"  parameterType=\"map\" >");
		batchSqlBuilder.append("select ").append(columnBuilder.toString()).append(" from ").append(CommonSQLHelper.getTableName(clazz));
		batchSqlBuilder.append(" where  id=#{id} and company_id=#{companyId}  and not del and env=#{env} "); 
		batchSqlBuilder.append("</select>");
		batchSqlBuilder.append("</mapper> ");
		try {
			 
			XMLMapperBuilder selectMapperBuilder = new XMLMapperBuilder(new ByteArrayInputStream(batchSqlBuilder.toString().getBytes("utf-8")), configuration, sqlKey, new HashMap<>());
			selectMapperBuilder.parse();
		} catch (UnsupportedEncodingException e) {
			LOG.error("AutoFindByIdHelper", e);
		}
	}

	public static void genGetSql(Configuration configuration, String namespace, String sqlKey, Class<?> clazz) {
		genGetSql(configuration,namespace,sqlKey,clazz,null);
		
	}

	public static void genGetSql(Configuration configuration, Class<? extends BaseDO> clazz) {
		String sqlKey = "GET_BYCOMPANYID_" + CommonSQLHelper.getTableName(clazz);
		String namespace="AUTOSQL";
		genGetSql(configuration, namespace, sqlKey, clazz);
	}

}
