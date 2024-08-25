package com.tianji.trade.domain.vo;

import com.tianji.api.dto.promotion.CouponDiscountDTO;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

@Data
@ApiModel(description = "订单确认页信息")
public class OrderConfirmVO {
    @ApiModelProperty("订单id")
    private Long orderId;
    @ApiModelProperty("订单总方案")
    private Integer totalAmount;
    // 之前预留了一个优惠折扣金额，需要修改为折扣方案
    @ApiModelProperty("优惠方案")
    private List<CouponDiscountDTO> discounts;
    @ApiModelProperty("订单中包含的课程")
    private List<OrderCourseVO> courses;
}
