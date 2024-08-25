package com.tianji.xxljobdemo.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Slf4j
@RequiredArgsConstructor
public class TestController {
    @GetMapping("/test")
    @Async("generateExchangeCodeExecutor")
    public void test() {
        // http-nio-8099-exec-6 不做处理，默认情况下是使用的tomcat默认线程的线程处理该请求
        // task-1说明使用了线程池重点线程 异步的执行
        log.debug(Thread.currentThread().getName() + " 线程开始");
        try {
            Thread.sleep(3000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        log.debug(Thread.currentThread().getName() + " 线程结束");
    }


    private final ThreadPoolTaskExecutor threadPoolExecutor;

    @GetMapping("/test1")
    @Async("generateExchangeCodeExecutor")
    public void test1() {
        log.debug(String.valueOf(threadPoolExecutor));
        log.debug("默认前缀：" + threadPoolExecutor.getThreadNamePrefix());
        log.debug("默认核心线程数" + threadPoolExecutor.getCorePoolSize());
        log.debug("默认最大线程池" + threadPoolExecutor.getMaxPoolSize());
        log.debug("当前活跃线程池数" + threadPoolExecutor.getActiveCount());
        log.debug("临时线程空闲时间" + threadPoolExecutor.getKeepAliveSeconds());
        log.debug("队列最大值" + threadPoolExecutor.getQueueCapacity());
        log.debug("队列数量" + threadPoolExecutor.getQueueSize());

    }
}

