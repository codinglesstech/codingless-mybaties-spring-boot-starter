package tech.codingless.core.plugs.mybaties3.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;

public class LockerUtil {

	private static final ConcurrentHashMap<String, Locker> LOCKER_MAP = new ConcurrentHashMap<>();
	private static final Locker WHOLE_LOCKER=new Locker();

    public static List<Locker> getLockers(String ... keys) {
		if(keys==null){
			return Collections.emptyList();
		}
		List<String> list = new ArrayList<>();

		for(String key:keys){
			if(MybatiesStringUtil.isNotEmpty(key)){
				list.add(key);
			}
		}
		Collections.sort(list);
		List<Locker> lockers = new ArrayList<>();
		list.forEach(key->{
			lockers.add(getLocker(key));
		});
		return lockers;
    }

    public static class Locker extends ReentrantLock{
	 
		private static final long serialVersionUID = 1L;
		private long t;
		private String key;
		public void setKey(String key) {
			this.key = key;
		}
		public String getKey() {
			return key;
		} 
		public void setT(long t) {
			this.t = t;
		}
		public long getT() {
			return t;
		}

		@Override
		public void lock() { 
			super.lock();
			this.t=System.currentTimeMillis();
		}
	}

	public static Locker getLocker(String key) {
		if(LOCKER_MAP.containsKey(key)) {
			Locker locker = LOCKER_MAP.get(key); 
			return locker;
		}
		synchronized (WHOLE_LOCKER) { 
			if(LOCKER_MAP.containsKey(key)) {
				Locker locker = LOCKER_MAP.get(key); 
				return locker;
			} 
			Locker locker = new Locker(); 
			locker.setKey(key);
			LOCKER_MAP.put(key, locker);
			return locker;
		} 
	}
}
