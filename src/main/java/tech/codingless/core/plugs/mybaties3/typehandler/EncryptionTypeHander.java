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
import lombok.extern.slf4j.Slf4j;
import tech.codingless.core.plugs.mybaties3.util.AESUtil;
import tech.codingless.core.plugs.mybaties3.util.MybatiesMD5Util;
import tech.codingless.core.plugs.mybaties3.util.MybatiesStringUtil;

/**
 * 
 * 数据加密存储
 *
 */
@Slf4j
@Setter
@Getter
public abstract class EncryptionTypeHander extends BaseTypeHandler<Object> {

	/**
	 * Attribute Type
	 * 
	 * @return Attribute Type
	 */
	public abstract Class<?> type();

	/**
	 * 用于加密的秘钥
	 * 
	 * @return
	 */
	public String secretKey() {
		return MybatiesMD5Util.md5Hex(type().getName());
	}

	/**
	 * 用于加密的盐
	 * 
	 * @return
	 */
	public String salt() {
		return MybatiesMD5Util.md5Hex(type().getName());
	}

	private String decoding(String str, String columnName, int columnIndex) {
		if (MybatiesStringUtil.isEmpty(str)) {
			return str;
		}
		try {
			String key = AESUtil.genFiexdSecret(this.secretKey() + this.salt());
			return AESUtil.decrypt(key, str);
		} catch (Exception e) {
			log.error("Type:{}, ColumnName:{},ColumnIndex:{} decrypt Fail", type(), columnName, columnIndex);
		}
		return null;
	}

	@Override
	public Object getNullableResult(ResultSet rs, String columnName) throws SQLException {
		String str = rs.getString(columnName);
		str = decoding(str, columnName, -1);

		if (MybatiesStringUtil.isEmpty(str)) {
			return null;
		}
		return JSON.parseObject(str, type());
	}

	@Override
	public Object getNullableResult(ResultSet rs, int columnIndex) throws SQLException {

		String str = rs.getString(columnIndex);
		str = decoding(str, null, columnIndex);
		if (MybatiesStringUtil.isEmpty(str)) {
			return null;
		}
		return JSON.parseObject(str, type());
	}

	@Override
	public Object getNullableResult(CallableStatement cs, int columnIndex) throws SQLException {
		String str = cs.getString(columnIndex);
		str = decoding(str, null, columnIndex);
		if (MybatiesStringUtil.isEmpty(str)) {
			return null;
		}
		return JSON.parseObject(str, type());
	}

	@Override
	public void setNonNullParameter(PreparedStatement ps, int i, Object parameter, JdbcType jdbcType) throws SQLException {
		String str = JSON.toJSONString(parameter);
		String key;
		try {
			key = AESUtil.genFiexdSecret(this.secretKey() + this.salt());
			str = AESUtil.encrypt(key, str);
			ps.setString(i, str);
		} catch (Exception e) {
			log.error("", e);
		}
	}

}
