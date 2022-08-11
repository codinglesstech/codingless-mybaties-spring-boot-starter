package tech.codingless.core.plugs.mybaties3;

import javax.sql.DataSource;

/**
 * 非Sharding数据源
 * 
 * @author 王鸿雁
 * @version 2021年1月4日
 */
public interface MybatiesDataSourceFactory {

	public DataSource make();
}
