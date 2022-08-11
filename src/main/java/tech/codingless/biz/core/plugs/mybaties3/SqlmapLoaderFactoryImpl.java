package tech.codingless.biz.core.plugs.mybaties3;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.stereotype.Component;
 
 
@Component
public class SqlmapLoaderFactoryImpl implements SqlmapLoaderFactory {
	private static final Logger LOG = LoggerFactory.getLogger(SqlmapLoaderFactoryImpl.class);

	@Override
	public Resource[] sqlMapperResource() {
		try {
			LOG.info("扫描自定义sqlmapper路径");
			PathMatchingResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
			Resource[] resourcesList = resolver.getResources("classpath*:tech/codingless/biz/**/*Mapper.xml");
			if (resourcesList != null) {
				for (Resource rs : resourcesList) {
					LOG.info("自定义sqlmapper:" + rs.getURL());
				}
			}

			return resourcesList;
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}

}
