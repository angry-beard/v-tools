package com.v.encrypt.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

/**
 * @author angry_beard
 * @date 2021/7/7 10:07 上午
 */
@Getter
@Setter
@Configuration
public class RSAConfig {

    @Value("${rsa.keys.file-public-key:pubKey}")
    private String publicKey;
    @Value("${rsa.keys.file-private-key:priKey}")
    private String privateKey;
}
