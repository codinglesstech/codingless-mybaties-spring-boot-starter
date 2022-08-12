package tech.codingless.core.plugs.mybaties3;

public class ScriptObjectMirrorUtil {
 
	public static Object to(Object obj, Class<?> clazz) {
		/*
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
		*/
		return null; 
	}

}
