package tech.codingless.biz.core.plugs.mybaties3.helper;

import tech.codingless.biz.core.plugs.mybaties3.BaseDO;
import tech.codingless.biz.core.plugs.mybaties3.MyTable;
import tech.codingless.biz.core.plugs.mybaties3.StringUtil;

/**
 * 
 * 表名工具类
 * 
 * @author 王鸿雁
 * @version 2021年7月30日
 */
public class MyTableNameUtil {
	private static String TABLE_NAME = "uni_";
	private static String SPLIT_WORDS = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";

	public static String getTableName(BaseDO obj) {
		MyTable myTable = obj.getClass().getAnnotation(MyTable.class);
		String tableName = StringUtil.isEmpty(myTable.prefix()) ? TABLE_NAME : myTable.prefix().trim() + "_" + change2dbFormat(obj.getClass().getSimpleName());
		tableName = tableName.replace("_D_O", "").toLowerCase();
		return tableName;
	}

	private static String change2dbFormat(String string) {
		StringBuffer sb = new StringBuffer(string);
		int len = sb.length();
		for (int i = 1; i < len; i++) {
			if (!SPLIT_WORDS.contains(sb.charAt(i) + "")) {
				continue;
			}
			sb.insert(i, "_");
			i++;
			len++;
		}
		return sb.toString();
	}
}
