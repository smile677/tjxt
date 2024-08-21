package com.tianji.promotion.domain.po;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import java.time.LocalDateTime;

import com.tianji.promotion.enums.UserCouponStatus;
import lombok.Data;

/**
 * 用户领取优惠券的记录，是真正使用的优惠券信息
 * @TableName user_coupon
 */
@TableName(value ="user_coupon")
@Data
public class UserCoupon implements Serializable {
    /**
     * 用户券id
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 优惠券的拥有者
     */
    @TableField(value = "user_id")
    private Long userId;

    /**
     * 优惠券模板id
     */
    @TableField(value = "coupon_id")
    private Long couponId;

    /**
     * 优惠券有效期开始时间
     */
    @TableField(value = "term_begin_time")
    private LocalDateTime termBeginTime;

    /**
     * 优惠券有效期结束时间
     */
    @TableField(value = "term_end_time")
    private LocalDateTime termEndTime;

    /**
     * 优惠券使用时间（核销时间）
     */
    @TableField(value = "used_time")
    private LocalDateTime usedTime;

    /**
     * 优惠券状态，1：未使用，2：已使用，3：已失效
     */
    @TableField(value = "status")
    private UserCouponStatus status;

    /**
     * 创建时间
     */
    @TableField(value = "create_time")
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    @TableField(value = "update_time")
    private LocalDateTime updateTime;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}