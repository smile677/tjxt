package com.tianji.learning;

import java.time.Duration;
import java.util.Date;
import java.util.concurrent.DelayQueue;
import java.util.concurrent.Delayed;
import java.util.concurrent.TimeUnit;

public class DelayTask<D> implements Delayed {
    private D data;
    private long deadlineNanos;

    public DelayTask(D data, Duration dalayTime) {
        this.data = data;
        this.deadlineNanos = System.nanoTime() + dalayTime.toNanos();
    }

    @Override
    public long getDelay(TimeUnit unit) {
        return unit.convert(Math.max(0, deadlineNanos - System.nanoTime()), TimeUnit.NANOSECONDS);
    }

    @Override
    public int compareTo(Delayed o) {
        long l = getDelay(TimeUnit.NANOSECONDS) - o.getDelay(TimeUnit.NANOSECONDS);
        return l == 0 ? 0 : (l < 0 ? -1 : 1);
    }

    @Override
    public String toString() {
        return "DelayTask{" +
                "data=" + data +
                ", deadlineNanos=" + deadlineNanos +
                '}';
    }

    public static void main(String[] args) throws InterruptedException {
        DelayQueue<DelayTask> delayQueue = new DelayQueue<>();
        delayQueue.add(new DelayTask<>("1", Duration.ofSeconds(5)));
        delayQueue.add(new DelayTask<>("2", Duration.ofSeconds(3)));
        delayQueue.add(new DelayTask<>("3", Duration.ofSeconds(1)));
        System.out.println("new Data() begin = " + new Date() );
        while (!delayQueue.isEmpty()) {
            DelayTask take = delayQueue.take();
            System.out.println("new Data() = " + new Date() + take);
        }
    }
}
