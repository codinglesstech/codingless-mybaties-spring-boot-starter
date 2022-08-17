# Setting codingless-mybaties 


- Step 1: pom.xml

```

<dependency>
  <groupId>tech.codingless</groupId>
  <artifactId>codingless-mybaties-spring-boot-starter</artifactId>
  <version>0.0.17</version>
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


#your column cant conflict with baseDO, there is Standard usefull common column for all biz like flow this
//Default Auto create id with ObjectId format of bson, you can implement IdCreator interface to create your own id strategy
@MyColumn(key = true)
protected String id;

@MyComment(value = "创建时间")
protected Date gmtCreate;

@MyComment(value = "最近修改时间")
protected Date gmtWrite;

@MyComment("创建者ID")
protected String createUid;

@MyComment("修改者ID")
protected String writeUid;

@MyComment("数据拥有者ID")
protected String ownerId;

@MyComment("公司编号，组织编号，作为机构间数据隔离的标志")
protected String companyId;

@MyComment("团队ID，可以是部门ID，也可以是虚拟团队ID")
protected String groupId;

@MyColumn(createIndex = true)
@MyComment("数据所处环境,1：生产环境，2:测试环境,DataEnvEnums")
protected Integer env;

@MyComment("逻辑删除,被逻辑删除的数据，可能随时会被清除")
@MyColumn(defaultValue = "false")
protected Boolean del;

```

- at last enjoy your work time of coding


