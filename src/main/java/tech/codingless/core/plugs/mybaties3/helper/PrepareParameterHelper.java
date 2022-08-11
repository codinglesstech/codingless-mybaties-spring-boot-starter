package tech.codingless.core.plugs.mybaties3.helper;

import java.math.BigDecimal;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Types;
import java.util.Date;
import java.util.List;

import org.springframework.util.CollectionUtils;

public class PrepareParameterHelper {

	/**
	 * 绑定参数 
	 * @param pstmt
	 * @param params
	 * @throws SQLException
	 *
	 */
	public static void bindParam(PreparedStatement pstmt, List<Object> params) throws SQLException {
		if(!CollectionUtils.isEmpty(params)) { 
			for(int i=0;i<params.size();i++) {
				Object param = 		params.get(i);
				int paramIndex = i+1;
				if(param==null) {
					pstmt.setNull(paramIndex, Types.NULL);
				}else if(param instanceof String) {
					pstmt.setString(paramIndex, (String)param);
				}else if(param instanceof Integer) {
					pstmt.setInt(paramIndex, (Integer)param);
				}else if(param instanceof Double) {
					pstmt.setDouble(paramIndex, (Double)param); 
				}else if(param instanceof Long) {
					pstmt.setLong(paramIndex, (Long)param); 
				}else if(param instanceof Date) {
					pstmt.setDate(paramIndex, new java.sql.Date(((Date)param).getTime())); 
				}else if(param instanceof BigDecimal) {
					pstmt.setBigDecimal(paramIndex, (BigDecimal)param); 
				}else if(param instanceof Float) {
					pstmt.setFloat(paramIndex, (Float)param);
				}else if(param instanceof Boolean) {
					pstmt.setBoolean(paramIndex, (Boolean)param);
				}else { 
					pstmt.setString(paramIndex, param.toString());
				}
			}
			
		}
		
	}

}
