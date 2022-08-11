package tech.codingless.core.plugs.mybaties3;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class MybatiesTestServiceImpl implements MybatiesTestService {

	@Autowired
	private MyBatiesService myBatiesService;

	@Transactional()
	@Override
	public void test(boolean error) {

		Map<String, String> param = new HashMap<>();
		param.put("id", System.currentTimeMillis() + "");
		param.put("v", System.currentTimeMillis() + "");
		int effect = myBatiesService.insert("TEST.insert1", param);
		System.out.println(effect);
		if (error) {
			throw new RuntimeException("出错了");

		}
		param.put("id", System.currentTimeMillis() + "P");
		param.put("v", System.currentTimeMillis() + "P");
		effect = myBatiesService.insert("TEST.insert1", param);

	}

}
