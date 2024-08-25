package com.tianji.xxljobdemo.job;

import com.xxl.job.core.context.XxlJobHelper;
import com.xxl.job.core.handler.annotation.XxlJob;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Date;

@Component
@Slf4j
public class XxlJobDemo {

    @XxlJob("xxljobtest") // 采用分片广播的路由策略 执行一次
    public void test() throws InterruptedException {
        log.debug("new Date() = " + new Date());
        // 当前实例的分片索引，是从0开始的
        int shardIndex = XxlJobHelper.getShardIndex();
        // 分片总实例
        int shardTotal = XxlJobHelper.getShardTotal();
        log.debug("shardIndex = " + shardIndex + "  shardTotal = " + shardTotal);

        // shardIndex: 0/1 shardtotal:2
        // 第一实例 shardIndex如果为0  处理的页码有：1 3 5 7
        // 第二实例 shardIndex如果为1  处理的页码有：2 4 6 8

        int pageNo = shardIndex + 1;
//        int pageSize = 1000;
        while (true) {
            log.debug("处理第" + pageNo + "页");
            Thread.sleep(1000);
            if (pageNo >= 10) {
                break;
            }
            pageNo += shardTotal;
        }
    }
}
