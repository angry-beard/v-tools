package com.v.encrypt.utils;

import org.apache.commons.codec.binary.Base64;

import javax.crypto.Cipher;
import java.security.*;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.HashMap;
import java.util.Map;


public class RSAUtil {

    private RSAUtil() {
    }

    public static final String KEY_ALGORITHM = "RSA";
    public static final String SIGNATURE_ALGORITHM_SHA256 = "SHA256withRSA";

    /**
     * 使用公钥加密
     *
     * @param data
     * @param pubKey
     * @return
     */
    public static byte[] encrypt(byte[] data, String pubKey) throws GeneralSecurityException {
        byte[] keyBytes = Base64.decodeBase64(pubKey);

        X509EncodedKeySpec x509KeySpec = new X509EncodedKeySpec(keyBytes);
        KeyFactory keyFactory = KeyFactory.getInstance(KEY_ALGORITHM);
        PublicKey publicKey = keyFactory.generatePublic(x509KeySpec);

        Cipher cipher = Cipher.getInstance(keyFactory.getAlgorithm());
        cipher.init(Cipher.ENCRYPT_MODE, publicKey);
        return cipher.doFinal(data);
    }

    /**
     * 使用私钥解密
     *
     * @param data
     * @param priKey
     * @return
     */
    public static byte[] decryptByPrivateKey(byte[] data, String priKey) throws GeneralSecurityException {
        byte[] keyBytes = Base64.decodeBase64(priKey);

        PKCS8EncodedKeySpec pkcs8KeySpec = new PKCS8EncodedKeySpec(keyBytes);
        KeyFactory keyFactory = KeyFactory.getInstance(KEY_ALGORITHM);
        PrivateKey privateKey = keyFactory.generatePrivate(pkcs8KeySpec);

        Cipher cipher = Cipher.getInstance(keyFactory.getAlgorithm());
        cipher.init(Cipher.DECRYPT_MODE, privateKey);


        return cipher.doFinal(data);

    }

    /**
     * sign with SHA256WithRSA
     *
     * @param data
     * @param privateKey
     * @return
     */
    public static byte[] signSha256(byte[] data, String privateKey) throws GeneralSecurityException {
        byte[] keyBytes = Base64.decodeBase64(privateKey);

        PKCS8EncodedKeySpec pkcs8KeySpec = new PKCS8EncodedKeySpec(keyBytes);
        KeyFactory keyFactory = KeyFactory.getInstance(KEY_ALGORITHM);

        PrivateKey priKey = keyFactory.generatePrivate(pkcs8KeySpec);

        Signature signature = Signature.getInstance(SIGNATURE_ALGORITHM_SHA256);
        signature.initSign(priKey);
        signature.update(data);

        return signature.sign();
    }

    /**
     * verify with SHA256RSA
     *
     * @param data
     * @param publicKey
     * @param sign
     * @return
     */
    public static boolean verifySha256(byte[] data, String publicKey, byte[] sign) throws GeneralSecurityException {
        byte[] keyBytes = Base64.decodeBase64(publicKey);

        X509EncodedKeySpec keySpec = new X509EncodedKeySpec(keyBytes);

        KeyFactory keyFactory = KeyFactory.getInstance(KEY_ALGORITHM);

        PublicKey pubKey = keyFactory.generatePublic(keySpec);

        Signature signature = Signature.getInstance(SIGNATURE_ALGORITHM_SHA256);
        signature.initVerify(pubKey);
        signature.update(data);

        return signature.verify(sign);
    }

    public static Map<String, String> initRSAKey() throws Exception {
        //为RSA算法创建一个KeyPairGenerator对象
        KeyPairGenerator kpg = KeyPairGenerator.getInstance("RSA");
        kpg.initialize(2048);
        //生成密匙对
        KeyPair keyPair = kpg.generateKeyPair();
        //得到公钥
        Key publicKey = keyPair.getPublic();
        String publicKeyStr = Base64.encodeBase64URLSafeString(publicKey.getEncoded());
        //得到私钥
        Key privateKey = keyPair.getPrivate();
        String privateKeyStr = Base64.encodeBase64URLSafeString(privateKey.getEncoded());
        Map<String, String> keyPairMap = new HashMap<>();
        keyPairMap.put("publicKey", publicKeyStr);
        keyPairMap.put("privateKey", privateKeyStr);
        return keyPairMap;
    }
}
