package tech.codingless.core.plugs.mybaties3.util;

import java.io.UnsupportedEncodingException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;
import java.util.Base64;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.KeyGenerator;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;

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
			return new String(Base64.getEncoder().encode(key.getEncoded()));
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
			return new String(Base64.getEncoder().encode(key.getEncoded()));
		} catch (Exception e) {
			e.printStackTrace();
			return EMPTY_STR;
		}
	}

	private static final byte[] SALT = { (byte) 0xA9, (byte) 0x9B, (byte) 0xC8, (byte) 0x32, (byte) 0x56, (byte) 0x35, (byte) 0xE3, (byte) 0x03 };
	private static final int ITERATION_COUNT = 65536;
	private static final int KEY_LENGTH = 128;

	public static String genFiexdSecret(String salt) throws NoSuchAlgorithmException, InvalidKeySpecException {
		SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1");
		KeySpec spec = new PBEKeySpec(salt.toCharArray(), SALT, ITERATION_COUNT, KEY_LENGTH);
		SecretKey tmp = factory.generateSecret(spec);
		SecretKey secret = new SecretKeySpec(tmp.getEncoded(), AES);
		return Base64.getEncoder().encodeToString(secret.getEncoded());
	}

	public static String encrypt(String base64Secret, String data)
			throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException, UnsupportedEncodingException {

		byte[] secret = Base64.getDecoder().decode(base64Secret);
		Cipher cipher = Cipher.getInstance(AES_ECB_PKCS5Padding);
		SecretKeySpec key = new SecretKeySpec(secret, AES); // 使用密钥创建AES的key
		cipher.init(Cipher.ENCRYPT_MODE, key); // 加密初始化
		return Base64.getEncoder().encodeToString(cipher.doFinal(data.getBytes(UTF8))); // 对数据加密后返回base64编码后的字符串

	}

	public static String decrypt(String base64Secret, String data)
			throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, UnsupportedEncodingException, IllegalBlockSizeException, BadPaddingException {
		byte[] secret = Base64.getDecoder().decode(base64Secret);
		Cipher cipher = Cipher.getInstance(AES_ECB_PKCS5Padding);
		SecretKeySpec key = new SecretKeySpec(secret, AES);
		cipher.init(Cipher.DECRYPT_MODE, key); // 解密初始化
		return new String(cipher.doFinal(Base64.getDecoder().decode(data)), UTF8);

	}

}
