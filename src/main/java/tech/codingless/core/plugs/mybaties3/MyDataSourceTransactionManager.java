package tech.codingless.core.plugs.mybaties3;

import javax.sql.DataSource;

import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.support.DefaultTransactionStatus;

import tech.codingless.core.plugs.mybaties3.helper.DataSourceHelper;

public class MyDataSourceTransactionManager extends DataSourceTransactionManager {
	private static final long serialVersionUID = 1L;

	@Override
	public DataSource getDataSource() {
		return DataSourceHelper.getDataSource();
	}

	@Override
	protected void doRollback(DefaultTransactionStatus status) {
		super.doRollback(status);
	}

	@Override
	protected Object doGetTransaction() {
		return super.doGetTransaction();
	}

	@Override
	protected void doBegin(Object transaction, TransactionDefinition definition) {
		super.doBegin(transaction, definition);
	}

	@Override
	protected void doCommit(DefaultTransactionStatus status) {
		super.doCommit(status);
	}

}
