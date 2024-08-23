package com.tianji.promotion.constants;

public interface PromotionConstants {
    // 自增自id 对应的键
    String COUPON_CODE_SERIAL_KEY = "coupon:code:serial";
    // 检验兑换码是否兑换 借助于redis bitmap
    String COUPON_CODE_MAP_KEY = "coupon:code:map";

    // 领取优惠券的key
    String COUPON_CACHE_KEY_PREFIX = "prs:coupon:";
    String USER_COUPON_CACHE_KEY_PREFIX = "prs:user:coupon:";

    String COUPON_RANGE_KEY = "coupon:code:range";
    String[] RECEIVE_COUPON_ERROR_MSG = {
            "活动未开始",
            "库存不足",
            "活动已经介绍",
            "领取次数过多",
    };
    String[] EXCHANGE_COUPON_ERROR_MSG = {
            "兑换码已兑换",
            "无效兑换码",
            "活动未开始",
            "活动已经介绍",
            "领取次数过多",
    };
}
