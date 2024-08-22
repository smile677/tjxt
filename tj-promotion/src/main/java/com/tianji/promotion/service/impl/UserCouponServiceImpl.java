package com.tianji.promotion.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tianji.common.exceptions.BadRequestException;
import com.tianji.common.exceptions.BizIllegalException;
import com.tianji.common.utils.StringUtils;
import com.tianji.common.utils.UserContext;
import com.tianji.promotion.domain.po.Coupon;
import com.tianji.promotion.domain.po.ExchangeCode;
import com.tianji.promotion.domain.po.UserCoupon;
import com.tianji.promotion.enums.CouponStatus;
import com.tianji.promotion.enums.ExchangeCodeStatus;
import com.tianji.promotion.mapper.CouponMapper;
import com.tianji.promotion.mapper.UserCouponMapper;
import com.tianji.promotion.service.IExchangeCodeService;
import com.tianji.promotion.service.IUserCouponService;
import com.tianji.promotion.utils.CodeUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

/**
 * @author smile67
 * @description 针对表【user_coupon(用户领取优惠券的记录，是真正使用的优惠券信息)】的数据库操作Service实现
 * @createDate 2024-08-21 19:02:37
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class UserCouponServiceImpl extends ServiceImpl<UserCouponMapper, UserCoupon>
        implements IUserCouponService {
    private final CouponMapper couponMapper;
    private final IExchangeCodeService exchangeCodeService;

    @Override
    public void receiveCoupon(Long id) {
        // 1.根据id查询优惠券信息 做相关校验
        if (id == null) {
            throw new BadRequestException("非法参数");
        }
        Coupon coupon = couponMapper.selectById(id);
        if (coupon == null) {
            throw new BizIllegalException("优惠券不存在");
        }
        if (coupon.getStatus() != CouponStatus.ISSUING) {
            throw new BizIllegalException("该优惠券状态不是正在发放中");
        }
        LocalDateTime now = LocalDateTime.now();
        if (now.isBefore(coupon.getIssueBeginTime()) || now.isAfter(coupon.getIssueEndTime())) {
            throw new BizIllegalException("该优惠券不在发放时间范围内");
        }
        if (coupon.getTotalNum() <= 0 || coupon.getIssueNum() >= coupon.getTotalNum()) {
            throw new BizIllegalException("该优惠券库存不足");
        }
        // 获取当前用户 对该优惠券的领取次数
        Long userId = UserContext.getUser();
        /*
        Integer count = this.lambdaQuery()
                .eq(UserCoupon::getUserId, userId)
                .eq(UserCoupon::getCouponId, id)
                .count();
        if (count != null && count >= coupon.getUserLimit()) {
            throw new BizIllegalException("该优惠券领取次数已达到上限");
        }
        // 2.优惠券的已发数量+1
//        coupon.setIssueNum(coupon.getIssueNum() + 1);
//        couponMapper.updateById(coupon);
        // 使用这种方式，考虑后面的并发控制
        couponMapper.incrIssueNum(id);

        // 3.生成用户券
        saveUserCoupon(userId, coupon);*/
        // 提取函数为
        checkAndCreateUserCoupon(userId, coupon, null);
    }

    @Override
    public void exchangeCoupon(String code) {
        // 1.校验非空
        if (StringUtils.isBlank(code)) {
            throw new BadRequestException("非法参数");
        }
        // 2.解析兑换码得到自增id
        Long serialNum = CodeUtil.parseCode(code);
        log.debug("自增id{}", serialNum);
        // 3.判断兑换码是否已经兑换 采用redis的bitmap结构 setbit key offset 1 如果返回true代表兑换码已经兑换
        boolean result = exchangeCodeService.updateExchangeCodeMark(serialNum, true);
        if (result) {
            // 说明兑换码已经被兑换
            throw new BizIllegalException("该兑换码已经被兑换");
        }
        try {
            // 4.判断兑换码是否存在 根据自增id查询 主键查询
            ExchangeCode exchangeCode = exchangeCodeService.getById(serialNum);
            if (exchangeCode == null) {
                throw new BizIllegalException("该兑换码不存在");
            }
            // 5.判断是否过期
            if (exchangeCode.getExpiredTime().isBefore(LocalDateTime.now())) {
                throw new BizIllegalException("该兑换码已过期");
            }
            // 校验并生成用户券
            Long userId = UserContext.getUser();
            Coupon coupon = couponMapper.selectById(exchangeCode.getExchangeTargetId());
            if (coupon == null) {
                throw new BizIllegalException("该兑换码对应的优惠券不存在");
            }
            checkAndCreateUserCoupon(userId, coupon, serialNum);
        } catch (Exception e) {
            // 将兑换码的状态重置
            exchangeCodeService.updateExchangeCodeMark(serialNum, false);
            throw e;
        }
    }

    private void checkAndCreateUserCoupon(Long userId, Coupon coupon, Long serialNum
    ) {
        // 1.获取当前用户 对该优惠券 已领数量 user_coupon 判断是否超出限领数量
        Integer count = this.lambdaQuery()
                .eq(UserCoupon::getUserId, userId)
                .eq(UserCoupon::getCouponId, coupon.getId())
                .count();
        if (count != null && count >= coupon.getUserLimit()) {
            throw new BizIllegalException("该优惠券领取次数已达到上限");
        }
        // 2.优惠券的已发数量+1
//        coupon.setIssueNum(coupon.getIssueNum() + 1);
//        couponMapper.updateById(coupon);
        // 使用这种方式，考虑后面的并发控制
        couponMapper.incrIssueNum(coupon.getId(), coupon.getIssueNum());
        // 3.生成用户券
        saveUserCoupon(userId, coupon);
        // 4.更新兑换码状态
        if (serialNum != null) {
            exchangeCodeService.lambdaUpdate()
                    .set(ExchangeCode::getStatus, ExchangeCodeStatus.USED)
                    .set(ExchangeCode::getUserId, userId)
                    .eq(ExchangeCode::getId, serialNum)
                    .update();
        }
    }

    // 保存用户券
    private void saveUserCoupon(Long userId, Coupon coupon) {
        UserCoupon userCoupon = new UserCoupon();
        userCoupon.setUserId(userId);
        userCoupon.setCouponId(coupon.getId());
        LocalDateTime termBeginTime = coupon.getTermBeginTime();
        LocalDateTime termEndTime = coupon.getTermEndTime();
        if (termBeginTime == null && termEndTime == null) {
            termBeginTime = LocalDateTime.now();
            termEndTime = termBeginTime.plusDays(coupon.getTermDays());
        }
        userCoupon.setTermBeginTime(termBeginTime);
        userCoupon.setTermEndTime(termEndTime);
        this.save(userCoupon);

    }
}




