package tech.codingless.core.plugs.mybaties3.helper;

import java.util.concurrent.ConcurrentHashMap;

import org.apache.ibatis.session.Configuration;
import org.apache.ibatis.type.TypeHandlerRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import lombok.extern.slf4j.Slf4j;

/**
 * 
 * 
 * @author 王鸿雁
 * @version 2021年10月5日
 */
@Slf4j
public class MyTypeHanderRegistHelper {
	private static final Logger LOG = LoggerFactory.getLogger(MyTypeHanderRegistHelper.class);
	private static ConcurrentHashMap<Class<?>, Boolean> CACHE = new ConcurrentHashMap<>();
	private static ConcurrentHashMap<Class<?>, Class<?>> MAP = new ConcurrentHashMap<>();

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
					if(LOG.isDebugEnabled()) {
						LOG.debug("Regist Type Hander:{} For ColumnType:{}, JavaType:{}", columnProp.getTypeHandler(), columnProp.getJdbcType(), columnProp.getJavaTypeClass());
					}
					if (MAP.containsKey(columnProp.getJavaTypeClass()) && MAP.get(columnProp.getJavaTypeClass()) != columnProp.getTypeHandler()) {

						Error error = new Error(String.format("The Type:%s duplicate Regist Type Handler (%s)  and (%s)", columnProp.getJavaTypeClass(), columnProp.getTypeHandler(),
								MAP.get(columnProp.getJavaTypeClass())));
						log.error("Important Error Cause System exist ", error);
						System.exit(0);
					}
					configuration.getTypeHandlerRegistry().register(columnProp.getJavaTypeClass(), columnProp.getJdbcType(), columnProp.getTypeHandler());
					MAP.put(columnProp.getJavaTypeClass(), columnProp.getTypeHandler());
				}
			} catch (Throwable e) {
				CACHE.remove(clazz);
				LOG.error("Regist Type Hander Error", e);
			}
		});
		CACHE.put(clazz, true);
	}

}
