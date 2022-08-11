package tech.codingless.biz.core.plugs.mybaties3.typehandler;

import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;

import org.apache.ibatis.type.BaseTypeHandler;
import org.apache.ibatis.type.JdbcType;

import tech.codingless.biz.core.plugs.mybaties3.util.AssertUtil;
 
 
 
public  class MyBaseTypeHander<T>  extends BaseTypeHandler<T>{
 
 
	@Override
	public void setNonNullParameter(PreparedStatement ps, int i, T parameter, JdbcType jdbcType) throws SQLException { 
		if(parameter==null) { 
			ps.setNull(i, jdbcType.TYPE_CODE);
			return;
		} 
		AssertUtil.assertTrue(parameter instanceof MyBaseColumn, "PARAM_NOT_INSTANCEOF_MYBASECOLUMN"); 
		Object dbObj = ((MyBaseColumn)parameter).toSerialize();
		if(dbObj==null) {
			ps.setNull(i, jdbcType.TYPE_CODE);
			return;
		}
		if(dbObj instanceof Integer&&jdbcType.TYPE_CODE==Types.INTEGER) {
			ps.setInt(i, (Integer)dbObj);
		}else if(dbObj instanceof String&&jdbcType.TYPE_CODE==Types.VARCHAR) {
			ps.setString(i, (String)dbObj); 
		}else {
			AssertUtil.assertFail("JAVA_TYPE_JDBC_TYPE_NOT_MATCH");
		}
		
	}

	@Override
	public T getNullableResult(ResultSet rs, String columnName) throws SQLException {
		
		
		
		return null;
	}

	@Override
	public T getNullableResult(ResultSet rs, int columnIndex) throws SQLException {
		 
		return null;
	}

	@Override
	public T getNullableResult(CallableStatement cs, int columnIndex) throws SQLException {
		
		return null;
	}
	 


}
