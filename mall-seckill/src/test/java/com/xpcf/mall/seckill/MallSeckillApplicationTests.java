package com.xpcf.mall.seckill;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Arrays;

@SpringBootTest
@RunWith(SpringRunner.class)
public class MallSeckillApplicationTests {

    @Autowired
    StringRedisTemplate redisTemplate;

    @Test
    public void test1() {
        redisTemplate.opsForList().leftPushAll("test", Arrays.asList("1", "2", "3"));
    }
}
