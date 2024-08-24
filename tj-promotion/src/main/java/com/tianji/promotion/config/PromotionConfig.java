package com.tianji.promotion.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * 自定义线程池
 */
@Slf4j
@Configuration
public class PromotionConfig {
    @Bean
    public ThreadPoolTaskExecutor generateExchangeCodeExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        // 1.核心线程池大小
        executor.setCorePoolSize(2);
        // 2.最大线程池大小
        executor.setMaxPoolSize(5);
        // 3.队列最大容量大小
        executor.setQueueCapacity(200);
        // 4.线程名称
        executor.setThreadNamePrefix("exchange-code-handler-");
        // 5.拒绝策略
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        executor.initialize();
        return executor;
    }
    @Bean
    public Executor calculteSolutionExecutor(){
        ThreadPoolTaskExecutor refundExecutor = new ThreadPoolTaskExecutor();
        //配置核心线程数
        refundExecutor.setCorePoolSize(10);
        //配置最大线程数
        refundExecutor.setMaxPoolSize(10);
        //配置队列大小
        refundExecutor.setQueueCapacity(200);
        //配置线程池中的线程的名称前缀
        refundExecutor.setThreadNamePrefix("calculte-solution-handler-");
        // 由调用者线程执行
        refundExecutor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        refundExecutor.initialize();
        return refundExecutor;
    }
}
