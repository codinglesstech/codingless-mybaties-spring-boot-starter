package tech.codingless.biz.core.plugs.mybaties3;

import java.util.Collection; 
public interface TableAutoCreateService {

	boolean create();

	boolean setDOList(Collection<BaseDO> collection);

	void closeConn();
	
	void setUrl(String url);

	void setUsername(String username);
	void setPassword(String password);
}