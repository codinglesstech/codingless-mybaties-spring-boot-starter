package tech.codingless.core.plugs.mybaties3;

import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Order(Ordered.LOWEST_PRECEDENCE - 100)
public class MybatiesImportSelector {

	public MybatiesImportSelector() {
		log.info("Init Codingless Mybaties");
	}
}
