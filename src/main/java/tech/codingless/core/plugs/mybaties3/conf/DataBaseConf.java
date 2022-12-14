package tech.codingless.core.plugs.mybaties3.conf;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties("tech.codingless.mybaties.rds")
public class DataBaseConf {
	private String url;
	private String username;
	private String password;
	private String classpathMapper;
	private String autoCreateTable;
	private Integer maxIdle;
	private Integer minIdle;
	private Integer maxTotal;
	private Integer maxWaitMillis;
	private Integer initialSize;
	private Integer removeAbandonedTimeout;

	public Integer getMaxIdle() {
		return maxIdle;
	}

	public void setMaxIdle(Integer maxIdle) {
		this.maxIdle = maxIdle;
	}

	public Integer getMinIdle() {
		return minIdle;
	}

	public void setMinIdle(Integer minIdle) {
		this.minIdle = minIdle;
	}

	public Integer getMaxTotal() {
		return maxTotal;
	}

	public void setMaxTotal(Integer maxTotal) {
		this.maxTotal = maxTotal;
	}

	public Integer getMaxWaitMillis() {
		return maxWaitMillis;
	}

	public void setMaxWaitMillis(Integer maxWaitMillis) {
		this.maxWaitMillis = maxWaitMillis;
	}

	public Integer getInitialSize() {
		return initialSize;
	}

	public void setInitialSize(Integer initialSize) {
		this.initialSize = initialSize;
	}

	public Integer getRemoveAbandonedTimeout() {
		return removeAbandonedTimeout;
	}

	public void setRemoveAbandonedTimeout(Integer removeAbandonedTimeout) {
		this.removeAbandonedTimeout = removeAbandonedTimeout;
	}

	public String getAutoCreateTable() {
		return autoCreateTable == null ? "" : autoCreateTable.trim();
	}

	public void setAutoCreateTable(String autoCreateTable) {
		this.autoCreateTable = autoCreateTable;
	}

	public boolean needAutoCreateTable() {
		return "true".equalsIgnoreCase(this.getAutoCreateTable()) || "1".equals(this.getAutoCreateTable());
	}

	public String getClasspathMapper() {
		return classpathMapper;
	}

	public void setClasspathMapper(String classpathMapper) {
		this.classpathMapper = classpathMapper;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

}
