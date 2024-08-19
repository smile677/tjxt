package com.tianji.xxljobdemo.job;

import com.xxl.job.core.handler.annotation.XxlJob;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class XxlJobDemo {

    @XxlJob("xxljobtest")
     public void test(){
         log.info("xxl-job demo 任务执行了");
     }
}
