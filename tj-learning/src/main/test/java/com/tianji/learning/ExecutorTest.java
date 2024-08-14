package com.tianji.learning;

import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class ExecutorTest {
    public static void main(String[] args) {
        // 创建线程池  阿里规约  OOM内存溢出
        Executors.newFixedThreadPool(3);// 创建固定线程数的线程池
        Executors.newSingleThreadExecutor();// 创建单线程的线程池
        Executors.newCachedThreadPool();// 创建缓存线程池
        Executors.newScheduledThreadPool(3);// 创建可以延迟执行的线程池
        System.out.println(Runtime.getRuntime().availableProcessors());
        ThreadPoolExecutor poolExecutor = new ThreadPoolExecutor(
                24,
                25,
                // 临时空闲线程存活时间
                60,
                TimeUnit.SECONDS,
                // 延迟阻塞队列
                new LinkedBlockingDeque<>(10));
        // 建议1: 如果任务是属于CPU运算型任务， 推荐核心线程数为CPU的核数
        // 建议2: 如果任务是属于IO型的任务，推荐核心线程数为CPU核数的2倍
        poolExecutor.submit(new Runnable() {
            @Override
            public void run() {
                // 任务
            }
        });

    }

}
