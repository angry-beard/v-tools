package com.v.minio.utils;

import com.v.minio.config.MinioConfiguration;
import io.minio.*;
import io.minio.messages.Bucket;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Created by angry_beard on 2021/5/13.
 */
@Slf4j
@Component
@AllArgsConstructor
public class MinioUtils {

    private final MinioClient client;
    private final MinioConfiguration configuration;

    /**
     * 创建bucket
     */
    public void createBucket(String bucketName) throws Exception {
        if (!client.bucketExists(BucketExistsArgs.builder().bucket(bucketName).build())) {
            client.makeBucket(MakeBucketArgs.builder().bucket(bucketName).build());
            //设置匿名访问
            try {
                client.setBucketPolicy(SetBucketPolicyArgs.builder()
                        .bucket(bucketName)
                        .config(getConfig(bucketName))
                        .build());
            } catch (Exception e) {
                log.error("创建桶[{}]失败，detail:", bucketName, e);
                removeBucket(bucketName);
            }
        }
    }

    /**
     * 上传文件
     */
    public String uploadFile(MultipartFile file, String bucketName) throws Exception {
        //判断存储桶是否存在  不存在则创建
        createBucket(bucketName);
        //文件名
        String originalFilename = file.getOriginalFilename();
        //新的文件名 = 存储桶文件名_时间戳.后缀名
        assert originalFilename != null;
        String fileName = getDbName(bucketName, originalFilename);
        //开始上传
        client.putObject(
                PutObjectArgs.builder().bucket(bucketName).object(fileName).stream(
                        file.getInputStream(), file.getSize(), -1)
                        .contentType(file.getContentType())
                        .build());
        return configuration.getEndpoint() + "/" + bucketName + "/" + fileName;
    }

    /**
     * 获取全部bucket
     *
     * @return
     */
    public List<Bucket> getAllBuckets() throws Exception {
        return client.listBuckets();
    }

    /**
     * 根据bucketName获取信息
     *
     * @param bucketName bucket名称
     */
    public Optional<Bucket> getBucket(String bucketName) throws Exception {
        return client.listBuckets().stream().filter(b -> b.name().equals(bucketName)).findFirst();
    }

    /**
     * 根据bucketName删除信息
     *
     * @param bucketName bucket名称
     */
    public void removeBucket(String bucketName) throws Exception {
        if (client.bucketExists(BucketExistsArgs.builder().bucket(bucketName).build())) {
            client.removeBucket(RemoveBucketArgs.builder().bucket(bucketName).build());
        }
    }

    /**
     * 获取⽂件
     *
     * @param bucketName bucket名称
     * @param objectName ⽂件名称
     * @return ⼆进制流
     */
    public InputStream getObject(String bucketName, String objectName) throws Exception {
        return client.getObject(GetObjectArgs.builder().bucket(bucketName).object(objectName).build());
    }


    public String putImg(String bucketName, InputStream stream) throws Exception {
        return putObject(bucketName, "xx.jpeg", stream, "image/jpeg");
    }

    /**
     * 上传⽂件
     *
     * @param bucketName       bucket名称
     * @param originalFilename ⽂件名称
     * @param stream           ⽂件流
     * @throws Exception https://docs.minio.io/cn/java-client-api-reference.html#putObject
     */
    public String putObject(String bucketName, String originalFilename, InputStream stream, String contentType) throws Exception {
        createBucket(bucketName);
        String dbName = getDbName(bucketName, originalFilename);
        client.putObject(PutObjectArgs.builder()
                .bucket(bucketName)
                .object(dbName)
                .stream(stream, stream.available(), -1)
                .contentType(contentType)
                .build());
        return configuration.getEndpoint() + "/" + bucketName + "/" + dbName;
    }

    /**
     * 获取⽂件信息
     *
     * @param bucketName bucket名称
     * @param objectName ⽂件名称
     * @throws Exception https://docs.minio.io/cn/java-client-api-reference.html#statObject
     */
    public ObjectStat getObjectInfo(String bucketName, String objectName) throws Exception {
        return client.statObject(StatObjectArgs.builder().bucket(bucketName).object(objectName).build());
    }

    /**
     * 删除⽂件
     *
     * @param bucketName bucket名称
     * @param objectName ⽂件名称
     * @throws Exception https://docs.minio.io/cn/java-client-apireference.html#removeObject
     */
    public void removeObject(String bucketName, String objectName) throws Exception {
        client.removeObject(RemoveObjectArgs.builder().bucket(bucketName).object(objectName).build());
    }

    private String getDbName(String bucketName, String originalFilename) {
        boolean isEncrypt = bucketName.startsWith("encrypt");
        return bucketName + "-"
                + UUID.randomUUID()
                + (isEncrypt ? ".ect" : originalFilename.substring(originalFilename.lastIndexOf(".")));
    }

    private String getConfig(String bucketName) {
        return "{\n" +
                "  \"Version\": \"2012-10-17\",\n" +
                "  \"Statement\": [\n" +
                "    {\n" +
                "      \"Effect\": \"Allow\",\n" +
                "      \"Principal\": {\n" +
                "        \"AWS\": [\n" +
                "          \"*\"\n" +
                "        ]\n" +
                "      },\n" +
                "      \"Action\": [\n" +
                "        \"s3:GetBucketLocation\",\n" +
                "        \"s3:ListBucket\",\n" +
                "        \"s3:ListBucketMultipartUploads\"\n" +
                "      ],\n" +
                "      \"Resource\": [\n" +
                "        \"arn:aws:s3:::" + bucketName + "\"\n" +
                "      ]\n" +
                "    },\n" +
                "    {\n" +
                "      \"Effect\": \"Allow\",\n" +
                "      \"Principal\": {\n" +
                "        \"AWS\": [\n" +
                "          \"*\"\n" +
                "        ]\n" +
                "      },\n" +
                "      \"Action\": [\n" +
                "        \"s3:AbortMultipartUpload\",\n" +
                "        \"s3:DeleteObject\",\n" +
                "        \"s3:GetObject\",\n" +
                "        \"s3:ListMultipartUploadParts\",\n" +
                "        \"s3:PutObject\"\n" +
                "      ],\n" +
                "      \"Resource\": [\n" +
                "        \"arn:aws:s3:::" + bucketName + "/*\"\n" +
                "      ]\n" +
                "    }\n" +
                "  ]\n" +
                "}";
    }

}
