package com.tianji.api.client.remark.remark;


import com.tianji.api.client.remark.RemarkClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.openfeign.FallbackFactory;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * RemarkClient降级类
 */
@Slf4j
public class RemarkClientFallback implements FallbackFactory<RemarkClient> {
    // 如果remark服务停止没启动或者其他服务调用remark服务超时则会走create降级
    @Override
    public RemarkClient create(Throwable cause) {
        log.error("查询remark-service服务异常", cause);
        return new RemarkClient() {
            /**
             * 批量查询点赞状态
             *
             * @param bizIds 业务id
             * @return 点赞的id
             */
            @Override

            public Set<Long> getLikesStatusByBizIds(List<Long> bizIds) {
                return new HashSet<>();
            }
        };
    }
}