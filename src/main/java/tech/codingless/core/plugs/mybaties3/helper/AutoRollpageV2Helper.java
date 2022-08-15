package tech.codingless.core.plugs.mybaties3.helper;

import java.io.ByteArrayInputStream;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;

import org.apache.ibatis.builder.xml.XMLMapperBuilder;
import org.apache.ibatis.session.Configuration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import tech.codingless.core.plugs.mybaties3.data.BaseDO;

public class AutoRollpageV2Helper {
	private static final Logger LOG = LoggerFactory.getLogger(AutoRollpageV2Helper.class);
	private static final String QUOTATION = "\"";
	private static final String XML_VERSION = "<?xml version=\"1.0\" encoding=\"UTF-8\" ?>";
	private static final String XML_DOCTYPE = "<!DOCTYPE mapper PUBLIC \"-//mybatis.org//DTD Mapper 3.0//EN\" \"http://mybatis.org/dtd/mybatis-3-mapper.dtd\">";

	public static void genSql(Configuration configuration, String namespace, String sqlKey, Class<?> clazz) {

		String mapId = "RollpageResultMap" + clazz.getSimpleName();
		StringBuffer result = new StringBuffer();
		result.append("<resultMap type=\"").append(clazz.getTypeName()).append("\"");
		result.append(" id=\"" + mapId + "\" >");

		StringBuilder where = new StringBuilder();
		where.append(" <where> ");

		// 设置返回值绑定
		MyTableColumnParser.parse(clazz).forEach(columnProp -> {
			if (columnProp.isVirturl()) {
				return;
			}
			result.append("<result column=\"").append(columnProp.getColumn()).append("\" property=\"").append(columnProp.getProp()).append("\" />");
			where.append("<if test=\"condition." + columnProp.getProp() + "!=null and condition." + columnProp.getProp() + "!='' \">  and " + columnProp.getColumn() + " = #{condition."
					+ columnProp.getProp() + "} </if>");

		});

		where.append("</where> ");
		result.append("</resultMap>");

		StringBuilder columns = new StringBuilder();

		// 以支持动态列
		columns.append("<if test=\"columns==null or columns.size()==0\"> * </if>");
		columns.append("<if test=\"columns!=null and columns.size()>0\"> <foreach item=\"column\" collection=\"columns\" index=\"index\"    separator=\",\" > ${column} </foreach> </if>");

		StringBuffer sql = new StringBuffer();
		sql.append(XML_VERSION);
		sql.append(XML_DOCTYPE);
		sql.append("<mapper namespace=").append(QUOTATION).append(namespace).append(QUOTATION).append(">");
		sql.append(result.toString());
		sql.append("<select id=").append(QUOTATION).append(sqlKey).append(QUOTATION);
		sql.append(" resultMap=\"" + mapId + "\"  parameterType=\"map\" >");
		sql.append("select ").append(columns.toString()).append(" from ").append(CommonSQLHelper.getTableName(clazz));
		sql.append(where.toString());
		sql.append("</select>");
		sql.append("</mapper> ");
		try {

			XMLMapperBuilder selectMapperBuilder = new XMLMapperBuilder(new ByteArrayInputStream(sql.toString().getBytes("utf-8")), configuration, sqlKey, new HashMap<>());
			selectMapperBuilder.parse();
		} catch (UnsupportedEncodingException e) {
			LOG.error("AutoRollpageV2Helper", e);
		}
	}
  
	public static void genSql(Configuration configuration, Class<? extends BaseDO> clazz) {
		String sqlKey = "findByExamplev2_" + CommonSQLHelper.getTableName(clazz);
		String namespace = "AUTOSQL";
		genSql(configuration, namespace, sqlKey, clazz);
	}

}
