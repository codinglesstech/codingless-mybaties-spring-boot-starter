package tech.codingless.biz.core.plugs.mybaties3.data;

import lombok.Data;
import tech.codingless.biz.core.plugs.mybaties3.BaseDO;

@Data
public class UpdateObject { 
	private BaseDO updateDO;
	private Long ver;
	private String companyId;
	private String id;
}
