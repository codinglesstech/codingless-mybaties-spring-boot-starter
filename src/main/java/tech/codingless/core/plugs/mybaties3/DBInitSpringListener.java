package tech.codingless.core.plugs.mybaties3;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationStartedEvent;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.ApplicationListener;

import tech.codingless.core.plugs.mybaties3.conf.DataBaseConf;
import tech.codingless.core.plugs.mybaties3.data.BaseDO;
import tech.codingless.core.plugs.mybaties3.helper.AutoFindByIdHelper;
import tech.codingless.core.plugs.mybaties3.helper.AutoGetHelper;
import tech.codingless.core.plugs.mybaties3.helper.AutoUpdateHelper;
import tech.codingless.core.plugs.mybaties3.helper.MyTypeHanderRegistHelper;
import tech.codingless.core.plugs.mybaties3.util.MybatiesStringUtil;

//优先级最高，最先执行 
public class DBInitSpringListener implements ApplicationListener<ApplicationStartedEvent> {
	private static final Logger LOG = LoggerFactory.getLogger(GenericUpdateDAOImpl.class);
	@Autowired
	private TableAutoCreateService tableAutoCreateService;
	@Autowired
	ApplicationEventPublisher eventPublisher;

	@Autowired
	private CommScriptGeneter commScriptGeneter;

	@Autowired
	ApplicationContext context;
	@Autowired
	private MyBatiesService myBatiesService;

	@Autowired(required = false)
	private DataBaseConf conf;

	@Value("${tech.codingless.biz.core.createtable:1}")
	private String isautocreate;

	@Override
	public void onApplicationEvent(ApplicationStartedEvent event) {
		LOG.info("监听到  Spring  启动.");
		LOG.info("ApplicationStartedEvent_DBInitSpringListener");

		Map<String, BaseDO> map = context.getBeansOfType(BaseDO.class);
		if (map != null && !map.isEmpty()) {
			tableAutoCreateService.setDOList(map.values());
			commScriptGeneter.setDOList(map.values());

		}
		if (conf != null && conf.needAutoCreateTable() && MybatiesStringUtil.isNotEmpty(conf.getUrl(), conf.getUsername(), conf.getPassword())) {
			LOG.info("自动同步表结构");
			tableAutoCreateService.setUrl(conf.getUrl());
			tableAutoCreateService.setUsername(conf.getUsername());
			tableAutoCreateService.setPassword(conf.getPassword());
			tableAutoCreateService.create();
			tableAutoCreateService.closeConn();
		}

		// 初始化所有TypeHandler
		map.values().forEach(dataobject -> {
			MyTypeHanderRegistHelper.regist(myBatiesService.getConfiguration(), dataobject.getClass());
		});

		// 系统启动即生成相应的SQL语句，这样可以减少错误
		map.values().forEach(entity -> {
			LOG.info("Gen Auto Sql For Entity:{}", entity);
			AutoGetHelper.genAutoSqlForGet(entity.getClass(), false, myBatiesService.getConfiguration());
			AutoUpdateHelper.genUpdateSkipNullSql(myBatiesService.getConfiguration(), entity.getClass());
			AutoFindByIdHelper.genGetSql(myBatiesService.getConfiguration(), entity.getClass());
		});

	}

}
