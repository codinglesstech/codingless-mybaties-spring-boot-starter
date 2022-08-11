package tech.codingless.biz.core.plugs.mybaties3.helper;

import java.io.ByteArrayInputStream;
import java.util.HashMap;
import java.util.List;

import org.apache.ibatis.builder.xml.XMLMapperBuilder;
import org.apache.ibatis.parsing.XNode;
import org.apache.ibatis.parsing.XPathParser;
import org.apache.ibatis.session.Configuration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import tech.codingless.biz.core.plugs.mybaties3.util.MybatiesStringUtil;
import tech.codingless.biz.core.plugs.mybaties3.util.XmlMapperUtil;

 

/**
 * 
 * @author 王鸿雁
 * @version 2020年11月14日
 */
public class AutoRollPageSelectSqlHelper {
	private static final Logger LOG = LoggerFactory.getLogger(AutoRollPageSelectSqlHelper.class);
	private static final String SPLIT_FROM = "[ \t\n]*[fF]{1}[rR]{1}[oO]{1}[mM]{1}[ \t\n]*"; 
	private static final String QUOTATION = "\"";
	private static final String XML_VERSION = "<?xml version=\"1.0\" encoding=\"UTF-8\" ?>";
	private static final String XML_DOCTYPE = "<!DOCTYPE mapper PUBLIC \"-//mybatis.org//DTD Mapper 3.0//EN\" \"http://mybatis.org/dtd/mybatis-3-mapper.dtd\">";

	public static void genSelectMapper(String namespance, String oldId, String selectKey, String countKey, Configuration configuration, String oldMapperStr) {

		String oldSql = XmlMapperUtil.fetchSelectSqlById(oldMapperStr, oldId);
		XPathParser parser = new XPathParser(oldMapperStr);
		List<XNode> nodeList = parser.evalNodes("/mapper/select");
		XNode selectNode = null;
		for (XNode node : nodeList) {
			if (oldId.equals(node.getStringAttribute("id"))) {
				selectNode = node;
			}
		}

		// Create New Select Sql
		String newSelectSql = new String(oldSql);
		newSelectSql += " limit #{_limit_}  offset #{_offset_}";
		StringBuilder selectXml = new StringBuilder(XML_VERSION);
		selectXml.append(XML_DOCTYPE);
		selectXml.append("<mapper namespace=\"").append(namespance.substring(0, namespance.length() - 1)).append("\">");
		selectXml.append("<select id=\"").append(selectKey).append(QUOTATION).append(" parameterType=\"map\" ");
		String resultMap = selectNode.getStringAttribute("resultMap");
		if (MybatiesStringUtil.isNotEmpty(resultMap)) {
			selectXml.append(" resultMap=").append(QUOTATION).append(resultMap).append(QUOTATION);
		}
		String resultType = selectNode.getStringAttribute("resultType");
		if (MybatiesStringUtil.isNotEmpty(resultType)) {
			selectXml.append(" resultType=").append(QUOTATION).append(resultType).append(QUOTATION);
		}
		selectXml.append(" >");
		selectXml.append(newSelectSql);
		selectXml.append("</select>");
		selectXml.append("</mapper> ");
		// LOG.info("New RollPage Select Sql:" + selectXml.toString());

		// Create New Count Sql
		String newCountSql = new String(oldSql);

		if (oldSql.toLowerCase().contains("group ")) {
			newCountSql = "select count(1) as total_count from (" + oldSql + ") as t";
		} else {
			newCountSql = "select count(1) as total_count from " + newCountSql.split(SPLIT_FROM, 2)[1];
		}

		StringBuilder countXml = new StringBuilder(XML_VERSION);
		countXml.append(XML_DOCTYPE);
		countXml.append("<mapper namespace=\"").append(namespance.substring(0, namespance.length() - 1)).append("\">");
		countXml.append("<select id=\"").append(countKey).append(QUOTATION).append(" parameterType=\"map\" ");
		countXml.append(" resultType=").append(QUOTATION).append("int").append(QUOTATION);
		countXml.append(" >");
		countXml.append(newCountSql);
		countXml.append("</select>");
		countXml.append("</mapper> ");
		// LOG.info("New Count Sql:" + countXml.toString());

		try {
			XMLMapperBuilder selectMapperBuilder = new XMLMapperBuilder(new ByteArrayInputStream(selectXml.toString().getBytes("utf-8")), configuration, namespance.concat(selectKey), new HashMap<>());
			selectMapperBuilder.parse();
			XMLMapperBuilder countMapperBuilder = new XMLMapperBuilder(new ByteArrayInputStream(countXml.toString().getBytes("utf-8")), configuration, namespance.concat(countKey), new HashMap<>());
			countMapperBuilder.parse();
		} catch (Exception e) {
			LOG.error("", e);
		}

	}

}
