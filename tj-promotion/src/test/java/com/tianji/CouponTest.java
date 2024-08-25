package com.tianji;

import com.tianji.promotion.PromotionApplication;
import com.tianji.promotion.domain.po.UserCoupon;
import com.tianji.promotion.enums.UserCouponStatus;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@SpringBootTest(classes = PromotionApplication.class)
class CouponTest {
    @Test
    void test() {
        List<UserCoupon> list = new ArrayList<>();
        UserCoupon c1 = new UserCoupon();
        c1.setUserId(1L);
        c1.setCouponId(101L);
        c1.setStatus(UserCouponStatus.USED);
        UserCoupon c2 = new UserCoupon();
        c2.setUserId(1L);
        c2.setCouponId(102L);
        c2.setStatus(UserCouponStatus.USED);
        UserCoupon c3 = new UserCoupon();
        c3.setUserId(1L);
        c3.setCouponId(101L);
        c3.setStatus(UserCouponStatus.UNUSED);
        list.add(c1);
        list.add(c2);
        list.add(c3);

        Map<Long, Long> issueMap = new HashMap<>();
        for (UserCoupon userCoupon : list) {
            // 优惠券领取数量
            Long num = issueMap.get(userCoupon.getCouponId());
            if (num == null) {
                issueMap.put(userCoupon.getCouponId(), 1L);
            } else {
                issueMap.put(userCoupon.getCouponId(), num + 1);
            }
        }
        System.out.println("issueMap = " + issueMap);
        // stream
        Map<Long, Long> longMap = list.stream().collect(Collectors.groupingBy(UserCoupon::getCouponId, Collectors.counting()));
        System.out.println("longMap = " + longMap);
        // stream
        Map<Long, Long> long2Map = list.stream().filter(c -> c.getStatus() == UserCouponStatus.UNUSED).collect(Collectors.groupingBy(UserCoupon::getCouponId, Collectors.counting()));
        System.out.println("long2Map = " + long2Map);
    }
}
