package com.v.encrypt.utils;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Random;

public abstract class AESUtil {
    private AESUtil() {
    }

    private static Random RANDOM;

    static {
        try {
            RANDOM = SecureRandom.getInstance("SHA1PRNG");
        } catch (NoSuchAlgorithmException ex) {
        }
    }

    /**
     * 生成随机16字节
     *
     * @return
     */
    public static byte[] randomKey() {
        byte[] aesKey = new byte[16];
        RANDOM.nextBytes(aesKey);
        return aesKey;

    }

    /**
     * encrypt aes
     *
     * @param msg
     * @param key
     * @return
     * @throws GeneralSecurityException
     */
    public static byte[] encrypt(byte[] msg, byte[] key) throws GeneralSecurityException {
        Cipher cipher = Cipher.getInstance("AES");
        SecretKey secretKey = new SecretKeySpec(key, "AES");
        cipher.init(Cipher.ENCRYPT_MODE, secretKey);
        return cipher.doFinal(msg);
    }

    /**
     * decrypt aes
     *
     * @param msg
     * @param key
     * @return
     * @throws GeneralSecurityException
     */
    public static byte[] decrypt(byte[] msg, byte[] key) throws GeneralSecurityException {
        Cipher cipher = Cipher.getInstance("AES");
        SecretKey secretKey = new SecretKeySpec(key, "AES");
        cipher.init(Cipher.DECRYPT_MODE, secretKey);
        return cipher.doFinal(msg);
    }

    public static void main(String[] args) throws Exception {
        byte[] key = randomKey();
        System.out.println(key);
        byte[] a = encrypt("hello".getBytes(StandardCharsets.UTF_8), key);
        System.out.println(new String(a));
        byte[] b = decrypt(a, key);
        System.out.println(new String(b));
    }
}
