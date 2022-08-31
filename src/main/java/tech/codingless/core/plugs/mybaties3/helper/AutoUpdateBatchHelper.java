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
 * 批量更新
 * @author 王鸿雁
 * @version  2021年10月23日
 */
public class AutoUpdateBatchHelper {
	private static final Logger LOG = LoggerFactory.getLogger(AutoUpdateBatchHelper.class);
	private static final String QUOTATION = "\"";
	private static final String XML_VERSION = "<?xml version=\"1.0\" encoding=\"UTF-8\" ?>";
	private static final String XML_DOCTYPE = "<!DOCTYPE mapper PUBLIC \"-//mybatis.org//DTD Mapper 3.0//EN\" \"http://mybatis.org/dtd/mybatis-3-mapper.dtd\">";

	public static void genBatchUpdateSql(Configuration configuration, String namespace, String sqlKey, Class<?> clazz) {		
 
		//生产批量创建 sqlmap 模版 
		StringBuilder updateSqlBuffer = new StringBuilder(); 
		StringBuilder batchSqlBuilder = new StringBuilder();
		  
		
		
		updateSqlBuffer.append("update ").append(CommonSQLHelper.getTableName(clazz)).append(" set ");
		MyTableColumnParser.parse(clazz).forEach(column->{ 
			if(MyTableColumnParser.needSkipProperties(column.getProp())) {
				return ;
			}
			updateSqlBuffer.append("<if test=\"item.updateDO."+column.getProp()+" != null\"> "+column.getColumn()+"= #{item.updateDO."+column.getProp()+"},</if>"); 
		}); 
		updateSqlBuffer.append(" gmt_write=UNIX_TIMESTAMP(),ver=ver+1 ");
		updateSqlBuffer.append(" where id=#{item.id} and company_id=#{item.companyId} and ver=#{item.ver}");
		 
		
		
		
		batchSqlBuilder.append(XML_VERSION);
		batchSqlBuilder.append(XML_DOCTYPE);
		batchSqlBuilder.append("<mapper namespace=").append(QUOTATION).append(namespace).append(QUOTATION).append(">"); 
		batchSqlBuilder.append("<update id=").append(QUOTATION).append(sqlKey).append(QUOTATION);
		batchSqlBuilder.append("  parameterType=\"list\" >"); 
		batchSqlBuilder.append("<foreach collection=\"list\" separator=\";\" item=\"item\">");
		batchSqlBuilder.append(updateSqlBuffer.toString());
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
