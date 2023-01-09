package tech.codingless.core.plugs.mybaties3.data;

import tech.codingless.core.plugs.mybaties3.annotation.MyColumn;
import tech.codingless.core.plugs.mybaties3.annotation.MyComment;

public class BaseDO {

	@MyColumn(key = true)
	protected String id;
	@MyComment(value = "创建时间,epoch second,注意(单位为秒)，为了方便UTC时区处理")
	protected Long gmtCreate;
	@MyComment(value = "最近修改时间 ,epoch second,注意(单位为秒)，为了方便UTC时区处理")
	protected Long gmtWrite;
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

	@MyComment("数据级别,级别从高到低，100 ~ -100, 默认0")
	protected Integer dataLevel;

	@MyComment("逻辑删除,被逻辑删除的数据，可能随时会被清除")
	@MyColumn(defaultValue = "false")
	protected Boolean del;

	public Integer getDataLevel() {
		return dataLevel;
	}

	public void setDataLevel(Integer dataLevel) {
		this.dataLevel = dataLevel;
	}

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

	public Long getGmtCreate() {
		return gmtCreate;
	}

	public Long getGmtWrite() {
		return gmtWrite;
	}

	public void setGmtCreate(Long gmtCreate) {
		this.gmtCreate = gmtCreate;
	}

	public void setGmtWrite(Long gmtWrite) {
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
