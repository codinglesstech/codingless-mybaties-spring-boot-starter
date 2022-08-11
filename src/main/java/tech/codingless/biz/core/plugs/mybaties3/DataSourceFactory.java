package tech.codingless.biz.core.plugs.mybaties3;

import javax.sql.DataSource;

/**
 * 数据源生成器
 * 
 * @author 王鸿雁
 *
 */
public interface DataSourceFactory {

	public DataSource make();
}
