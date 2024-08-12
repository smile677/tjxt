package com.tianji.learning;

import java.util.Calendar;
import java.util.Date;
import java.util.concurrent.DelayQueue;
import java.util.concurrent.Delayed;
import java.util.concurrent.TimeUnit;

public class MyDelayedTask implements Delayed {
    // 执行任务的时间
    private int executeTime = 0; //代表元素执行时间
    private String name; //元素名称

    /**
     * @param delay 元素延迟多久执行
     * @param name
     */
    public MyDelayedTask(int delay, String name) {
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.SECOND, delay);
        this.name = name;
        this.executeTime = (int) (calendar.getTimeInMillis() / 1000);
    }

    /**
     * 元素在队列中的剩余时间
     *
     * @param unit the time unit
     * @return
     */
    @Override
    public long getDelay(TimeUnit unit) {
        Calendar calendar = Calendar.getInstance();
        return executeTime - (int) (calendar.getTimeInMillis() / 1000);
    }

    /**
     * 元素排序
     *
     * @param o the object to be compared.
     * @return
     */
    @Override
    public int compareTo(Delayed o) {
        long val = this.getDelay(TimeUnit.NANOSECONDS) - o.getDelay(TimeUnit.NANOSECONDS);
        return val == 0 ? 0 : (val < 0 ? -1 : 1);
    }

    @Override
    public String toString() {
        return "MyDelayedTask{" +
                "executeTime=" + executeTime +
                ", name='" + name + '\'' +
                '}';
    }

    public static void main(String[] args) throws InterruptedException {
        DelayQueue<MyDelayedTask> queue = new DelayQueue<>();
        queue.add(new MyDelayedTask(10, "task1"));
        queue.add(new MyDelayedTask(5, "task2"));
        queue.add(new MyDelayedTask(15, "task3"));
        System.out.println("new Date() = " + new Date());
        while (queue.size() != 0) {
//            MyDelayedTask delayedTask = queue.poll();// 从队列中拉取元素 poll非阻塞方法
            MyDelayedTask delayedTask = queue.take();// 从队列中拉取元素 poll阻塞方法
//            while (delayedTask!=null){
            System.out.println(new Date() + "delayedTask = " + delayedTask);
//            }
            // 每隔一秒消费一次
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}
