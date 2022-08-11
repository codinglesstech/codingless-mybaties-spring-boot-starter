package tech.codingless.core.plugs.mybaties3.condition;

import java.util.Collection;

import lombok.Data;

@Data
public class QueryCondition implements BaseQueryWrapper { 
	private static final String AND=" and ";
	private static final String EQ=" = ";
	private static final String BEGIN_ATTR="  #{";
	private static final String CLOSE_ATTR="} ";
	private static final String IS_NULL=" is null ";
	private static final String IS_NOT_NULL=" is not null ";
	private static final String GT=" &gt; ";
	private static final String LT=" &lt; ";
	private static final String BETWEEN=" between ";
	public static final String PROP_NAME_ENDFIX_BEFORE="_before_";
	public static final String PROP_NAME_ENDFIX_AFTER="_after_";
	public static final String PROP_NAME_ENDFIX_MIN="_min_";
	public static final String PROP_NAME_ENDFIX_MAX="_max_";
	public static final String PROP_NAME_ENDFIX_LT="_lt_";
	public static final String PROP_NAME_ENDFIX_GT="_gt_";
	private boolean hasPreCondition;//是否有前置条件
	
	String propName;
	String columnName;
	Object value;
	Collection<Object> values;
	Long min;
	Long max;
	QueryConditionEnums type;
	QueryConditionRelEnums rel;

	public StringBuilder toSql() {
		StringBuilder builder = new StringBuilder(); 
		if(hasPreCondition) {
			builder.append(AND);
		}
		if (QueryConditionEnums.GT == type) { 
			 
			builder.append(getColumnName()).append(GT).append(BEGIN_ATTR).append(getPropName()).append(PROP_NAME_ENDFIX_GT).append(CLOSE_ATTR);   
			
		} else if (QueryConditionEnums.LT == type) {
			builder.append(getColumnName()).append(LT).append(BEGIN_ATTR).append(getPropName()).append(PROP_NAME_ENDFIX_LT).append(CLOSE_ATTR); 

		} else if (QueryConditionEnums.EQ == type) { 
			builder.append(getColumnName()).append(EQ).append(BEGIN_ATTR).append(getPropName()).append(CLOSE_ATTR);  
		}else if (QueryConditionEnums.IS == type) {

			builder.append(getColumnName()).append(EQ).append(BEGIN_ATTR).append(getPropName()).append(CLOSE_ATTR);  
		}else if (QueryConditionEnums.IS_NULL == type) { 
			builder.append(getColumnName()).append(IS_NULL);  
		}else if (QueryConditionEnums.IS_NOT_NULL == type) {    
			builder.append(getColumnName()).append(IS_NOT_NULL);   
		}else if (QueryConditionEnums.BETWEEN == type) { 
			builder.append(getColumnName()).append(BETWEEN).append(BEGIN_ATTR).append(getPropName()).append(PROP_NAME_ENDFIX_MIN).append(CLOSE_ATTR);
			builder.append(AND).append(BEGIN_ATTR).append(getPropName()).append(PROP_NAME_ENDFIX_MAX).append(CLOSE_ATTR); 
		}else if (QueryConditionEnums.IN == type) { 
			 
			builder.append(getColumnName()).append(" in ");
			builder.append("<foreach item=\"").append("item").append("\" collection=\"").append(this.getPropName()).append("\" index=\"index\" open=\"(\" separator=\",\" close=\")\">");
			builder.append("#{item}").append("</foreach>");  
		}else if (QueryConditionEnums.BEFORE == type) {  
 
			builder.append(getColumnName()).append(GT).append(BEGIN_ATTR).append(getPropName()).append(PROP_NAME_ENDFIX_BEFORE).append(CLOSE_ATTR);  
		}else if (QueryConditionEnums.AFTER == type) { 
			 
			builder.append(getColumnName()).append(LT).append(BEGIN_ATTR).append(getPropName()).append(PROP_NAME_ENDFIX_AFTER).append(CLOSE_ATTR);   
		}
		return builder;
	}
}
