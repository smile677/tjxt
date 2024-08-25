package com.tianji.promotion.domain.po;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import java.time.LocalDateTime;

import com.tianji.promotion.enums.CouponStatus;
import com.tianji.promotion.enums.DiscountType;
import com.tianji.promotion.enums.ObtainType;
import lombok.Data;

/**
 * 优惠券的规则信息
 * @author smile67
 * @TableName coupon
 */
@TableName(value ="coupon")
@Data
public class Coupon implements Serializable {
    /**
     * 优惠券id
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 优惠券名称，可以和活动名称保持一致
     */
    @TableField(value = "`name`")
    private String name;

    /**
     * 优惠券类型，1：普通券。目前就一种，保留字段
     */
    @TableField(value = "type")
    private Integer type;

    /**
     * 折扣类型，1：每满减，2：折扣，3：无门槛，4：普通满减
     */
    @TableField(value = "discount_type")
    private DiscountType discountType;

    /**
     * 是否限定作用范围，false：不限定，true：限定。默认false
     */
    @TableField(value = "`specific`")
    private Boolean specific;

    /**
     * 折扣值，如果是满减则存满减金额，如果是折扣，则存折扣率，8折就是存80
     */
    @TableField(value = "discount_value")
    private Integer discountValue;

    /**
     * 使用门槛，0：表示无门槛，其他值：最低消费金额
     */
    @TableField(value = "threshold_amount")
    private Integer thresholdAmount;

    /**
     * 最高优惠金额，满减最大，0：表示没有限制，不为0，则表示该券有金额的限制
     */
    @TableField(value = "max_discount_amount")
    private Integer maxDiscountAmount;

    /**
     * 获取方式：1：手动领取，2：兑换码
     */
    @TableField(value = "obtain_way")
    private ObtainType obtainWay;

    /**
     * 开始发放时间
     */
    @TableField(value = "issue_begin_time")
    private LocalDateTime issueBeginTime;

    /**
     * 结束发放时间
     */
    @TableField(value = "issue_end_time")
    private LocalDateTime issueEndTime;

    /**
     * 优惠券有效期天数，0：表示有效期是指定有效期的
     */
    @TableField(value = "term_days")
    private Integer termDays;

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
     * 优惠券配置状态，1：待发放，2：未开始   3：进行中，4：已结束，5：暂停
     */
    @TableField(value = "status")
    private CouponStatus status;

    /**
     * 总数量，不超过5000
     */
    @TableField(value = "total_num")
    private Integer totalNum;

    /**
     * 已发行数量，用于判断是否超发
     */
    @TableField(value = "issue_num")
    private Integer issueNum;

    /**
     * 已使用数量
     */
    @TableField(value = "used_num")
    private Integer usedNum;

    /**
     * 每个人限领的数量，默认1
     */
    @TableField(value = "user_limit")
    private Integer userLimit;

    /**
     * 拓展参数字段，保留字段
     */
    @TableField(value = "ext_param")
    private Object extParam;

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