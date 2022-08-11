package tech.codingless.biz.core.util;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ThreadUtil {
	private static final ExecutorService executor = Executors.newFixedThreadPool(2);

	public static void execute(Runnable runnable) {
		executor.execute(runnable);
	}

}
