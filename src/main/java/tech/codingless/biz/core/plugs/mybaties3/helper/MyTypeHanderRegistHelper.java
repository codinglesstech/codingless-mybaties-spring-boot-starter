package tech.codingless.biz.core.plugs.mybaties3.helper;

import java.util.concurrent.ConcurrentHashMap;

import org.apache.ibatis.session.Configuration;
import org.apache.ibatis.type.TypeHandlerRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 
 * 
 * @author 王鸿雁
 * @version 2021年10月5日
 */
public class MyTypeHanderRegistHelper {
	private static final Logger LOG = LoggerFactory.getLogger(MyTypeHanderRegistHelper.class);
	private static ConcurrentHashMap<Class<?>, Boolean> CACHE = new ConcurrentHashMap<>();

	/**
	 * 
	 * 注册自定义TypeHander
	 * 
	 * @param configuration
	 * @param clazz
	 *
	 */
	public static void regist(Configuration configuration, Class<?> clazz) {
		if (CACHE.containsKey(clazz)) {
			return;
		}
		MyTableColumnParser.parse(clazz).forEach(columnProp -> {
			try {
				if (MyTableColumnParser.isDefaultSupportType(columnProp.getJavaTypeClass())) {
					return;
				}
				if (columnProp.getTypeHandler() != null) {
					TypeHandlerRegistry registry = configuration.getTypeHandlerRegistry();
					if (registry.hasTypeHandler(columnProp.getTypeHandler())) {
						return;
					}
					LOG.info("Regist Type Hander {}", columnProp.getTypeHandler());
					configuration.getTypeHandlerRegistry().register(columnProp.getJavaTypeClass(), columnProp.getJdbcType(), columnProp.getTypeHandler());
				}
			} catch (Throwable e) {
				CACHE.remove(clazz);
				LOG.error("Regist Type Hander Error", e);
			}
		});
		CACHE.put(clazz, true);
	}

}
