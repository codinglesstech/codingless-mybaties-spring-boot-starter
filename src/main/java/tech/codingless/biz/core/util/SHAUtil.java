package tech.codingless.biz.core.util;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import org.apache.commons.codec.binary.Base64;

public class SHAUtil {

	/**
	 * sha256 签名
	 * 
	 * @param salt
	 * @param data
	 * @return
	 * @throws NoSuchAlgorithmException
	 */
	public static String sign(String salt, String data) throws NoSuchAlgorithmException {
		MessageDigest md = MessageDigest.getInstance("SHA-256");
		md.update(salt.getBytes());
		return Base64.encodeBase64String(md.digest(data.getBytes()));
	}

	public static boolean verifySign(String salt, String data, String sign) throws NoSuchAlgorithmException {
		MessageDigest md = MessageDigest.getInstance("SHA-256");
		md.update(salt.getBytes());
		return Base64.encodeBase64String(md.digest(data.getBytes())).equals(sign);
	}

}
