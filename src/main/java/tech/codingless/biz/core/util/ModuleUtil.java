package tech.codingless.biz.core.util;

import java.util.concurrent.ConcurrentHashMap;

import lombok.Data;

public class ModuleUtil { 
	private static final String GRANT_PREFIX="tech.codingless.biz.module.";
	private static final String versionRegex=".v[0-9]."; 
	private static final ConcurrentHashMap<Class<?>, ModuleInfo> CACHE=new ConcurrentHashMap<>();
	@Data
	public static class ModuleInfo{
		private String moduleCode;
		private String moduleVersion;
		private String resource;
		private String moduleCodeAndVersion;
		
	}
	private static ModuleInfo NONE = new ModuleInfo();
	static {
		NONE.setModuleCode("*");
		NONE.setModuleVersion("*");
		NONE.setResource("*");
		NONE.setModuleCodeAndVersion("*/*"); 
	}

	/**
	 * 
	 * 从类名中获取模块及版本信息
	 * @param fullClassName
	 * @return
	 *
	 */
	public static ModuleInfo pickModuleInfoFromClassName(String fullClassName) {
		ModuleInfo info = new ModuleInfo();
		info.setModuleCode("NONE");
		info.setModuleVersion("v1");
		info.setResource(fullClassName);
		String subStr= fullClassName.substring(GRANT_PREFIX.length()); 
		String [] codes = subStr.split(versionRegex, 2);
		if(codes.length!=2) {
			return info;
		}
		String ver = StringUtil.findOne(subStr, versionRegex).replace(".", "");
		
		 
		info.setModuleCode(codes[0]);
		info.setModuleVersion(ver);
		 
		info.setResource(fullClassName);
		info.setModuleCodeAndVersion(info.getModuleCode()+"/"+info.getModuleVersion());
		
		return info;
	}

	public static ModuleInfo pickModuleInfo(Class<? extends Object> clazz) {
		if(CACHE.containsKey(clazz)) {
			return CACHE.get(clazz);
		}
		if(!clazz.getName().startsWith(GRANT_PREFIX)) {
			return NONE;
		}
		ModuleInfo moduleInfo = pickModuleInfoFromClassName(clazz.getName());
		CACHE.put(clazz, moduleInfo);
		
		return moduleInfo;
	}
}
