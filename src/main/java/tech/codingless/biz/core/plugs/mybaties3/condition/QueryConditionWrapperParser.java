package tech.codingless.biz.core.plugs.mybaties3.condition;

import java.lang.reflect.Field;

import tech.codingless.biz.core.plugs.mybaties3.TableAutoCreateServiceMysqlImpl;
import tech.codingless.biz.core.plugs.mybaties3.helper.MyTableColumnParser;
import tech.codingless.biz.core.plugs.mybaties3.util.ReflectionUtil;
import tech.codingless.biz.core.plugs.mybaties3.util.MybatiesStringUtil;

public class QueryConditionWrapperParser {
 
	private static final String QUOTATION = "\"";
	private static final String XML_VERSION = "<?xml version=\"1.0\" encoding=\"UTF-8\" ?>";
	private static final String XML_DOCTYPE = "<!DOCTYPE mapper PUBLIC \"-//mybatis.org//DTD Mapper 3.0//EN\" \"http://mybatis.org/dtd/mybatis-3-mapper.dtd\">";
	public static String parse(QueryConditionWrapper<?> wrapper) {
		StringBuilder sqlBuilder = wrapper.build(); 
		String sql = sqlBuilder.toString().trim(); 
		if (sql.startsWith("and")) {
			sql = sql.substring(3).trim(); 
		} 
		if (sql.startsWith("(") && sql.endsWith(")")) { 
			sql = sql.substring(1,sql.length()-1).trim(); 
		} 
		if (sql.startsWith("and")) {
			sql = sql.substring(3).trim(); 
		}
		  
		return "<where> "+sql+"</where> ";
	}

	public static String parse(Class<?> clazz, ColumnSelector<? > columns, QueryConditionWrapper<?> wrapper) {
		StringBuilder sql = new StringBuilder();
		sql.append("select ");
		if(columns==null||columns.getColumns().isEmpty()) {
			sql.append(" * "); 
		}else {
			for(int i=0;i<columns.getColumns().size();i++) {
				Field filed = ReflectionUtil.findField(columns.getColumns().get(i));
				sql.append(TableAutoCreateServiceMysqlImpl.change2dbFormat(filed.getName()).toLowerCase());
				if(i<columns.getColumns().size()-1) {
					sql.append(",");	
				} 
			} 
		}
		sql.append(" from  ").append(TableAutoCreateServiceMysqlImpl.getTableName(clazz)).append(" ").append(parse(wrapper));  
		 
		return sql.toString();
	}
 
	
	public static String toXml(Class<?> clazz,String namespace,ColumnSelector<?> columns,String id,String sql) {
		
		String mapId = "selectByCondition" + clazz.getSimpleName()+MybatiesStringUtil.md5(id);
		StringBuffer rs = new StringBuffer();
		rs.append("<resultMap type=\"").append(clazz.getTypeName()).append("\"");
		rs.append(" id=\"" + mapId + "\" >");

		// 设置返回值绑定
		if(columns ==null ||columns.getColumns().isEmpty()) {
			MyTableColumnParser.parse(clazz).forEach(columnProp -> {
				rs.append("<result column=\"").append(columnProp.getColumn()).append("\" property=\"").append(columnProp.getProp()).append("\" />");
			});
		}else {
			columns.getColumns().forEach(action->{
				Field filed = ReflectionUtil.findField(action);
				rs.append("<result column=\"").append(TableAutoCreateServiceMysqlImpl.change2dbFormat(filed.getName()).toLowerCase()).append("\" property=\"").append(filed.getName()).append("\" />");
			});
		}
 

		
		
		rs.append("</resultMap>");
		  
		StringBuilder xml = new StringBuilder();
		xml.append(XML_VERSION);
		xml.append(XML_DOCTYPE);
		xml.append("<mapper namespace=").append(QUOTATION).append(namespace).append(QUOTATION).append(">"); 
		xml.append(rs); 
		xml.append("<select id=").append(QUOTATION).append(id).append(QUOTATION);
		xml.append(" resultMap=\"" + mapId + "\"  parameterType=\"map\" >");
		xml.append(sql); 
		xml.append("</select>");
		xml.append("</mapper> ");
		
		return xml.toString();
	}

	public static String parseCount(Class<?> clazz, QueryConditionWrapper<?> wrapper) {
		StringBuilder sql = new StringBuilder();
		sql.append("select count(1) from "); 
		sql.append(TableAutoCreateServiceMysqlImpl.getTableName(clazz)).append(" ").append(parse(wrapper));  
		 
		return sql.toString();
	}

	public static String toCountXml(Class<?> clazz, String namespace, String id, String sql) {
	  
		StringBuilder xml = new StringBuilder();
		xml.append(XML_VERSION);
		xml.append(XML_DOCTYPE);
		xml.append("<mapper namespace=").append(QUOTATION).append(namespace).append(QUOTATION).append(">");  
		xml.append("<select id=").append(QUOTATION).append(id).append(QUOTATION);
		xml.append(" resultType=\"long\"  parameterType=\"map\" >");
		xml.append(sql); 
		xml.append("</select>");
		xml.append("</mapper> ");
		
		return xml.toString();
	}

	 
}
