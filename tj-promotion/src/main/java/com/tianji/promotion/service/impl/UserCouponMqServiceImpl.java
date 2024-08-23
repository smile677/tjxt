package com.tianji.promotion.service.impl;

import cn.hutool.core.bean.copier.CopyOptions;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tianji.common.autoconfigure.mq.RabbitMqHelper;
import com.tianji.common.constants.MqConstants;
import com.tianji.common.exceptions.BadRequestException;
import com.tianji.common.exceptions.BizIllegalException;
import com.tianji.common.utils.BeanUtils;
import com.tianji.common.utils.StringUtils;
import com.tianji.common.utils.UserContext;
import com.tianji.promotion.constants.PromotionConstants;
import com.tianji.promotion.domain.dto.UserCouponDTO;
import com.tianji.promotion.domain.po.Coupon;
import com.tianji.promotion.domain.po.ExchangeCode;
import com.tianji.promotion.domain.po.UserCoupon;
import com.tianji.promotion.enums.ExchangeCodeStatus;
import com.tianji.promotion.mapper.CouponMapper;
import com.tianji.promotion.mapper.UserCouponMapper;
import com.tianji.promotion.service.IExchangeCodeService;
import com.tianji.promotion.service.IUserCouponService;
import com.tianji.promotion.utils.CodeUtil;
import com.tianji.promotion.utils.MyLock;
import com.tianji.promotion.utils.MyLockStrategy;
import com.tianji.promotion.utils.MyLockType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RedissonClient;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * @author smile67
 * @description 针对表【user_coupon(用户领取优惠券的记录，是真正使用的优惠券信息)】的数据库操作Service实现
 * @createDate 2024-08-21 19:02:37
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class UserCouponMqServiceImpl extends ServiceImpl<UserCouponMapper, UserCoupon> implements IUserCouponService {
    private final CouponMapper couponMapper;
    private final IExchangeCodeService exchangeCodeService;
    private final StringRedisTemplate redisTemplate;
    private final RedissonClient redissonClient;
    private final RabbitMqHelper mqHelper;

    // 领取优惠券
    @Override
    @MyLock(name = "lock:coupon:uid:#{id}")
    public void receiveCoupon(Long id) {
        // 1.根据id查询优惠券信息 做相关校验
        if (id == null) {
            throw new BadRequestException("非法参数");
        }
//        Coupon coupon = couponMapper.selectById(id);
        // 从redis中获取优惠券
        Coupon coupon = queryCouponByCache(id);
        if (coupon == null) {
            throw new BizIllegalException("优惠券不存在");
        }
//        if (coupon.getStatus() != CouponStatus.ISSUING) {
//            throw new BizIllegalException("该优惠券状态不是正在发放中");
//        }
        LocalDateTime now = LocalDateTime.now();
        if (now.isBefore(coupon.getIssueBeginTime()) || now.isAfter(coupon.getIssueEndTime())) {
            throw new BizIllegalException("该优惠券不在发放时间范围内");
        }
//        if (coupon.getTotalNum() <= 0 || coupon.getIssueNum() >= coupon.getTotalNum()) {
        if (coupon.getTotalNum() <= 0) {
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
//        synchronized (userId.toString().intern()) {
//            // 从aop上下文中 获取当前类的代理对象
//            IUserCouponService userCouponServiceProxy = (IUserCouponService) AopContext.currentProxy();
//            // 这种写法是调用原对象
////            checkAndCreateUserCoupon(userId, coupon, null);
//            // 调用代理对象的的方法，这种方法有事务处理
//            userCouponServiceProxy.checkAndCreateUserCoupon(userId, coupon, null);
//        }

        // 通过工具类的方式手动实现分布式锁
//        String key = "lock:coupon:uid:" + userId;
//        RedisLock redisLock = new RedisLock(key, redisTemplate);
//        boolean isLock = redisLock.tryLock(5, TimeUnit.SECONDS);
//        if (!isLock) {
//            throw new BizIllegalException("请求太频繁");
//        }
//        try {
//            IUserCouponService userCouponServiceProxy = (IUserCouponService) AopContext.currentProxy();
//            userCouponServiceProxy.checkAndCreateUserCoupon(userId, coupon, null);
//        } finally {
//
//            redisLock.unlock();
//        }

        // 通过 Redisson 实现分布式锁
//        String key = "lock:coupon:uid:" + userId;
//        RLock lock = redissonClient.getLock(key);
//        try {
//            // 看门狗机制会生效 默认失效时间是30s
//            boolean isLock = lock.tryLock();
//            if (!isLock) {
//                throw new BizIllegalException("请求太频繁~");
//            }
//            IUserCouponService userCouponServiceProxy = (IUserCouponService) AopContext.currentProxy();
//            userCouponServiceProxy.checkAndCreateUserCoupon(userId, coupon, null);
//        } finally {
//            lock.unlock();
//        }

        // 统计已领取的数量
        // long num = redisTemplate.opsForHash.get(key, userid)
        // 校验是否超过了限领数量
        // if(num >= coupon.getUserLimit())throw
        // 修改已经领取量+1
        // long num = redisTemplate.opsForHash.put(key, userid, 1);

        String key = PromotionConstants.USER_COUPON_CACHE_KEY_PREFIX + id;
        // increment 代表本次领取后的 已领数量
        long increment = redisTemplate.opsForHash().increment(key, userId.toString(), 1);
        if (increment > coupon.getUserLimit()) {
            throw new BizIllegalException("超过限领数量");
        }
        // 修改优惠券库存 -1
        String couponKey = PromotionConstants.COUPON_CACHE_KEY_PREFIX + id;
        redisTemplate.opsForHash().increment(couponKey, "totalNum", -1);

        // 发送消息到mq 消息内容为 userId couponId
        UserCouponDTO msg = new UserCouponDTO();
        msg.setUserId(userId);
        msg.setCouponId(id);
        mqHelper.send(
                MqConstants.Exchange.PROMOTION_EXCHANGE,
                MqConstants.Key.COUPON_RECEIVE,
                msg
        );

        //  自定义分布式锁组件
//        String key = "lock:coupon:uid:" + userId;
//        IUserCouponService userCouponServiceProxy = (IUserCouponService) AopContext.currentProxy();
//        userCouponServiceProxy.checkAndCreateUserCoupon(userId, coupon, null);

    }

    /**
     * 从缓存中获取优惠券(开始时间、结束时间、发行总数量、限领数量)
     *
     * @param id
     * @return
     */
    private Coupon queryCouponByCache(Long id) {
        String key = PromotionConstants.COUPON_CACHE_KEY_PREFIX + id;
        Map<Object, Object> entries = redisTemplate.opsForHash().entries(key);
        Coupon coupon = BeanUtils.mapToBean(entries, Coupon.class, false, CopyOptions.create());
        return coupon;
    }

    @MyLock(name = "lock:coupon:uid:#{userId}",
            lockType = MyLockType.RE_ENTRANT_LOCK,
            lockStrategy = MyLockStrategy.FAIL_AFTER_RETRY_TIMEOUT)
    @Transactional
    @Override
    public void checkAndCreateUserCoupon(Long userId, Coupon coupon, Long serialNum) {
        // Long类型 -128~127 之间是同一个对象 享元模式 超过该区间是不同的对象
        // Long.toString 方法底层是new String 所以还是不同的对象
        // Long.toString.intern() inter方法是强制从常量池中取出字符串
//        synchronized (userId.toString().intern()) {
        // 1.获取当前用户 对该优惠券 已领数量 user_coupon 判断是否超出限领数量
        Integer count = this.lambdaQuery().eq(UserCoupon::getUserId, userId).eq(UserCoupon::getCouponId, coupon.getId()).count();
        if (count != null && count >= coupon.getUserLimit()) {
            throw new BizIllegalException("该优惠券领取次数已达到上限");
        }
        // 2.优惠券的已发数量+1
//        coupon.setIssueNum(coupon.getIssueNum() + 1);
//        couponMapper.updateById(coupon);
        // 使用这种方式，考虑后面的并发控制
        int r = couponMapper.incrIssueNum(coupon.getId());
        if (r == 0) {
            throw new BizIllegalException("优惠券已领完,库存不足！");
        }

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
//        }
//        throw new RuntimeException("故意报错");
    }

    //    @MyLock(name = "lock:coupon:uid:#{id}",
//            lockType = MyLockType.RE_ENTRANT_LOCK,
//            lockStrategy = MyLockStrategy.FAIL_AFTER_RETRY_TIMEOUT)
    @Transactional
    @Override
    public void checkAndCreateUserCouponNew(UserCouponDTO msg) {
        // 1.查询优惠券
        Coupon coupon = couponMapper.selectById(msg.getCouponId());
        if (coupon == null) {
//            throw new BizIllegalException("优惠券不存在！");
            return;
        }
        // 2.更新优惠券的已经发放的数量 + 1
        int r = couponMapper.incrIssueNum(coupon.getId());
        if (r == 0) {
//            throw new BizIllegalException("优惠券库存不足！");
            return;
        }
        // 3.新增一个用户券
        saveUserCoupon(msg.getUserId(), coupon);
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

    public static void main(String[] args) {
        Long l1 = 1L;
        Long l2 = 1L;
        System.out.println("l1 == l2 = " + (l1 == l2));
        Long l3 = -129L;
        Long l4 = -129L;
        // -128 ~ 127
        System.out.println("l3 == l4 = " + (l3 == l4));
        Long l5 = 129L;
        Long l6 = 129L;
        // -128 ~ 127
        System.out.println("l3 == l4 = " + (l5 == l6));

        String s1 = new String("abc");
        String s2 = new String("abc");
        System.out.println("(s1 == s2) = " + (s1 == s2));

        String s3 = new String("cdf").intern();
        String s4 = new String("cdf").intern();
        System.out.println("(s3 == s4) = " + (s3 == s4));

        String s5 = new String("gh");
        String s6 = new String("gh").intern();
        System.out.println(" (s5 == s6) = " + (s5 == s6));
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




