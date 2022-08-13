# Codingless For Mybaties 


- Step 1: pom.xml

```

<dependency>
  <groupId>tech.codingless</groupId>
  <artifactId>codingless-mybaties-spring-boot-starter</artifactId>
  <version>0.0.16</version>
</dependency>

```

- Step 2: application.properties

```
#create table and column when started if true
tech.codingless.mybaties.auto-create-table= < true | false >
tech.codingless.mybaties.rds.url=<jdbc url>
tech.codingless.mybaties.rds.username=<username>
tech.codingless.mybaties.rds.password=<password>
tech.codingless.mybaties.rds.classpath-mapper= < your classpath eg. com/xxx/xxx/**/*Mapper.xml >
```

# Examples

- create data object and auto mapping to database
```
@Mytable
@setter
@getter
public class TestDO extends BaseDO {
  
  
	@MyColumn(type = "varchar(10)") //is optional for column control
	@MyComment("this is colunm comment")// is optional 
  private String xxx;
  
  ...
}
```
