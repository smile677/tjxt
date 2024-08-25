package com.tianji.promotion.mapper;


import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.tianji.promotion.domain.po.Coupon;
import com.tianji.promotion.domain.po.UserCoupon;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
* @author smile67
* @description 针对表【user_coupon(用户领取优惠券的记录，是真正使用的优惠券信息)】的数据库操作Mapper
* @createDate 2024-08-21 19:02:37
* @Entity com.tianji.promotion.domain.po.UserCoupon
*/
public interface UserCouponMapper extends BaseMapper<UserCoupon> {

    @Select("select c.id,\n" +
            "       c.discount_type,\n" +
            "       c.`specific`,\n" +
            "       c.threshold_amount,\n" +
            "       c.discount_value,\n" +
            "       c.max_discount_amount,\n" +
            "       uc.id as creater\n" +
            "from coupon c\n" +
            "         inner join user_coupon uc on c.id = uc.coupon_id\n" +
            "where uc.user_id = #{userId}\n" +
            "  and uc.`status` = 1")
    List<Coupon> queryMyCoupons(Long userId);
}




