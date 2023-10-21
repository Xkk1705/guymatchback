package com.xk.usercenter.job;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xk.usercenter.model.domain.User;
import com.xk.usercenter.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;


/**
 * 定时任务 每天定时更新缓存中的信息
 */
@Component
@Slf4j
public class TimeUpdateUserCache {
    @Resource
    private RedisTemplate<String, Object> redisTemplate;

    @Resource
    private UserService userService;

    @Resource
    private RedissonClient redissonClient;

    // 给重点用户缓存预热
    List<Long> vipUserList = Arrays.asList(6L);

    @Scheduled(cron = "0 12 20 * * ?")   //每天2点执行一次
    public void execute() {

        RLock lock = redissonClient.getLock("xk:onetime:task:vipmessage");
        try {
            if (lock.tryLock(0, 10000, TimeUnit.MINUTES)) {
                System.out.println("获取到锁了");
                for (Long userid : vipUserList) {
                    String key = "cache:xk:user:" + userid;
                    ValueOperations<String, Object> operations = redisTemplate.opsForValue();
                    Page<User> userPage = new Page<>(1, 30);
                    Page<User> userList = userService.page(userPage, new QueryWrapper<>());
                    operations.set(key, userList, 12, TimeUnit.MILLISECONDS);
                }
            }
        } catch (InterruptedException e) {
            log.error("vip用户缓存失败");
        }finally {
            if (lock.isHeldByCurrentThread()) {
                System.out.println("释放掉锁了");
                lock.unlock();
            }
        }

    }
}
