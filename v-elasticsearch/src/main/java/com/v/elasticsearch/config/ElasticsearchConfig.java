package com.v.elasticsearch.config;

import org.apache.http.HttpHost;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestClientBuilder;
import org.elasticsearch.client.RestHighLevelClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by angry_beard on 2021/3/24.
 */
@Configuration
@ConfigurationProperties(prefix = "elasticsearch")
public class ElasticsearchConfig {
    /**
     * 协议
     */
    @Value("${schema:http}")
    private String schema;
    /**
     * 集群地址，如果有多个用","隔开
     */
    @Value("${address:127.0.0.1:9200}")
    private String address;
    /**
     * 连接超时时间
     */
    @Value("${connectTimeout:5000}")
    private int connectTimeout;
    /**
     * Socket 连接超时时间
     */
    @Value("${socketTimeout:10000}")
    private int socketTimeout;
    /**
     * 获取连接的超时时间
     */
    @Value("${connectionRequestTimeout:5000}")
    private int connectionRequestTimeout;
    /**
     * 最大连接数
     */
    @Value("${maxConnectNum:100}")
    private int maxConnectNum;
    /**
     * 最大路由连接数
     */
    @Value("${maxConnectPerRoute:100}")
    private int maxConnectPerRoute;

    @Bean
    public RestHighLevelClient restHighLevelClient() {
        // 拆分地址
        List<HttpHost> hostLists = new ArrayList<>();
        String[] hostList = address.split(",");
        for (String addr : hostList) {
            String host = addr.split(":")[0];
            String port = addr.split(":")[1];
            hostLists.add(new HttpHost(host, Integer.parseInt(port), schema));
        }
        // 转换成 HttpHost 数组
        HttpHost[] httpHost = hostLists.toArray(new HttpHost[]{});
        // 构建连接对象
        RestClientBuilder builder = RestClient.builder(httpHost);
        // 异步连接延时配置
        builder.setRequestConfigCallback(requestConfigBuilder -> {
            requestConfigBuilder.setConnectTimeout(connectTimeout);
            requestConfigBuilder.setSocketTimeout(socketTimeout);
            requestConfigBuilder.setConnectionRequestTimeout(connectionRequestTimeout);
            return requestConfigBuilder;
        });
        // 异步连接数配置
        builder.setHttpClientConfigCallback(httpClientBuilder -> {
            httpClientBuilder.setMaxConnTotal(maxConnectNum);
            httpClientBuilder.setMaxConnPerRoute(maxConnectPerRoute);
            return httpClientBuilder;
        });
        return new RestHighLevelClient(builder);
    }
}