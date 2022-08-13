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
tech.codingless.mybaties.rds.url=<jdbc url>
tech.codingless.mybaties.rds.username=<username>
tech.codingless.mybaties.rds.password=<password>
tech.codingless.mybaties.rds.classpath-mapper= < your classpath eg. com/xxx/xxx/**/*Mapper.xml >
```
