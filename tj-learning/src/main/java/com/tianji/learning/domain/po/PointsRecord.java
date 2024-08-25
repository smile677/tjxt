package com.tianji.learning.domain.po;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import java.util.Date;

import com.tianji.learning.enums.PointsRecordType;
import lombok.Data;

/**
 * 学习积分记录，每个月底清零
 * @TableName points_record
 */
@TableName(value ="points_record")
@Data
public class PointsRecord implements Serializable {
    /**
     * 积分记录表id
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 用户id
     */
    @TableField(value = "user_id")
    private Long userId;

    /**
     * 积分方式：1-课程学习，2-每日签到，3-课程问答， 4-课程笔记，5-课程评价
     */
    @TableField(value = "type")
    private PointsRecordType type;

    /**
     * 积分值
     */
    @TableField(value = "points")
    private Integer points;

    /**
     * 创建时间
     */
    @TableField(value = "create_time")
    private Date createTime;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}