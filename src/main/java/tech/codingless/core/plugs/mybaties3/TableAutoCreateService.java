package tech.codingless.core.plugs.mybaties3;

import java.util.Collection;

import javax.sql.DataSource;

import tech.codingless.core.plugs.mybaties3.data.BaseDO;

public interface TableAutoCreateService {

	boolean create();

	boolean setDOList(Collection<BaseDO> collection);

	void closeConn();

	void setUrl(String url);

	void setUsername(String username);

	void setPassword(String password);

	/**
	 * 创建数据表
	 * 
	 * @param dataSource
	 * @return
	 */
	boolean create(DataSource dataSource, String db);
}
