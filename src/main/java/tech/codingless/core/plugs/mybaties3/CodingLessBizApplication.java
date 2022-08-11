package tech.codingless.core.plugs.mybaties3;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@EnableScheduling
@EnableTransactionManagement
@SpringBootApplication(exclude = { DataSourceAutoConfiguration.class })
@ComponentScan(basePackages = { "tech.codingless.biz" })
public class CodingLessBizApplication {

	public static void main(String[] args) {
		SpringApplication.run(CodingLessBizApplication.class, args); 
		//GracefulShutdownCallback
	}
	
 

}
