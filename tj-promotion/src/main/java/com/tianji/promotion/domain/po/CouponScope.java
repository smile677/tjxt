package com.tianji.promotion.domain.po;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 优惠券作用范围信息
 * @TableName coupon_scope
 */
@TableName(value ="coupon_scope")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class CouponScope implements Serializable {
    /**
     * 
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 范围限定类型：1-分类，2-课程，等等
     */
    @TableField(value = "type")
    private Integer type;

    /**
     * 优惠券id
     */
    @TableField(value = "coupon_id")
    private Long couponId;

    /**
     * 优惠券作用范围的业务id，例如分类id、课程id
     */
    @TableField(value = "biz_id")
    private Long bizId;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}