package com.tianji.promotion.mapper;

import com.tianji.promotion.domain.po.Coupon;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

/**
 * @author smile67
 * @description 针对表【coupon(优惠券的规则信息)】的数据库操作Mapper
 * @createDate 2024-08-19 18:48:06
 * @Entity com.tianji.promotion.domain.po.Coupon
 */
public interface CouponMapper extends BaseMapper<Coupon> {
    // 更新优惠券已领取梳理
    @Update("update coupon set issue_num = issue_num + 1 where id = #{id} and issue_num < total_num")
    void incrIssueNum(@Param("id") Long id);
}




