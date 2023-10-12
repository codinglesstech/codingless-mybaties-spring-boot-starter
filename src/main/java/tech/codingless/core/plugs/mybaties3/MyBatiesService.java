package tech.codingless.core.plugs.mybaties3;

import java.util.List;
import java.util.Map;

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

	String init();

	int executeUpdateSql(String sql, List<Object> param);

	long execinsert(String xmlInsertSql, Map<String, Object> param);

	long execupdate(String xmlUpdateSql, Map<String, Object> param);

	long execdelete(String xmlDeleteSql, Map<String, Object> param);

}
