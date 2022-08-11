package tech.codingless.biz.core.plugs.mybaties3.util;

public class XmlMapperUtil {
	private static final String REGEX_SPLIT_ID_1 = "[iI]{1}[dD]{1}[ ]*=[ ]*[\"\']{1}";
	private static final String REGEX_SPLIT_ID_2 = "[\"\']{1}";
	private static final String REGEX_SELECT_CLOSE = "</[sS]{1}[eE]{1}[lL]{1}[eE]{1}[cC]{1}[tT]{1}[ ]*>";

	public static String fetchSelectSqlById(String xmlMapper, String id) {
		String str[] = xmlMapper.split(REGEX_SPLIT_ID_1 + id + REGEX_SPLIT_ID_2);
		if (str.length == 1) {
			return null;
		}
		String sql = str[1];
		sql = sql.substring(sql.indexOf(">") + 1);
		sql = sql.split(REGEX_SELECT_CLOSE)[0];
		return sql.trim();
	}

	public static void main(String[] args) {
		String xml = "<?xml version=\"1.0\" encoding=\"UTF-8\" ?>\r\n" + "<!DOCTYPE mapper PUBLIC \"-//mybatis.org//DTD Mapper 3.0//EN\" \"http://mybatis.org/dtd/mybatis-3-mapper.dtd\">\r\n" + "\r\n"
				+ "<mapper namespace=\"com.erp.main.pojo.ShopDO\">\r\n" + "\r\n" + " \r\n" + "\r\n" + "\r\n"
				+ "	<select id=\"listShopByCondition\" resultType=\"map\" parameterType=\"java.util.Map\">\r\n" + "	  \r\n" + "		select a.*, b.user_name\r\n"
				+ "		from uni_shop a left join uni_shop_user b on a.id = b.shop_id and b.role = 'master'\r\n" + "		WHERE \r\n" + "		a.company_id = #{companyId}\r\n"
				+ "		<if test=\"shopName !=null\">and a.shop_name like CONCAT('%',#{shopName},'%')</if>\r\n" + "		<if test=\"platform != null\">and a.platform = #{platform}</if>\r\n"
				+ "		<if test=\"stat != null\">and a.stat = #{stat}</if>\r\n" + "		<if test=\"authStat != null\">and a.auth_stat = #{authStat}</if>\r\n"
				+ "		order by a.gmt_create desc\r\n" + "		 \r\n" + "	</select>\r\n" + " \r\n" + " \r\n" + "	\r\n" + "</mapper> ";

		String s = XmlMapperUtil.fetchSelectSqlById(xml, "listShopByCondition");
		System.out.println(s);

	}

}
