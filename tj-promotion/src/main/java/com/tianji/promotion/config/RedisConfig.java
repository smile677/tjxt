package com.tianji.promotion.config;

import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RedisConfig {
    @Bean
    public RedissonClient redissonConfig() {
        // 配置类
        Config config = new Config();
        // 添加redis地址，这里添加的是单点的地址，也可以使用config.useClusterServers()来添加集群的地址
        config.useSingleServer()
                .setAddress("redis://192.168.150.101:6379")
                .setPassword("123321");
        // 创建客户端
        return Redisson.create(config);
    }
}
