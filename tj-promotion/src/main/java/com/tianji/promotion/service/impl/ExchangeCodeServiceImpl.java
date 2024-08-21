package com.tianji.promotion.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tianji.promotion.constants.PromotionConstants;
import com.tianji.promotion.domain.po.Coupon;
import com.tianji.promotion.domain.po.ExchangeCode;
import com.tianji.promotion.service.IExchangeCodeService;
import com.tianji.promotion.mapper.ExchangeCodeMapper;
import com.tianji.promotion.utils.CodeUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

import static com.tianji.promotion.constants.PromotionConstants.COUPON_RANGE_KEY;

/**
 * @author smile67
 * @description 针对表【exchange_code(兑换码)】的数据库操作Service实现
 * @createDate 2024-08-19 18:48:06
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class ExchangeCodeServiceImpl extends ServiceImpl<ExchangeCodeMapper, ExchangeCode>
        implements IExchangeCodeService {
    private final StringRedisTemplate redisTemplate;

    @Override
    @Async("generateExchangeCodeExecutor")// 使用generateExchangeCodeExecutor自定义的线程池中的线程异步执行
    public void asyncGenerateExchangeCode(Coupon coupon) {
        log.debug("生成兑换码 线程名：" + Thread.currentThread().getName());
        Integer totalNum = coupon.getTotalNum();
        // 方式1：循环兑换 循环中单个获取自增id   incr（效率不高）
        // 方式2：先调用incrby 得到自增id最大值，然后在循环生成兑换码（只需要操作一次redis即可）
        // 1.生成自增id 借助于redis incrby
        Long increment = redisTemplate.opsForValue().increment(PromotionConstants.COUPON_CODE_SERIAL_KEY, totalNum);
        if (increment == null) {
            return;
        }
        int maxSerialNum = increment.intValue();
        int begin = maxSerialNum - totalNum + 1;
        // 2.循环生成兑换码 调用工具类生成
        List<ExchangeCode> exchangeCodeList = new ArrayList<>(totalNum);
        for (int serialNum = begin; serialNum <= maxSerialNum; serialNum++) {
            String code = CodeUtil.generateCode(serialNum, coupon.getId());
            ExchangeCode exchangeCode = new ExchangeCode();
            exchangeCode.setId(serialNum);
            exchangeCode.setCode(code);
            exchangeCode.setExchangeTargetId(coupon.getId());
            exchangeCode.setExpiredTime(coupon.getIssueEndTime());
            exchangeCodeList.add(exchangeCode);
        }
        // 3.批量插入
        this.saveBatch(exchangeCodeList);
        // 4.写入缓存 member:couponId score 兑换码的最大序号
        redisTemplate.opsForZSet().add(COUPON_RANGE_KEY, coupon.getId().toString(), maxSerialNum);
    }

    public static void main(String[] args) {
        String code = CodeUtil.generateCode(3, 1);
        log.debug("code = " + code);
        long l = CodeUtil.parseCode(code);
        log.debug("l = " + l);
    }
}




