package tech.codingless.core.plugs.mybaties3.util;

import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReentrantLock;

public class SnowFlakeNumberUtil {

	// 2022-12-26 00:00:00 业务开始时间
	private static final long BEGIN_TIME_STAMP = 1671984000000L;
	private static AtomicInteger count = new AtomicInteger();
	private static long PRE_MILLIS = 0;
	private static long NODE = random(0, 1020);
	private static ReentrantLock LOCK = new ReentrantLock(); 

	public static long getNode() {
		return NODE;
	}

	public static long setNode(long node) {
		if (node > 0 && node < 1020) {
			NODE = node;
		}
		return NODE;
	}

	public static int random(int min, int max) {
		Random random = new Random();
		return random.nextInt(min + max) - min;
	}

	/**
	 * <pre>
	 * 通过雪花算法获得ID
	 * </pre>
	 * 
	 * 63Bit long value 高41比特为时间毫秒 + 中间 10bit为服务 + 尾 12bit为自增数
	 * 
	 * @return
	 */
	public static long nextId() {
		long t = System.currentTimeMillis();
		if (t != PRE_MILLIS) {
			try {
				LOCK.lock();
				if (t != PRE_MILLIS) {
					count.set(0);
					PRE_MILLIS = t;

					long id = (t - BEGIN_TIME_STAMP) << 22;
					id += (NODE << 10);
					id += count.incrementAndGet();
					return id;
				}
			} finally {
				LOCK.unlock();
			}
		}
		long id = (t - BEGIN_TIME_STAMP) << 22;
		id += (NODE << 10);
		id += count.incrementAndGet();
		return id;

	}

}
