package com.xk.usercenter;

import org.junit.jupiter.api.Test;
import org.redisson.api.RList;
import org.redisson.api.RedissonClient;
import org.springframework.boot.test.context.SpringBootTest;

import javax.annotation.Resource;

@SpringBootTest
public class redissonTest {


    @Resource
    private RedissonClient redissonClient;
    @Test
    public void testRedisson() {
        RList<Object> rList = redissonClient.getList("rList");
        rList.add("xk");
        System.out.println(rList.get(0));
    }

}
