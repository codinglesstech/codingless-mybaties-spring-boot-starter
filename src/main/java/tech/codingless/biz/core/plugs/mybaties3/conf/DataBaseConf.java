package tech.codingless.biz.core.plugs.mybaties3.conf;

import java.util.Map;

import lombok.Data;

public class DataBaseConf {
	@Data
	public static class Conf{
		private String url;
		private String username;
		private String password;
	}

	public static Conf get() {
		String urlKey="biz.core.plugs.mybaties3.db.url";
		String usernameKey="biz.core.plugs.mybaties3.db.username";
		String passwordKey="biz.core.plugs.mybaties3.db.password";
		Map<String, String>  prop = null;
		
		String url=prop.get(urlKey);
		String username=prop.get(usernameKey);
		String password=prop.get(passwordKey);
		Conf conf = new Conf();
		conf.setUrl(url);
		conf.setUsername(username);
		conf.setPassword(password);
		return conf;
	}
	
}
