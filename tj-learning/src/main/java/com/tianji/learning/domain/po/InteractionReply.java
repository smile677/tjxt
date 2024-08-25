package com.tianji.learning.domain.po;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import java.util.Date;
import lombok.Data;

/**
 * 互动问题的回答或评论
 * @TableName interaction_reply
 */
@TableName(value ="interaction_reply")
@Data
public class InteractionReply implements Serializable {
    /**
     * 互动问题的回答id
     */
    @TableId(value = "id", type = IdType.ASSIGN_ID)
    private Long id;

    /**
     * 互动问题问题id
     */
    @TableField(value = "question_id")
    private Long questionId;

    /**
     * 回复的上级回答id
     */
    @TableField(value = "answer_id")
    private Long answerId;

    /**
     * 回答者id
     */
    @TableField(value = "user_id")
    private Long userId;

    /**
     * 回答内容
     */
    @TableField(value = "content")
    private String content;

    /**
     * 回复的目标用户id
     */
    @TableField(value = "target_user_id")
    private Long targetUserId;

    /**
     * 回复的目标回复id
     */
    @TableField(value = "target_reply_id")
    private Long targetReplyId;

    /**
     * 评论数量
     */
    @TableField(value = "reply_times")
    private Integer replyTimes;

    /**
     * 点赞数量
     */
    @TableField(value = "liked_times")
    private Integer likedTimes;

    /**
     * 是否被隐藏，默认false
     */
    @TableField(value = "hidden")
    private Boolean hidden;

    /**
     * 是否匿名，默认false
     */
    @TableField(value = "anonymity")
    private Boolean anonymity;

    /**
     * 创建时间
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