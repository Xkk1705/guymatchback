package com.xk.usercenter.config;

import lombok.Data;
import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "spring.redis")
@Data
public class RedissonConfig {
    private String host;

    private String port;
    @Bean
    public RedissonClient redissonClient() {
        // 1. Create config object
        Config config = new Config();
        String address = String.format("redis://%s:%s",host,port);
        config.useSingleServer()
                // use "rediss://" for SSL connection
                .setAddress(address)
                .setDatabase(3);
        return Redisson.create(config);
    }
}
