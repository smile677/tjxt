package com.tianji.promotion.domain.po;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import java.time.LocalDateTime;
import lombok.Data;

/**
 * 促销活动，形式多种多样，例如：优惠券
 * @TableName promotion
 */
@TableName(value ="promotion")
@Data
public class Promotion implements Serializable {
    /**
     * 促销活动id
     */
    @TableId(value = "id")
    private Long id;

    /**
     * 活动名称
     */
    @TableField(value = "name")
    private String name;

    /**
     * 促销活动类型：1-优惠券，2-分销
     */
    @TableField(value = "type")
    private Integer type;

    /**
     * 是否是热门活动：true或false，默认false
     */
    @TableField(value = "hot")
    private Integer hot;

    /**
     * 活动开始时间
     */
    @TableField(value = "begin_time")
    private LocalDateTime beginTime;

    /**
     * 活动结束时间
     */
    @TableField(value = "end_time")
    private LocalDateTime endTime;

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

    /**
     * 创建人
     */
    @TableField(value = "creater")
    private Long creater;

    /**
     * 更新人
     */
    @TableField(value = "updater")
    private Long updater;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}