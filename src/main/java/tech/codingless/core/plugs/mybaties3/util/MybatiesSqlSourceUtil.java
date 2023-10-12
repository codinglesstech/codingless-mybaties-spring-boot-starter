package tech.codingless.core.plugs.mybaties3.util;

import java.io.ByteArrayInputStream;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.ibatis.builder.xml.XMLMapperEntityResolver;
import org.apache.ibatis.mapping.SqlSource;
import org.apache.ibatis.parsing.XNode;
import org.apache.ibatis.parsing.XPathParser;
import org.apache.ibatis.scripting.xmltags.XMLScriptBuilder;
import org.apache.ibatis.session.Configuration;

public class MybatiesSqlSourceUtil {
	private static ConcurrentHashMap<String, SqlSource> SQL_CACHE = new ConcurrentHashMap<>();
	private static final String XML_INSERT_1 = "<?xml version=\"1.0\" encoding=\"UTF-8\" ?>\r\n"
			+ "<!DOCTYPE mapper PUBLIC \"-//mybatis.org//DTD Mapper 3.0//EN\" \"http://mybatis.org/dtd/mybatis-3-mapper.dtd\">\r\n" + "\r\n" + "<mapper namespace=\"MY_SQL\"> \r\n"
			+ "    <insert id=\"MY_SQL_ID\"   parameterType=\"map\">";

	private static final String XML_INSERT_2 = "    </insert> \r\n" + "</mapper>";

	public static SqlSource exchangeInsertSqlSource(String xmlInsertSql, Map<String, Object> param) throws Exception {
		String key = MybatiesStringUtil.md5("INSERT:" + xmlInsertSql);
		SqlSource sql = SQL_CACHE.get(key);
		if (sql == null) {
			Configuration config = new Configuration();
			String xml = XML_INSERT_1 + xmlInsertSql + XML_INSERT_2;
			Properties properties = new Properties();
			XPathParser xpath = new XPathParser(new ByteArrayInputStream(xml.getBytes("utf-8")), true, properties, new XMLMapperEntityResolver());
			List<XNode> selects = xpath.evalNode("/mapper").evalNodes("insert");
			XNode xnode = selects.get(0);// xpath.evalNodes("select|insert|update|delete");
			XMLScriptBuilder xmlscript = new XMLScriptBuilder(config, xnode);
			sql = xmlscript.parseScriptNode();
			SQL_CACHE.put(key, sql);
		}
		return sql;
	}

	private static final String XML_UPDATE_1 = "<?xml version=\"1.0\" encoding=\"UTF-8\" ?>\r\n"
			+ "<!DOCTYPE mapper PUBLIC \"-//mybatis.org//DTD Mapper 3.0//EN\" \"http://mybatis.org/dtd/mybatis-3-mapper.dtd\">\r\n" + "\r\n" + "<mapper namespace=\"TEST\"> \r\n"
			+ "    <update id=\"test\"   parameterType=\"map\">";

	private static final String XML_UPDATE_2 = "    </update> \r\n" + "</mapper>";

	public static SqlSource exchangeUpdateSqlSource(String xmlUpdateSql, Map<String, Object> param) throws Exception {
		String key = MybatiesStringUtil.md5("UDPATE:" + xmlUpdateSql);
		SqlSource sql = SQL_CACHE.get(key);
		if (sql == null) {
			Configuration config = new Configuration();
			String xml = XML_UPDATE_1 + xmlUpdateSql + XML_UPDATE_2;
			Properties properties = new Properties();
			XPathParser xpath = new XPathParser(new ByteArrayInputStream(xml.getBytes("utf-8")), true, properties, new XMLMapperEntityResolver());
			List<XNode> selects = xpath.evalNode("/mapper").evalNodes("update");
			XNode xnode = selects.get(0);// xpath.evalNodes("select|insert|update|delete");
			XMLScriptBuilder xmlscript = new XMLScriptBuilder(config, xnode);
			sql = xmlscript.parseScriptNode();
			SQL_CACHE.put(key, sql);
		}
		return sql;
	}

	private static final String XML_DELETE_1 = "<?xml version=\"1.0\" encoding=\"UTF-8\" ?>\r\n"
			+ "<!DOCTYPE mapper PUBLIC \"-//mybatis.org//DTD Mapper 3.0//EN\" \"http://mybatis.org/dtd/mybatis-3-mapper.dtd\">\r\n" + "\r\n" + "<mapper namespace=\"TEST\"> \r\n"
			+ "    <delete id=\"test\"   parameterType=\"map\">";

	private static final String XML_DELETE_2 = "    </delete> \r\n" + "</mapper>";

	public static SqlSource exchangeDeleteSqlSource(String xmlDeleteSql, Map<String, Object> param) throws Exception {
		String key = MybatiesStringUtil.md5("DELETE:" + xmlDeleteSql);
		SqlSource sql = SQL_CACHE.get(key);
		if (sql == null) {
			Configuration config = new Configuration();
			String xml = XML_DELETE_1 + xmlDeleteSql + XML_DELETE_2;
			Properties properties = new Properties();
			XPathParser xpath = new XPathParser(new ByteArrayInputStream(xml.getBytes("utf-8")), true, properties, new XMLMapperEntityResolver());
			List<XNode> selects = xpath.evalNode("/mapper").evalNodes("delete");
			XNode xnode = selects.get(0);// xpath.evalNodes("select|insert|update|delete");
			XMLScriptBuilder xmlscript = new XMLScriptBuilder(config, xnode);
			sql = xmlscript.parseScriptNode();
			SQL_CACHE.put(key, sql);
		}
		return sql;
	}

}
