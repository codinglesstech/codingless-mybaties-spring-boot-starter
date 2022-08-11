package tech.codingless.biz.core.plugs.mybaties3;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.servlet.http.HttpServletRequest;

import org.apache.ibatis.mapping.MappedStatement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import tech.codingless.biz.core.util.StringUtil;

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
		if (StringUtil.isEmpty(parameter.getSqlId())) {
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
		if (StringUtil.isEmpty(parameter.getSqlId())) {
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

	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	public Map list(String listId, String countId, HttpServletRequest request) {
		Map map = new HashMap();

		Map p = new HashMap();
		Iterator it = request.getParameterMap().keySet().iterator();
		while (it.hasNext()) {
			String key = (String) it.next();
			p.put(key, request.getParameter(key));
		}
		String totalRows = request.getParameter("totalRows");
		String noCount = request.getParameter("noCount");
		String pageStr = request.getParameter("page");
		String sort = request.getParameter("sort");
		String order = request.getParameter("order");
		Integer page = StringUtil.isNotEmpty(pageStr) ? Integer.parseInt(pageStr) : 1;
		String rowsStr = request.getParameter("rows");
		Integer rows = StringUtil.isNotEmpty(rowsStr) ? Integer.parseInt(rowsStr) : 1;
		if (totalRows == null && !"true".equalsIgnoreCase(noCount)) {
			QueryService.Parameter parameter = new QueryService.Parameter();
			parameter.setMap(p);
			parameter.setSqlId(countId);
			int count = count(parameter);
			map.put("total", count);
		}

		QueryService.Parameter parameter = new QueryService.Parameter();
		parameter.setMap(p);
		parameter.setSqlId(listId);
		parameter.setPageNumber(page != null && page > 1 ? page : 1);
		parameter.setPageSize(rows != null && rows > 0 ? rows : 10);
		parameter.setSort(sort);
		parameter.setOrder(order);
		parameter.addAttribute("sort", sort);
		parameter.addAttribute("order", order);
		List<Map<String, Object>> list = list(parameter);
		map.put("rows", list);
		return map;
	}

	@SuppressWarnings("rawtypes")
	public String getString(Map map, String key, String defaultStr) {
		if (map.containsKey(key)) {
			return map.get(key).toString();
		}
		return defaultStr;
	}

}
