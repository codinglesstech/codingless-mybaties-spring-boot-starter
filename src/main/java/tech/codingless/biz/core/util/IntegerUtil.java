package tech.codingless.biz.core.util;

import java.util.Random;

public class IntegerUtil {
	private static Random random  = new Random();
	/**
	 * 获得一个随机数N , 满足  min <= N <= max
	 * @param min
	 * @param max
	 * @return
	 */
	public static int random(int min, int max) { 
		return random.nextInt(max+1-min)+min; 
	}

}
