package com.v.encrypt.utils;

import com.v.encrypt.config.RSAConfig;
import lombok.AllArgsConstructor;
import org.apache.commons.io.IOUtils;
import org.springframework.stereotype.Component;

import java.io.*;
import java.security.GeneralSecurityException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

/**
 * Created by angry_beard on 2021/5/14.
 */
@Component
@AllArgsConstructor
public class EncryptUtils {

    private final RSAConfig rsaConfig;

    public InputStream encrypt(InputStream source, String originalFilename) throws IOException, GeneralSecurityException {
        //得到原始文件内容
        byte[] sourceArr = IOUtils.toByteArray(source);
        //step 1. 先压缩
        ByteArrayOutputStream zipFilContainer = new ByteArrayOutputStream();
        try (ZipOutputStream zos = new ZipOutputStream(zipFilContainer)) {
            zos.putNextEntry(new ZipEntry(originalFilename));
            zos.write(sourceArr);
        }
        //得到压缩后的文件内容
        byte[] zipArr = zipFilContainer.toByteArray();
        //step 2. 进行AES加密
        //生成随机AES密钥
        byte[] aesKey = AESUtil.randomKey();
        //得到文档中描述的 file.tmp
        byte[] fileTmpArr = AESUtil.encrypt(zipArr, aesKey);
        //step 3. 将AES密钥使用  对方公钥进行加密,得到文档中描述的bytesA
        byte[] bytesA = RSAUtil.encrypt(aesKey, rsaConfig.getPublicKey());
        //step 4. 将原始文件内容进行签名,使用已方私钥,得到文档中描述的bytesB
        byte[] bytesB = RSAUtil.signSha256(sourceArr, rsaConfig.getPrivateKey());
        //step LAST, 进行拼接
        try (ByteArrayOutputStream fos = new ByteArrayOutputStream();
             DataOutputStream dos = new DataOutputStream(fos)) {
            //bytesA的长度
            //bytesA的长度
            dos.writeShort(bytesA.length);
            //bytesA
            dos.write(bytesA);
            //bytesB的长度
            dos.writeShort(bytesB.length);
            //bytesB
            dos.write(bytesB);
            //file.tmp
            dos.write(fileTmpArr);
            return new ByteArrayInputStream(fos.toByteArray());
        }
    }

    public InputStream decrypt(InputStream is) throws IOException, GeneralSecurityException {
        try (DataInputStream dis = new DataInputStream(is)) {
            //读取bytesA的长度
            int len = dis.readShort();
            byte[] bytesA = new byte[len];
            //得到bytesA
            dis.readFully(bytesA);
            //读取bytesB的长度
            len = dis.readShort();
            byte[] bytesB = new byte[len];
            //得到bytesB
            dis.readFully(bytesB);
            //得到剩余部分, 即加密时的file.tmp
            byte[] fileTmpArr = IOUtils.toByteArray(dis);
            //解密得到aesKey
            byte[] aesKey = RSAUtil.decryptByPrivateKey(bytesA, rsaConfig.getPrivateKey());
            //对fileTmpArr进行解密
            byte[] zipArr = AESUtil.decrypt(fileTmpArr, aesKey);
            //对zipArr进行解压
            ByteArrayOutputStream bos = new ByteArrayOutputStream(4096);
            try (ZipInputStream zis = new ZipInputStream(new ByteArrayInputStream(zipArr))) {
                zis.getNextEntry();
                IOUtils.copy(zis, bos);
            }
            //得到明文文件内容
            byte[] txtArr = bos.toByteArray();
            //下面进行验签
            if (!RSAUtil.verifySha256(txtArr, rsaConfig.getPublicKey(), bytesB)) {
                throw new GeneralSecurityException("验签失败");
            }
            try (ByteArrayOutputStream fos = new ByteArrayOutputStream();
                 DataOutputStream dos = new DataOutputStream(fos)) {
                dos.write(txtArr);
                return new ByteArrayInputStream(fos.toByteArray());
            }
        }
    }
}
