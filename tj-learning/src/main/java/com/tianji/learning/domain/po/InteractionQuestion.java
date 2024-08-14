package com.tianji.learning.domain.po;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import java.util.Date;

import com.tianji.learning.enums.QuestionStatus;
import lombok.Data;

/**
 * 互动提问的问题表
 * @TableName interaction_question
 */
@TableName(value ="interaction_question")
@Data
public class InteractionQuestion implements Serializable {
    /**
     * 主键，互动问题的id
     */
    @TableId(value = "id", type = IdType.ASSIGN_ID)
    private Long id;
    /**
     * 互动问题的标题
     */
    @TableField(value = "title")
    private String title;
    /**
     * 问题描述信息
     */
    @TableField(value = "description")
    private String description;
    /**
     * 所属课程id
     */
    @TableField(value = "course_id")
    private Long courseId;
    /**
     * 所属课程章id
     */
    @TableField(value = "chapter_id")
    private Long chapterId;
    /**
     * 所属课程节id
     */
    @TableField(value = "section_id")
    private Long sectionId;
    /**
     * 提问学员id
     */
    @TableField(value = "user_id")
    private Long userId;
    /**
     * 最新的一个回答的id
     */
    @TableField(value = "latest_answer_id")
    private Long latestAnswerId;
    /**
     * 问题下的回答数量
     */
    @TableField(value = "answer_times")
    private Integer answerTimes;
    /**
     * 是否匿名，默认false
     */
    @TableField(value = "anonymity")
    private Boolean anonymity;
    /**
     * 是否被隐藏，默认false
     */
    @TableField(value = "hidden")
    private Boolean hidden;
    /**
     * 管理端问题状态：0-未查看，1-已查看
     */
    @TableField(value = "status")
    private QuestionStatus status;
    /**
     * 提问时间
     */
    @TableField(value = "create_time")
    private Date createTime;
    /**
     * 更新时间
     */
    @TableField(value = "update_time")
    private Date updateTime;
    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}