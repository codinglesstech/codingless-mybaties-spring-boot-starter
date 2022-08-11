package tech.codingless.biz.core.plugs.mybaties3.data;

import java.util.List;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class PageRollResult<T> {
	private Integer totalCount;
	private Integer currentPage;
	private Integer pageSize;
	private Integer totalPage;
	private List<T> list;
}
