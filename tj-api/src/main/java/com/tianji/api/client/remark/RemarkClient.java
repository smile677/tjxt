package com.tianji.api.client.remark;

import com.tianji.api.client.remark.remark.RemarkClientFallback;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;
import java.util.Set;

// 被调用方的服务名
@FeignClient(value = "remark-service", fallbackFactory = RemarkClientFallback.class)
public interface RemarkClient {
    /**
     * 批量查询点赞状态
     *
     * @param bizIds 业务id
     * @return 点赞的id
     */
    @GetMapping("/likes/list")
    Set<Long> getLikesStatusByBizIds(@RequestParam("bizIds") List<Long> bizIds);
}
