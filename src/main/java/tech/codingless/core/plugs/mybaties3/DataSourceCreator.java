package tech.codingless.core.plugs.mybaties3;

import javax.sql.DataSource;

/**
 * 
 * @author 王鸿雁
 *
 */
public interface DataSourceCreator {

	public DataSource make();
}
