package tech.codingless.biz.core.plugs.mybaties3;

import java.util.Date;

import org.bson.types.ObjectId;

 
public class BaseDO {
 
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


	public void setEnv(Integer env) {
		this.env = env;
	}
	public Integer getEnv() {
		return env;
	}
	
	public void setGroupId(String groupId) {
		this.groupId = groupId;
	}

	public String getGroupId() {
		return groupId;
	}

	@MyComment("版本")
	protected Long ver;

	public void setVer(Long ver) {
		this.ver = ver;
	}

	public Long getVer() {
		return ver;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getId() {
		return id;
	}

	/**
	 * 生成主键
	 */
	public void generatorKey() { 
		ObjectId objectId = new ObjectId();
		setId(objectId.toHexString()); 
		//setId(System.currentTimeMillis()+StringUtil.genShortGUID()); 
	}

	public Date getGmtCreate() {
		return gmtCreate;
	}

	public void setGmtCreate(Date gmtCreate) {
		this.gmtCreate = gmtCreate;
	}

	public Date getGmtWrite() {
		return gmtWrite;
	}

	public void setGmtWrite(Date gmtWrite) {
		this.gmtWrite = gmtWrite;
	}

	public String getCreateUid() {
		return createUid;
	}

	public void setCreateUid(String createUid) {
		this.createUid = createUid;
	}

	public String getWriteUid() {
		return writeUid;
	}

	public void setWriteUid(String writeUid) {
		this.writeUid = writeUid;
	}

	public String getOwnerId() {
		return ownerId;
	}

	public void setOwnerId(String ownerId) {
		this.ownerId = ownerId;
	}

	public void setCompanyId(String companyId) {
		this.companyId = companyId;
	}

	public String getCompanyId() {
		return companyId;
	}

	public Boolean isDel() {
		return del;
	}

	public void setDel(Boolean del) {
		this.del = del;
	}
}
