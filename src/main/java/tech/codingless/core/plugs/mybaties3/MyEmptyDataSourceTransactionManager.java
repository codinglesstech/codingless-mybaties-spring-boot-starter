package tech.codingless.core.plugs.mybaties3;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionException;
import org.springframework.transaction.support.AbstractPlatformTransactionManager;
import org.springframework.transaction.support.DefaultTransactionStatus;
import org.springframework.transaction.support.ResourceTransactionManager;

public class MyEmptyDataSourceTransactionManager extends AbstractPlatformTransactionManager implements ResourceTransactionManager, InitializingBean {
	private static final long serialVersionUID = 1L;
	private static final Logger LOG = LoggerFactory.getLogger(MyEmptyDataSourceTransactionManager.class);

	public MyEmptyDataSourceTransactionManager() {
		LOG.error("您启动了一个假的事管理器！");
	}

	@Override
	public void afterPropertiesSet() throws Exception {

	}

	@Override
	public Object getResourceFactory() {
		return null;
	}

	@Override
	protected Object doGetTransaction() throws TransactionException {
		return null;
	}

	@Override
	protected void doBegin(Object transaction, TransactionDefinition definition) throws TransactionException {

	}

	@Override
	protected void doCommit(DefaultTransactionStatus status) throws TransactionException {

	}

	@Override
	protected void doRollback(DefaultTransactionStatus status) throws TransactionException {
		LOG.error("哈哈〜 您加载了一个假的事管理器,没有执行回滚动作!");

	}

}
