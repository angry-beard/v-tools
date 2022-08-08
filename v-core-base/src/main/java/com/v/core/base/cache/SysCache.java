package com.v.core.base.cache;

import com.v.core.base.utils.RedisUtil;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

/**
 * @author angry_beard
 * @date 2021/7/30 4:35 下午
 */
@Component
@AllArgsConstructor
public class SysCache {

    private final RedisUtil redisUtil;
    public static RedisUtil redisCache;

    @PostConstruct
    public void init() {
        redisCache = this.redisUtil;
    }

}
