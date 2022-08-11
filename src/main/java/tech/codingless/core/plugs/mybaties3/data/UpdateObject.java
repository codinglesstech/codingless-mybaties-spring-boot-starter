package tech.codingless.core.plugs.mybaties3.data;

import lombok.Data;

@Data
public class UpdateObject { 
	private BaseDO updateDO;
	private Long ver;
	private String companyId;
	private String id;
}
