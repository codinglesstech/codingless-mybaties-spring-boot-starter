package tech.codingless.core.plugs.mybaties3.condition;

import java.util.ArrayList;
import java.util.List;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;

import tech.codingless.core.plugs.mybaties3.util.MybatiesStringUtil;

public class ConditionHelper {

	public static List<QueryCondition> parse(String conditionDoc) {
		if (MybatiesStringUtil.isEmpty(conditionDoc)) {
			return new ArrayList<>(2);
		}

		List<QueryCondition> conditionList = new ArrayList<>();
		JSONObject json = JSON.parseObject(conditionDoc);
		for (String column : json.keySet()) {
			JSONObject oneColumnCondition = json.getJSONObject(column);
			for (String op : oneColumnCondition.keySet()) {
				Object val = oneColumnCondition.get(op);
				if (val == null || (val instanceof String && MybatiesStringUtil.isEmpty((String) val))) {
					continue;
				}

				QueryCondition condition = new QueryCondition();
				condition.setColumnName(column);
				conditionList.add(condition);

				if ("eq".equalsIgnoreCase(op)) {
					condition.setValue(val);
					condition.setType(QueryConditionEnums.EQ);
				} else if ("in".equalsIgnoreCase(op) && val instanceof JSONArray) {
					JSONArray values = (JSONArray) val;
					condition.setValues(List.of(values.toArray()));
					condition.setType(QueryConditionEnums.IN);
				} else if ("gt".equalsIgnoreCase(op)) {
					condition.setValue(val);
					condition.setType(QueryConditionEnums.GT);
				} else if ("lt".equalsIgnoreCase(op)) {
					condition.setValue(val);
					condition.setType(QueryConditionEnums.LT);
				}else if ("is".equalsIgnoreCase(op) && val instanceof Boolean) {
					condition.setValue(val);
					condition.setType(QueryConditionEnums.IS);
				}
			}
		}
		return conditionList;
	}

}
