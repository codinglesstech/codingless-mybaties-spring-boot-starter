package tech.codingless.biz.core.plugs.mybaties3;

import java.io.ByteArrayInputStream;

import javax.annotation.Resource;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.session.Configuration;
import org.springframework.stereotype.Service;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import com.alibaba.csp.sentinel.util.StringUtil;

@Service
public class SqlMapperServiceImpl implements SqlMapperService {

	@Resource
	private MyBatiesService myBatiesService;
	private static final String EXTERNAL = "external.";

	@Override
	public boolean unload(String sqlId) {
		String namespace = sqlId.split("[.]")[0];
		String realSelectId1 = EXTERNAL + namespace + "." + (sqlId.replace(".", "-"));
		String realSelectId2 = sqlId.replace(".", "-");
		try {
			ConcurrentSqlCreatorLocker.remove(realSelectId1);
			Configuration conf = myBatiesService.getConfiguration();
			if (conf.getMappedStatementNames().contains(realSelectId1)) {
				MappedStatement ms = conf.getMappedStatement(realSelectId1);
				conf.getMappedStatements().remove(ms);
			}
			if (conf.getMappedStatementNames().contains(realSelectId2)) {
				MappedStatement ms = conf.getMappedStatement(realSelectId2);
				conf.getMappedStatements().remove(ms);
			}
			return true;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return false;
	}

	@Override
	public void unloadByXml(String xml) {
		if (StringUtil.isEmpty(xml)) {
			return;
		}

		try {

			DocumentBuilder db = DocumentBuilderFactory.newInstance().newDocumentBuilder();
			Document doc = db.parse(new ByteArrayInputStream(xml.getBytes("utf-8")));
			NodeList mappers = doc.getElementsByTagName("mapper");

			if (mappers == null || mappers.getLength() == 0) {
				return;
			}
			Element mapper = (Element) mappers.item(0);
			String namespace = mapper.getAttribute("namespace");

			NodeList seletNodeList = doc.getElementsByTagName("select");
			for (int i = 0; i < seletNodeList.getLength(); i++) {
				Element ele = (Element) seletNodeList.item(i);
				String id = ele.getAttribute("id");
				unload(namespace + "." + id);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
