package tech.codingless.core.plugs.mybaties3;

import java.util.Collection;

import tech.codingless.core.plugs.mybaties3.data.BaseDO; 
public interface TableAutoCreateService {

	boolean create();

	boolean setDOList(Collection<BaseDO> collection);

	void closeConn();
	
	void setUrl(String url);

	void setUsername(String username);
	void setPassword(String password);
}
