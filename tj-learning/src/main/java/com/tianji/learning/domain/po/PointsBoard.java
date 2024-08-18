package com.tianji.learning.domain.po;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import lombok.Data;

/**
 * 学霸天梯榜
 * @TableName points_board
 */
@TableName(value ="points_board")
@Data
public class PointsBoard implements Serializable {
    /**
     * 榜单id
     */
    @TableId(value = "id",type = IdType.ASSIGN_ID)
    private Long id;

    /**
     * 学生id
     */
    @TableField(value = "user_id")
    private Long userId;

    /**
     * 积分值
     */
    @TableField(value = "points")
    private Integer points;

    /**
     * 名次，只记录赛季前100
     */
    @TableField(value = "rank")
    private Integer rank;

    /**
     * 赛季，例如 1,就是第一赛季，2-就是第二赛季
     */
    @TableField(value = "season")
    private Integer season;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}