package tech.codingless.biz.core.util;

import java.security.InvalidKeyException;
import java.security.Key;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.Signature;
import java.security.SignatureException;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;

import javax.crypto.Cipher;

import org.apache.commons.codec.binary.Base64;

public class RsaUtil { 
	private static String PUB_KEY = StringUtil.EMPTY_STR;
	private static String RSA_PRIVATE_PKCS8_KEY = StringUtil.EMPTY_STR;
	public static final String KEY_ALGORTHM = "RSA";//
	public static final String SIGNATURE_ALGORITHM = "SHA1WithRSA";

	   
	public static String getPubkey() {
		return PUB_KEY;
	}

	/**
	 * 公钥验证
	 * 
	 * @throws NoSuchAlgorithmException
	 * @throws InvalidKeySpecException
	 * @throws SignatureException
	 * @throws InvalidKeyException
	 */
	public static boolean verify(String src, String signStr) throws NoSuchAlgorithmException, InvalidKeySpecException, SignatureException, InvalidKeyException {
		KeyFactory keyFactory = KeyFactory.getInstance(KEY_ALGORTHM);

		X509EncodedKeySpec ps = new X509EncodedKeySpec(Base64.decodeBase64(PUB_KEY));
		PublicKey pkey = keyFactory.generatePublic(ps);

		Signature sign = Signature.getInstance(SIGNATURE_ALGORITHM);
		sign.initVerify(pkey);
		sign.update(src.getBytes());
		return sign.verify(Base64.decodeBase64(signStr));
	}

	/**
	 * 私钥签名
	 * 
	 * @throws NoSuchAlgorithmException
	 * @throws InvalidKeySpecException
	 * @throws SignatureException
	 * @throws InvalidKeyException
	 */
	public static String sign(String srcString) throws NoSuchAlgorithmException, InvalidKeySpecException, InvalidKeyException, SignatureException {
		KeyFactory keyFactory = KeyFactory.getInstance(KEY_ALGORTHM);
		PKCS8EncodedKeySpec pkcs8 = new PKCS8EncodedKeySpec(Base64.decodeBase64(RSA_PRIVATE_PKCS8_KEY));
		PrivateKey privateKey = keyFactory.generatePrivate(pkcs8);
		Signature sign = Signature.getInstance(SIGNATURE_ALGORITHM);
		sign.initSign(privateKey);
		sign.update(srcString.getBytes());
		return Base64.encodeBase64String(sign.sign());
	}

	public static byte[] encryptByPublicKey(byte[] data, String key) throws Exception {
		// 对公钥解密
		byte[] keyBytes = Base64.decodeBase64(key);
		// 取公钥
		X509EncodedKeySpec x509EncodedKeySpec = new X509EncodedKeySpec(keyBytes);
		KeyFactory keyFactory = KeyFactory.getInstance(KEY_ALGORTHM);
		Key publicKey = keyFactory.generatePublic(x509EncodedKeySpec);

		// 对数据解密
		Cipher cipher = Cipher.getInstance(keyFactory.getAlgorithm());
		cipher.init(Cipher.ENCRYPT_MODE, publicKey);

		return cipher.doFinal(data);
	}

	public static String encryptWithPublic(String str, String key) {
		try {
			return java.util.Base64.getEncoder().encodeToString(encryptByPublicKey(str.getBytes(), key));
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	public static String encryptWithPublic(String str) {
		try {
			return java.util.Base64.getEncoder().encodeToString(encryptByPublicKey(str.getBytes(), PUB_KEY));
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	public static byte[] decryptByPublicKey(byte[] data, String key) throws Exception {
		// 对私钥解密
		byte[] keyBytes = Base64.decodeBase64(key);
		X509EncodedKeySpec x509EncodedKeySpec = new X509EncodedKeySpec(keyBytes);
		KeyFactory keyFactory = KeyFactory.getInstance(KEY_ALGORTHM);
		Key publicKey = keyFactory.generatePublic(x509EncodedKeySpec);

		// 对数据解密
		Cipher cipher = Cipher.getInstance(keyFactory.getAlgorithm());
		cipher.init(Cipher.DECRYPT_MODE, publicKey);

		return cipher.doFinal(data);
	}

	public static String decryptByPublicKey(String encryStr, String key) throws Exception {
		return new String(decryptByPublicKey(java.util.Base64.getDecoder().decode(encryStr), key));
	}

	/**
	 * 用私钥加密
	 * 
	 * @param data 加密数据
	 * @param key  密钥
	 * @return
	 * @throws Exception
	 */
	public static byte[] encryptByPrivateKey(byte[] data, String key) throws Exception {
		// 解密密钥
		byte[] keyBytes = Base64.decodeBase64(key);
		// 取私钥
		PKCS8EncodedKeySpec pkcs8EncodedKeySpec = new PKCS8EncodedKeySpec(keyBytes);
		KeyFactory keyFactory = KeyFactory.getInstance(KEY_ALGORTHM);
		Key privateKey = keyFactory.generatePrivate(pkcs8EncodedKeySpec);

		// 对数据加密
		Cipher cipher = Cipher.getInstance(keyFactory.getAlgorithm());
		cipher.init(Cipher.ENCRYPT_MODE, privateKey);

		return cipher.doFinal(data);
	}

	public static String encryptByPrivateKey(String str, String key) {
		try {
			return java.util.Base64.getEncoder().encodeToString(encryptByPrivateKey(str.getBytes(), key));
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * 用私钥解密
	 * 
	 * @param data 加密数据
	 * @param key  密钥
	 * @return
	 * @throws Exception
	 */
	public static byte[] decryptByPrivateKey(byte[] data, String key) throws Exception {
		byte[] keyBytes = Base64.decodeBase64(key);
		PKCS8EncodedKeySpec pkcs8EncodedKeySpec = new PKCS8EncodedKeySpec(keyBytes);
		KeyFactory keyFactory = KeyFactory.getInstance(KEY_ALGORTHM);
		Key privateKey = keyFactory.generatePrivate(pkcs8EncodedKeySpec);
		// 对数据解密
		Cipher cipher = Cipher.getInstance(keyFactory.getAlgorithm());
		cipher.init(Cipher.DECRYPT_MODE, privateKey);

		return cipher.doFinal(data);
	}

	public static String decryptWithPrivate(String encryptBase64Str) throws Exception {
		return new String(decryptByPrivateKey(java.util.Base64.getDecoder().decode(encryptBase64Str), RSA_PRIVATE_PKCS8_KEY));
	}

	public static String decryptWithPrivate(String encryptBase64Str, String key) throws Exception {
		return new String(decryptByPrivateKey(java.util.Base64.getDecoder().decode(encryptBase64Str), key));
	}

	public class KeyPairVO {
		private String pubKey;
		private String priKey;

		public String getPubKey() {
			return pubKey;
		}

		public void setPubKey(String pubKey) {
			this.pubKey = pubKey;
		}

		public String getPriKey() {
			return priKey;
		}

		public void setPriKey(String priKey) {
			this.priKey = priKey;
		}

	}

	/**
	 * 生成一对新的秘钥对
	 * 
	 * @return
	 * @throws NoSuchAlgorithmException
	 */
	public static KeyPairVO newKeyPair(int keysize) throws NoSuchAlgorithmException {
		KeyPairVO keyPairVO = new RsaUtil().new KeyPairVO();
		KeyPairGenerator keyPairGen = KeyPairGenerator.getInstance(KEY_ALGORTHM);
		keyPairGen.initialize(keysize);
		KeyPair keyPair = keyPairGen.generateKeyPair();
		RSAPublicKey pubKey = (RSAPublicKey) keyPair.getPublic();
		RSAPrivateKey priKey = (RSAPrivateKey) keyPair.getPrivate();
		keyPairVO.setPubKey(java.util.Base64.getEncoder().encodeToString(((Key) pubKey).getEncoded()));
		keyPairVO.setPriKey(java.util.Base64.getEncoder().encodeToString(((Key) priKey).getEncoded()));
		return keyPairVO;
	}
}
