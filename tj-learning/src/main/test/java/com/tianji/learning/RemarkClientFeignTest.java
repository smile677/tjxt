package com.tianji.learning;

import com.tianji.api.client.remark.RemarkClient;
import org.assertj.core.util.Lists;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Set;

@SpringBootTest(classes = LearningApplication.class)
class RemarkClientFeignTest {
    @Autowired
    RemarkClient remarkClient;

    @Test
    void remarkClientTest() {
        Set<Long> bizIds = remarkClient.getLikesStatusByBizIds(
                Lists.list(1824335668462600193L, 1824336675733094401L));
        System.out.println("bizIds = " + bizIds);
    }
}
