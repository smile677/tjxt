package com.tianji.promotion.strategy.discount;

import com.tianji.promotion.domain.po.Coupon;
import com.tianji.promotion.enums.DiscountType;

import java.util.EnumMap;

public class DiscountStrategy {

    private final static EnumMap<DiscountType, Discount> strategies;

    static {
        strategies = new EnumMap<>(DiscountType.class);
        strategies.put(DiscountType.NO_THRESHOLD, new NoThresholdDiscount());
        strategies.put(DiscountType.PER_PRICE_DISCOUNT, new PerPriceDiscount());
        strategies.put(DiscountType.RATE_DISCOUNT, new RateDiscount());
        strategies.put(DiscountType.PRICE_DISCOUNT, new PriceDiscount());
    }

    public static Discount getDiscount(DiscountType type) {
        return strategies.get(type);
    }

    public static void main(String[] args) {
        Coupon coupon = new Coupon();
        coupon.setDiscountType(DiscountType.PER_PRICE_DISCOUNT);
        coupon.setThresholdAmount(10000);
        coupon.setDiscountValue(1500);
        coupon.setMaxDiscountAmount(3000);
        boolean flag1 = DiscountStrategy.getDiscount(coupon.getDiscountType()).canUse(150, coupon);
        System.out.println("flag1 = " + flag1);
        String rule1 = DiscountStrategy.getDiscount(coupon.getDiscountType()).getRule(coupon);
        System.out.println("rule1 = " + rule1);
        boolean flag2 = DiscountStrategy.getDiscount(coupon.getDiscountType()).canUse(99, coupon);
        System.out.println("flag2 = " + flag2);
    }
}
