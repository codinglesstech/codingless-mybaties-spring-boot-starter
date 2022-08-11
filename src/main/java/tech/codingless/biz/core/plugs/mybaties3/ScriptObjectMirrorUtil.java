package tech.codingless.biz.core.plugs.mybaties3;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;

import jdk.nashorn.api.scripting.ScriptObjectMirror;

public class ScriptObjectMirrorUtil {

	@SuppressWarnings("removal")
	public static Object to(Object obj, Class<?> clazz) {
		ScriptObjectMirror scriptObj = (ScriptObjectMirror) obj;
		try { 
			JSONObject json = new JSONObject();
			scriptObj.keySet().forEach(key->{ 
				json.put(key, scriptObj.get(key));
			});
			Object newInstance = JSON.parseObject(json.toJSONString(), clazz); 
			return newInstance;
		} catch (Exception e) { 
			e.printStackTrace();
		} 
		return null; 
	}

}
