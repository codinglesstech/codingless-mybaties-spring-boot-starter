package tech.codingless.biz.core.util;

public class StackTraceUtil {
	private static final String HC="\n\t";
	private static final String AT="at ";
	private static final String CAUSEBY="Caused by:";
	public static String format(Throwable e) {
		StringBuilder sb = new StringBuilder();
		sb.append(e);
		for (StackTraceElement ele : e.getStackTrace()) {
			sb.append(HC).append(AT).append(ele.toString()); 
		} 
		
		if(e.getCause()!=null&&e.getCause().getStackTrace()!=null) {
			sb.append(HC).append(CAUSEBY).append(e.getCause());
			for (StackTraceElement ele : e.getCause().getStackTrace()) {
				sb.append(HC).append(AT).append(ele.toString()); 
			} 
		}
		 
		
		
		return sb.toString();
	}

}
