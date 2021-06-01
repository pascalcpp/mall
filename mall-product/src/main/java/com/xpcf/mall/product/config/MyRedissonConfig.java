package com.xpcf.mall.product.config;

import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;

/**
 * TODO
 *
 * @author XPCF
 * @version 1.0
 * @date 12/29/2020 12:26 PM
 */
@Configuration
public class MyRedissonConfig {

    @Bean(destroyMethod="shutdown")
    RedissonClient redisson(@Value("${spring.redis.host}") String url) throws IOException {
        Config config = new Config();
        config.useSingleServer().setAddress("redis://" + url + ":6379");

        return Redisson.create(config);
    }

}
