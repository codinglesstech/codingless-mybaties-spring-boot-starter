package tech.codingless.biz.core.util;

import java.util.TreeMap;

public class SignUtil {
	private static final String DENGYU="=";
	private static final String AND="&";
	/**
	 * param1=xxx&param2=xxx&...&1647417940667
	 * @param param
	 * @param time
	 * @return
	 */
	public static String toSignSrc(TreeMap<String, String> param,String time) {
		StringBuilder src = new StringBuilder();
		for(String key:param.keySet()) {
			if(param.get(key)==null) {
				continue;
			}
			src.append(key).append(DENGYU).append(param.get(key)).append(AND); 
		}
		src.append(time);
		
		return src.toString(); 
	}

}
