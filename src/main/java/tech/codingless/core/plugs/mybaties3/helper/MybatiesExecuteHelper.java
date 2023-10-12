package tech.codingless.core.plugs.mybaties3.helper;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.ParameterMapping;
import org.apache.ibatis.mapping.SqlSource;
import org.springframework.util.CollectionUtils;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class MybatiesExecuteHelper {

	public static long execinsert(SqlSource sqlSource, Map<String, Object> param) throws SQLException {
		try {
			Connection conn = null;
			PreparedStatement ps = null;
			BoundSql boundSql = sqlSource.getBoundSql(param);
			conn = DataSourceHelper.getSqlSessionTemplate().getConnection();
			ps = conn.prepareStatement(boundSql.getSql());
			// bind param
			StringBuilder plog = new StringBuilder();
			if (log.isDebugEnabled()) {
				plog.append(boundSql.getSql().replaceAll("[\r\n]", "").replaceAll("[ \t]+", " ")).append("\t PARAM: ");
			}
			@SuppressWarnings("unchecked")
			HashMap<String, Object> parameter = ((HashMap<String, Object>) boundSql.getParameterObject());

			if (!CollectionUtils.isEmpty(boundSql.getParameterMappings())) {
				for (int i = 0; i < boundSql.getParameterMappings().size(); i++) {
					ParameterMapping p = boundSql.getParameterMappings().get(i);
					Object val = boundSql.getAdditionalParameter(p.getProperty());
					if (val == null) {
						val = parameter.get(p.getProperty());
					}
					ps.setObject(i + 1, val);
					if (log.isDebugEnabled()) {
						plog.append(val).append("(").append(p.getJavaType().getSimpleName()).append(")").append(",");
					}
				}
			}
			if (plog.length() > 0 && log.isDebugEnabled()) {
				plog.deleteCharAt(plog.length() - 1);
				log.debug("INSERT: {}", plog.toString());
			}
			return ps.executeUpdate();

		} finally {

		}

	}

}
