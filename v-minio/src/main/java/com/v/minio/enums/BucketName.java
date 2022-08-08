package com.v.minio.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.joda.time.DateTime;

/**
 * Created by angry_beard on 2021/5/14.
 */
@Getter
@AllArgsConstructor
public enum BucketName {

    GENERAL_V("general-v"),
    ENCRYPT_V("encrypt-v");

    private final String namePre;

    public static String bucketName(BucketName bucketName) {
        return bucketName.getNamePre() + "-" + DateTime.now().toString("yyyyMM");
    }
}
