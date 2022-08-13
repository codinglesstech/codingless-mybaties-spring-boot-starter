package tech.codingless.core.plugs.mybaties3.helper;

import java.io.ByteArrayInputStream;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;

import org.apache.ibatis.builder.xml.XMLMapperBuilder;
import org.apache.ibatis.session.Configuration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 
 * 批量逻辑删除
 * 
 * @author 王鸿雁
 * @version 2021年11月17日
 */
public class AutoDeleteLogicalBatchHelper {
	private static final Logger LOG = LoggerFactory.getLogger(AutoDeleteLogicalBatchHelper.class);
	private static final String QUOTATION = "\"";
	private static final String XML_VERSION = "<?xml version=\"1.0\" encoding=\"UTF-8\" ?>";
	private static final String XML_DOCTYPE = "<!DOCTYPE mapper PUBLIC \"-//mybatis.org//DTD Mapper 3.0//EN\" \"http://mybatis.org/dtd/mybatis-3-mapper.dtd\">";

	public static void genSql(Configuration configuration, String namespace, String sqlKey, Class<?> clazz) {

		StringBuilder batchSqlBuilder = new StringBuilder();

		batchSqlBuilder.append(XML_VERSION);
		batchSqlBuilder.append(XML_DOCTYPE);
		batchSqlBuilder.append("<mapper namespace=").append(QUOTATION).append(namespace).append(QUOTATION).append(">");
		batchSqlBuilder.append("<update id=").append(QUOTATION).append(sqlKey).append(QUOTATION);
		batchSqlBuilder.append("  parameterType=\"map\" >");
		batchSqlBuilder.append("update ").append(CommonSQLHelper.getTableName(clazz)).append(" set del=true,ver=ver+1  where company_id=#{companyId} and id in ");
		batchSqlBuilder.append("<foreach collection=\"idList\" separator=\",\" item=\"item\" open=\"(\" close=\")\">");
		batchSqlBuilder.append(" #{item}");
		batchSqlBuilder.append("</foreach></update>");
		batchSqlBuilder.append("</mapper> ");
		try {
			System.out.println("==============================");
			System.out.println(batchSqlBuilder.toString());

			XMLMapperBuilder selectMapperBuilder = new XMLMapperBuilder(new ByteArrayInputStream(batchSqlBuilder.toString().getBytes("utf-8")), configuration, sqlKey, new HashMap<>());
			selectMapperBuilder.parse();
		} catch (UnsupportedEncodingException e) {
			LOG.error("AutoCreateBatchHelper", e);
		}
	}

}
