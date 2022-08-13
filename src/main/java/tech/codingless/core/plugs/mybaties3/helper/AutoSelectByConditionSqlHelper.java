package tech.codingless.core.plugs.mybaties3.helper;

import java.io.ByteArrayInputStream;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;

import org.apache.ibatis.builder.xml.XMLMapperBuilder;
import org.apache.ibatis.session.Configuration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import tech.codingless.core.plugs.mybaties3.data.BaseDO;
 
/**
 * 
 *  
 * 这是一段非常有用的代码, 动态条件，动态返回值
 * @author 王鸿雁
 * @version  2021年11月20日
 */
public class AutoSelectByConditionSqlHelper {
	private static final Logger LOG = LoggerFactory.getLogger(AutoSelectByConditionSqlHelper.class);
	private static final String QUOTATION = "\"";
	private static final String XML_VERSION = "<?xml version=\"1.0\" encoding=\"UTF-8\" ?>";
	private static final String XML_DOCTYPE = "<!DOCTYPE mapper PUBLIC \"-//mybatis.org//DTD Mapper 3.0//EN\" \"http://mybatis.org/dtd/mybatis-3-mapper.dtd\">";

	public static void genSql(Configuration configuration, String namespace, String sqlKey, Class<? extends BaseDO> clazz,boolean selectSql,boolean countSql) {

		String mapId = "SelectByConditionResultMap" + clazz.getSimpleName();
		 
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
		if(selectSql) { 
			columnBuilder.append("<if test=\"columns==null or columns.size()==0\"> * </if>");
			columnBuilder.append("<if test=\"columns!=null and columns.size()>0\"> <foreach item=\"column\" collection=\"columns\" index=\"index\"    separator=\",\" > ${column} </foreach> </if>");
		}
		
		if(countSql) {
			
			columnBuilder.append("count(1) as rows");
			
		}
		
 
		
		
		
		StringBuffer sqlBuilder = new StringBuffer();
		sqlBuilder.append(XML_VERSION);
		sqlBuilder.append(XML_DOCTYPE);
		sqlBuilder.append("<mapper namespace=").append(QUOTATION).append(namespace).append(QUOTATION).append(">");
		
		if(selectSql) { 
			sqlBuilder.append(resultMapSb.toString());
		}
		
		sqlBuilder.append("<select id=").append(QUOTATION).append(sqlKey).append(QUOTATION);
		if(selectSql) { 
			sqlBuilder.append(" resultMap=\"" + mapId + "\"  parameterType=\"map\" >");
		}
		if(countSql) {
			sqlBuilder.append(" resultType=\"int\"  parameterType=\"map\" >"); 
		}
		sqlBuilder.append("select ").append(columnBuilder.toString()).append(" from ").append(CommonSQLHelper.getTableName(clazz));
		sqlBuilder.append(" <where>");
		sqlBuilder.append("<if test=\"conditions!=null\"><foreach  item=\"condition\" collection=\"conditions\" index=\"index\"    separator=\" and\"  >");
		sqlBuilder.append("<if test=\"condition.type=='eq'\"> ${condition.columnName}= #{condition.value}   </if>");
		sqlBuilder.append("<if test=\"condition.type=='gt'\"> ${condition.columnName} &gt; #{condition.value}   </if>");
		sqlBuilder.append("<if test=\"condition.type=='lt'\"> ${condition.columnName} &lt; #{condition.value}   </if>");
		
		//is 
		sqlBuilder.append("<if test=\"condition.type=='is' and condition.value==true \"> ${condition.columnName} is true   </if>");
		sqlBuilder.append("<if test=\"condition.type=='is' and condition.value==false \"> ${condition.columnName} is false   </if>");
		
		//in
		sqlBuilder.append("<if test=\"condition.type=='in'\"> ${condition.columnName} in ");
		
		sqlBuilder.append("<foreach  item=\"val\" collection=\"condition.values\" index=\"index\"  open=\"(\" separator=\",\" close=\")\">");
		sqlBuilder.append("#{val}");
		sqlBuilder.append("</foreach>");
		
		sqlBuilder.append("</if>"); 
		sqlBuilder.append("</foreach>");
		sqlBuilder.append("</if>");
		sqlBuilder.append("</where> ");
		
		if(selectSql) { 
			sqlBuilder.append(" limit #{limit}  offset #{offset}");
		}
		sqlBuilder.append("</select>");
		sqlBuilder.append("</mapper> ");
		try { 
			XMLMapperBuilder selectMapperBuilder = new XMLMapperBuilder(new ByteArrayInputStream(sqlBuilder.toString().getBytes("utf-8")), configuration, sqlKey, new HashMap<>());
			selectMapperBuilder.parse();
		} catch (UnsupportedEncodingException e) {
			LOG.error("AutoCreateBatchHelper", e);
		}
	}

	public static void gen(Configuration configuration, String namespace, String sqlKey, Class<? extends BaseDO> clazz)
	{
		 genSql(configuration, namespace, sqlKey, clazz, true, false);
	}
	public static void genCount(Configuration configuration, String namespace, String sqlKey, Class<? extends BaseDO> clazz) {
		
		genSql(configuration, namespace, sqlKey, clazz, false, true); 
	}

  

}
