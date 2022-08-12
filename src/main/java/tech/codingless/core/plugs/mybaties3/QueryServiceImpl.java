package tech.codingless.core.plugs.mybaties3;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.ibatis.mapping.MappedStatement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import tech.codingless.core.plugs.mybaties3.util.MybatiesStringUtil;

@Service
public class QueryServiceImpl implements QueryService {
	private final static Logger LOG = LoggerFactory.getLogger(TableAutoCreateServiceMysqlImpl.class); 
	private static ConcurrentHashMap<String, Boolean> SAFE_SQLID_MAP = new ConcurrentHashMap<String, Boolean>();

	@SuppressWarnings("rawtypes")
	@Autowired
	private GenericQueryDao   queryDao;

	@Autowired
	private MyBatiesService myBatiesService;

	@SuppressWarnings("unchecked")
	@Transactional(propagation = Propagation.NOT_SUPPORTED)
	@Override
	public List<Map<String, Object>> list(Parameter parameter) {
		if (MybatiesStringUtil.isEmpty(parameter.getSqlId())) {
			return null;
		}
		if (!SAFE_SQLID_MAP.containsKey(parameter.getSqlId())) {
			MappedStatement stmt = myBatiesService.getConfiguration().getMappedStatement(parameter.getSqlId());

			parameter.addAttribute("offset", 0);
			parameter.addAttribute("limit", 0);
			if (stmt.getSqlSource().getBoundSql(parameter.getMap()).getSql().toUpperCase().startsWith("SELECT")) {
				SAFE_SQLID_MAP.put(parameter.getSqlId(), true);
			}
		}

		if (!SAFE_SQLID_MAP.containsKey(parameter.getSqlId())) {
			LOG.error("sql语句[" + parameter.getSqlId() + "]可能不存在,或是不安全!");
			return null;
		}

		int offset = (parameter.getPageNumber() - 1) * parameter.getPageSize();
		parameter.addAttribute("offset", offset);
		parameter.addAttribute("limit", parameter.getPageSize());
		return queryDao.selectList(parameter.getSqlId(), parameter.getMap());
	}

	@Transactional(propagation = Propagation.NOT_SUPPORTED)
	@Override
	public int count(Parameter parameter) {
		if (MybatiesStringUtil.isEmpty(parameter.getSqlId())) {
			return 0;
		}
		if (!SAFE_SQLID_MAP.containsKey(parameter.getSqlId())) {
			MappedStatement stmt = myBatiesService.getConfiguration().getMappedStatement(parameter.getSqlId());

			if (stmt.getSqlSource().getBoundSql(parameter.getMap()).getSql().toUpperCase().startsWith("SELECT")) {
				SAFE_SQLID_MAP.put(parameter.getSqlId(), true);
			}
		}

		if (!SAFE_SQLID_MAP.containsKey(parameter.getSqlId())) {
			LOG.error("sql语句[" + parameter.getSqlId() + "]可能不存在,或是不安全!");
			return 0;
		}
	 
		return (int) this.queryDao.selectOneRow(parameter.getSqlId(), parameter.getMap());
	}

 

	@SuppressWarnings("rawtypes")
	public String getString(Map map, String key, String defaultStr) {
		if (map.containsKey(key)) {
			return map.get(key).toString();
		}
		return defaultStr;
	}

}
