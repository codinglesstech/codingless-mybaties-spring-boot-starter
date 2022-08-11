package tech.codingless.biz.core.plugs.mybaties3.helper;

import java.io.ByteArrayInputStream;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;

import org.apache.ibatis.builder.xml.XMLMapperBuilder;
import org.apache.ibatis.session.Configuration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import tech.codingless.biz.core.plugs.mybaties3.CommonSQLHelper;
import tech.codingless.biz.core.plugs.mybaties3.data.BaseDO;

 
/**
 * 
 * @author ASUS
 *
 */
public class AutoUpdateHelper {
	private static final String NAMESPACE="AUTOSQL";
	private static final Logger LOG = LoggerFactory.getLogger(AutoUpdateHelper.class);
	private static final String QUOTATION = "\"";
	private static final String XML_VERSION = "<?xml version=\"1.0\" encoding=\"UTF-8\" ?>";
	private static final String XML_DOCTYPE = "<!DOCTYPE mapper PUBLIC \"-//mybatis.org//DTD Mapper 3.0//EN\" \"http://mybatis.org/dtd/mybatis-3-mapper.dtd\">";

	public static void genUpdateSkipNullSql(Configuration configuration, String namespace, String sqlKey, Class<?> clazz) {		
  
		StringBuilder updateSqlBuffer = new StringBuilder(); 
		StringBuilder batchSqlBuilder = new StringBuilder();
		  
		 
		updateSqlBuffer.append("update ").append(CommonSQLHelper.getTableName(clazz)).append(" set ");
		MyTableColumnParser.parse(clazz).forEach(column->{ 
			if(MyTableColumnParser.needSkipProperties(column.getProp())||column.isReadonly()||column.isVirturl()) {
				return ;
			}
			
			updateSqlBuffer.append("<if test=\""+column.getProp()+" != null\"> "+column.getColumn()+"= #{"+column.getProp()+"},</if>"); 
		}); 
		updateSqlBuffer.append(" gmt_write=now(),ver=ver+1 ");
		updateSqlBuffer.append(" where id=#{id} and ver=#{ver} ");
		updateSqlBuffer.append("<if test=\"companyId!=null\"> and company_id = #{companyId}</if>");
		
		 
		batchSqlBuilder.append(XML_VERSION);
		batchSqlBuilder.append(XML_DOCTYPE);
		batchSqlBuilder.append("<mapper namespace=").append(QUOTATION).append(namespace).append(QUOTATION).append(">"); 
		batchSqlBuilder.append("<update id=").append(QUOTATION).append(sqlKey).append(QUOTATION);
		batchSqlBuilder.append("  parameterType=\"").append(clazz.getName()).append("\" >");  
		batchSqlBuilder.append(updateSqlBuffer.toString());
		batchSqlBuilder.append("</update>");
		batchSqlBuilder.append("</mapper> "); 
		try { 
	  
			XMLMapperBuilder selectMapperBuilder = new XMLMapperBuilder(new ByteArrayInputStream(batchSqlBuilder.toString().getBytes("utf-8")), configuration, sqlKey, new HashMap<>());
			selectMapperBuilder.parse();
		} catch (UnsupportedEncodingException e) {
			LOG.error("AutoCreateBatchHelper", e);
		}
	}

	public static void genUpdateSkipNullSql(Configuration configuration, Class<? extends BaseDO> clazz) { 
		String sqlId = "UPDATE_SKIP_NULLV2_" + CommonSQLHelper.getTableName(clazz);  
		genUpdateSkipNullSql(configuration, NAMESPACE, sqlId, clazz); 
		
	}

}
