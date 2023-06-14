package tech.codingless.core.plugs.mybaties3.helper;

import java.io.ByteArrayInputStream;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;

import org.apache.ibatis.builder.xml.XMLMapperBuilder;
import org.apache.ibatis.session.Configuration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AutoUpinsertBatchHelper {
	private static final Logger LOG = LoggerFactory.getLogger(AutoUpinsertBatchHelper.class);
	private static final String QUOTATION = "\"";
	private static final String XML_VERSION = "<?xml version=\"1.0\" encoding=\"UTF-8\" ?>";
	private static final String XML_DOCTYPE = "<!DOCTYPE mapper PUBLIC \"-//mybatis.org//DTD Mapper 3.0//EN\" \"http://mybatis.org/dtd/mybatis-3-mapper.dtd\">";

	public static void genBatchCreateSql(Configuration configuration, String namespace, String sqlKey, Class<?> clazz) {
		// 添加typeHandlers
		MyTypeHanderRegistHelper.regist(configuration, clazz);
		// 生产批量创建 sqlmap 模版
		StringBuffer columnBuilder = new StringBuffer();
		StringBuffer valueBuilder = new StringBuffer();
		StringBuffer batchSqlBuilder = new StringBuffer();
		StringBuffer setBuilder = new StringBuffer();

		ColumnHelper.parse(clazz).eachInsertColumn(column -> {

			columnBuilder.append(column.getColumnName()).append(",");
			valueBuilder.append("#{item.").append(column.getAttrName()).append("},");
		}).eachUpdateColumn(column -> {

			setBuilder.append("<if test=\"").append("item.").append(column.getAttrName()).append(" != null\">").append(column.getColumnName()).append("=#{item.").append(column.getAttrName())
					.append("},");
			setBuilder.append("</if>");
		});

		columnBuilder.append("gmt_write").append(",");
		columnBuilder.append("gmt_create").append(",");
		columnBuilder.append("ver");
		valueBuilder.append("UNIX_TIMESTAMP(),");
		valueBuilder.append("UNIX_TIMESTAMP(),");
		valueBuilder.append("1");
		setBuilder.append("gmt_write=UNIX_TIMESTAMP(),ver=ver+1");

		batchSqlBuilder.append(XML_VERSION);
		batchSqlBuilder.append(XML_DOCTYPE);
		batchSqlBuilder.append("<mapper namespace=").append(QUOTATION).append(namespace).append(QUOTATION).append(">");
		batchSqlBuilder.append("<insert id=").append(QUOTATION).append(sqlKey).append(QUOTATION);
		batchSqlBuilder.append("  parameterType=\"list\" >");

		batchSqlBuilder.append("<foreach collection=\"list\" separator=\";\" item=\"item\">");
		batchSqlBuilder.append("INSERT INTO ").append(CommonSQLHelper.getTableName(clazz));
		batchSqlBuilder.append(" (");
		batchSqlBuilder.append(columnBuilder.toString());
		batchSqlBuilder.append(")VALUES (");
		batchSqlBuilder.append(valueBuilder.toString());
		batchSqlBuilder.append(") on duplicate key update ");
		batchSqlBuilder.append(setBuilder.toString());
		batchSqlBuilder.append("</foreach></insert>");
		batchSqlBuilder.append("</mapper> ");
		try {
			XMLMapperBuilder selectMapperBuilder = new XMLMapperBuilder(new ByteArrayInputStream(batchSqlBuilder.toString().getBytes("utf-8")), configuration, sqlKey, new HashMap<>());
			selectMapperBuilder.parse();
		} catch (UnsupportedEncodingException e) {
			LOG.error("AutoCreateBatchHelper", e);
		}
	}

}
