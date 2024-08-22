package com.tianji.promotion.service;

import com.tianji.promotion.domain.po.Coupon;
import com.tianji.promotion.domain.po.ExchangeCode;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * @author smile67
 * @description 针对表【exchange_code(兑换码)】的数据库操作Service
 * @createDate 2024-08-19 18:48:06
 */
public interface IExchangeCodeService extends IService<ExchangeCode> {
    // 异步生成兑换码
    void asyncGenerateExchangeCode(Coupon coupon);

    boolean updateExchangeCodeMark(long serialNum, boolean flag);
}
