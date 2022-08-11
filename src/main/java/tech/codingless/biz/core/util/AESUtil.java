package tech.codingless.biz.core.util;

import java.security.SecureRandom;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

import org.apache.commons.codec.binary.Base64;

import com.alibaba.fastjson.JSONObject;

public class AESUtil {
	private static final String EMPTY_STR = "";
	private static final String AES = "AES";
	private static final String UTF8 = "utf-8";
	private static final String SHA1PRNG = "SHA1PRNG";
	private static final String AES_ECB_PKCS5Padding = "AES/ECB/PKCS5Padding";

	public enum KeyLen {
		LEN_128(128), LEN_192(192), LEN_256(256),;
		private int len;

		private KeyLen(int len) {
			this.len = len;
		}

		public int getLen() {
			return this.len;
		}
	}

	/**
	 * 
	 * @param keyLen 指定长度密钥
	 * @param salt
	 * @return
	 */
	public static String genNewSecret(KeyLen keyLen, String salt) {
		try {
			KeyGenerator kgen = KeyGenerator.getInstance(AES); // 密钥生成器
			SecureRandom secureRandom = SecureRandom.getInstance(SHA1PRNG); // 创建强随机数对象
			secureRandom.setSeed(salt.getBytes()); // 传入盐值作为种子
			kgen.init(keyLen.len, new SecureRandom(salt.getBytes())); // 创建128位的密钥，AES的密钥有128、192、256
			SecretKey key = kgen.generateKey(); // 生成key
			return new String(Base64.encodeBase64(key.getEncoded()));
		} catch (Exception e) {
			e.printStackTrace();
			return EMPTY_STR;
		}
	}

	public static String genNewSecret(String salt) {
		try {
			KeyGenerator kgen = KeyGenerator.getInstance(AES); // 密钥生成器
			SecureRandom secureRandom = SecureRandom.getInstance(SHA1PRNG); // 创建强随机数对象
			secureRandom.setSeed(salt.getBytes()); // 传入盐值作为种子
			kgen.init(128, new SecureRandom(salt.getBytes())); // 创建128位的密钥，AES的密钥有128、192、256
			SecretKey key = kgen.generateKey(); // 生成key
			return new String(Base64.encodeBase64(key.getEncoded()));
		} catch (Exception e) {
			e.printStackTrace();
			return EMPTY_STR;
		}
	}

	public static String encrypt(String base64Secret, String data) {
		try {
			byte[] secret = Base64.decodeBase64(base64Secret);
			Cipher cipher = Cipher.getInstance(AES_ECB_PKCS5Padding);
			SecretKeySpec key = new SecretKeySpec(secret, AES); // 使用密钥创建AES的key
			cipher.init(Cipher.ENCRYPT_MODE, key); // 加密初始化
			return Base64.encodeBase64String(cipher.doFinal(data.getBytes(UTF8))); // 对数据加密后返回base64编码后的字符串
		} catch (Exception e) {
			e.printStackTrace();
			return EMPTY_STR;
		}
	}

	public static String decrypt(String base64Secret, String data) {
		try {
			byte[] secret = Base64.decodeBase64(base64Secret);
			Cipher cipher = Cipher.getInstance(AES_ECB_PKCS5Padding);
			SecretKeySpec key = new SecretKeySpec(secret, AES);
			cipher.init(Cipher.DECRYPT_MODE, key); // 解密初始化
			return new String(cipher.doFinal(Base64.decodeBase64(data)), UTF8);
		} catch (Exception e) {
			e.printStackTrace();
			return EMPTY_STR;
		}
	}

	public static void main(String[] args) throws Exception {
		String str = "在太阳系，八大行星以太阳为核心公转，形成了8个环形轨道，其中地球位于“三环”，火星则在“四环”运行。天问一号探测器要想冲出地球抵达火星，绝不是简单地从“三环”跨越到“四环”，而是长途跋涉几个月，直线距离突破4亿公里的旅程。";
		System.out.println(AESUtil.genNewSecret("oCaTdqMAfJHXm"));
		System.out.println(AESUtil.genNewSecret("123"));
		String key = "oCaTdqMAfJHXm/z6uHf9qw==";
		System.out.println(AESUtil.encrypt(key, str)); 
		System.out.println(AESUtil.decrypt(key, AESUtil.encrypt(key, str)));
		
		JSONObject json = new JSONObject();
		json.put("buyerName", "Hello ,Wang");
		System.out.println(AESUtil.encrypt("EZfpOga2+q2kINgk6Wo2OQ==", json.toJSONString()));
		System.out.println(AESUtil.encrypt("EZfpOga2+q2kINgk6Wo2OQ==", "1")); 
	}
}
