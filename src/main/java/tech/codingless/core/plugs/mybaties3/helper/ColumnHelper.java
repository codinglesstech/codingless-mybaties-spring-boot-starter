package tech.codingless.core.plugs.mybaties3.helper;

import tech.codingless.core.plugs.mybaties3.util.MybatiesStringUtil;

public class ColumnHelper {
	private final static String REGEX="^[a-zA-Z_0-9]+$";
	public static boolean isIncorrectColumn(String column) { 
		 return !isCorrectColumn(column);
	}
	
	public static boolean isCorrectColumn(String column) { 
		if(MybatiesStringUtil.isEmpty(column)) {
			return false;
		}
		return column.matches(REGEX);
	}

}
