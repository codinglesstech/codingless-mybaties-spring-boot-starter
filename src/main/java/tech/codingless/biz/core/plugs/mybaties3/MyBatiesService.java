package tech.codingless.biz.core.plugs.mybaties3;

import java.util.List;

import org.apache.ibatis.session.Configuration;

/**
 * 
 * @author 王鸿雁
 * @since 1.0
 */
public interface MyBatiesService {

	int update(String statement, Object parameter);

	int insert(String statement, Object parameter);

	int delete(String statement, Object parameter);

	Configuration getConfiguration();

	<T> T selectOne(String statement, Object parameter);

	<E> List<E> selectList(String statement, Object parameter);

	<E> List<E> selectList(String statement);

	/**
	 * 
	 * 走未分片数据源
	 *
	 */
	<E> List<E> selectListNoSharding(String statement, Object parameter);

	/**
	 * 初始化访问连接池
	 * 
	 * @return
	 */
	String init();

	/**
	 * 执行SQL语句
	 * 
	 * @param sql
	 * @return
	 */
	int executeUpdateSql(String sql, List<Object> param);

}
