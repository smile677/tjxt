package com.tianji.learning.domain.po;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import java.io.Serializable;
import java.time.LocalDate;

import lombok.Data;

/**
 * @TableName points_board_season
 */
@TableName(value = "points_board_season")
@Data
public class PointsBoardSeason implements Serializable {
    /**
     * 自增长id，season标示
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    /**
     * 赛季名称，例如：第1赛季
     */
    @TableField(value = "name")
    private String name;

    /**
     * 赛季开始时间
     */
    @TableField(value = "begin_time")
    private LocalDate beginTime;

    /**
     * 赛季结束时间
     */
    @TableField(value = "end_time")
    private LocalDate endTime;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}