package tech.codingless.core.plugs.mybaties3.typehandler;

import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.apache.ibatis.type.BaseTypeHandler;
import org.apache.ibatis.type.JdbcType;

import com.alibaba.fastjson2.JSON;

import lombok.Getter;
import lombok.Setter;
import tech.codingless.core.plugs.mybaties3.util.MybatiesStringUtil;

@Setter
@Getter
public abstract class JsonTypeHander extends BaseTypeHandler<Object> {
	
	/**
	 * Attribute Type
	 * @return Attribute Type
	 */
	public abstract Class<?> type();

	@Override
	public Object getNullableResult(ResultSet rs, String columnName) throws SQLException {
		String str = rs.getString(columnName);

		if (MybatiesStringUtil.isEmpty(str)) {
			return null;
		}
		return JSON.parseObject(str, type());
	}

	@Override
	public Object getNullableResult(ResultSet rs, int columnIndex) throws SQLException {

		String str = rs.getString(columnIndex);
		if (MybatiesStringUtil.isEmpty(str)) {
			return null;
		}
		return JSON.parseObject(str, type());
	}

	@Override
	public Object getNullableResult(CallableStatement cs, int columnIndex) throws SQLException {
		String str = cs.getString(columnIndex);
		if (MybatiesStringUtil.isEmpty(str)) {
			return null;
		}
		return JSON.parseObject(str, type());
	}

	@Override
	public void setNonNullParameter(PreparedStatement ps, int i, Object parameter, JdbcType jdbcType) throws SQLException {
		ps.setString(i, JSON.toJSONString(parameter));
	}

}
