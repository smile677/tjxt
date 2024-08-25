package com.tianji.promotion.domain.po;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import java.io.Serializable;
import java.time.LocalDateTime;

import com.tianji.promotion.enums.ExchangeCodeStatus;
import lombok.Data;

/**
 * 兑换码
 *
 * @TableName exchange_code
 */
@TableName(value = "exchange_code")
@Data
public class ExchangeCode implements Serializable {
    /**
     * 兑换码id
     */
    @TableId(value = "id", type = IdType.INPUT)
    private Integer id;

    /**
     * 兑换码
     */
    @TableField(value = "code")
    private String code;

    /**
     * 兑换码状态， 1：待兑换，2：已兑换，3：兑换活动已结束
     */
    @TableField(value = "status")
    private ExchangeCodeStatus status;

    /**
     * 兑换人
     */
    @TableField(value = "user_id")
    private Long userId;

    /**
     * 兑换类型，1：优惠券，以后再添加其它类型
     */
    @TableField(value = "type")
    private Integer type;

    /**
     * 兑换码目标id，例如兑换优惠券，该id则是优惠券的配置id
     */
    @TableField(value = "exchange_target_id")
    private Long exchangeTargetId;

    /**
     * 创建时间
     */
    @TableField(value = "create_time")
    private LocalDateTime createTime;

    /**
     * 兑换码过期时间
     */
    @TableField(value = "expired_time")
    private LocalDateTime expiredTime;

    /**
     * 更新时间
     */
    @TableField(value = "update_time")
    private LocalDateTime updateTime;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}