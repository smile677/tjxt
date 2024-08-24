package com.tianji.promotion.service.impl;

import cn.hutool.core.bean.copier.CopyOptions;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tianji.common.autoconfigure.mq.RabbitMqHelper;
import com.tianji.common.constants.MqConstants;
import com.tianji.common.exceptions.BadRequestException;
import com.tianji.common.exceptions.BizIllegalException;
import com.tianji.common.utils.BeanUtils;
import com.tianji.common.utils.CollUtils;
import com.tianji.common.utils.StringUtils;
import com.tianji.common.utils.UserContext;
import com.tianji.promotion.constants.PromotionConstants;
import com.tianji.promotion.domain.dto.CouponDiscountDTO;
import com.tianji.promotion.domain.dto.OrderCourseDTO;
import com.tianji.promotion.domain.dto.UserCouponDTO;
import com.tianji.promotion.domain.po.Coupon;
import com.tianji.promotion.domain.po.CouponScope;
import com.tianji.promotion.domain.po.ExchangeCode;
import com.tianji.promotion.domain.po.UserCoupon;
import com.tianji.promotion.enums.ExchangeCodeStatus;
import com.tianji.promotion.mapper.CouponMapper;
import com.tianji.promotion.mapper.UserCouponMapper;
import com.tianji.promotion.service.ICouponScopeService;
import com.tianji.promotion.service.IExchangeCodeService;
import com.tianji.promotion.service.IUserCouponService;
import com.tianji.promotion.strategy.discount.Discount;
import com.tianji.promotion.strategy.discount.DiscountStrategy;
import com.tianji.promotion.utils.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RedissonClient;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

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
    private final ICouponScopeService couponScopeService;

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
    public List<CouponDiscountDTO> findDiscountSolution(List<OrderCourseDTO> courseDTOS) {
        // 1.查询当前用户可用的优惠券 coupon和user_coupon表 条件：userId、status=1 字段：优惠券规则、优惠券id、用户券id(提交订单的时候需要用)
        List<Coupon> coupons = getBaseMapper().queryMyCoupons(UserContext.getUser());
        log.debug("当前用户的优惠券共有{}张", coupons.size());
        for (Coupon coupon : coupons) {
            log.debug("优惠券：{}, {}",
                    DiscountStrategy.getDiscount(coupon.getDiscountType()).getRule(coupon),
                    coupon);
        }

        // 2.初筛
        // 2.1 计算订单的总金额 对courseDTO的price累加
//        int totalAmout = 0;
//        for (OrderCourseDTO courseDTO : courseDTOS) {
//            totalAmout += courseDTO.getPrice();
//        }
        int totalAmount = courseDTOS.stream().mapToInt(OrderCourseDTO::getPrice).sum();
        log.debug("订单总金额：{}", totalAmount);

        // 2.2 校验优惠券是否可用
//        List<Coupon> availableCouponList = new ArrayList<>();
//        for (Coupon coupon : coupons) {
//            boolean flag = DiscountStrategy.getDiscount(coupon.getDiscountType()).canUse(totalAmout, coupon);
//            if (flag) {
//                availableCouponList.add(coupon);
//            }
//        }
        List<Coupon> availableCouponList = coupons.stream()
                .filter(coupon -> DiscountStrategy.getDiscount(coupon.getDiscountType()).canUse(totalAmount, coupon))
                .collect(Collectors.toList());
        if (CollUtils.isEmpty(availableCouponList)) {
            return CollUtils.emptyList();
        }
        log.debug("经过初筛之后，还剩{}张", availableCouponList.size());
        for (Coupon coupon : availableCouponList) {
            log.debug("优惠券：{}, {}",
                    DiscountStrategy.getDiscount(coupon.getDiscountType()).getRule(coupon),
                    coupon);
        }


        // 3.细筛（考虑优惠券的限定范围） 排列组合
        Map<Coupon, List<OrderCourseDTO>> avaMap = findAvailableCoupons(availableCouponList, courseDTOS);
        if (avaMap.isEmpty()) {
            return CollUtils.emptyList();
        }
        Set<Map.Entry<Coupon, List<OrderCourseDTO>>> entries = avaMap.entrySet();
        for (Map.Entry<Coupon, List<OrderCourseDTO>> entry : entries) {
            log.debug("经过细筛之后优惠券：{}, {}",
                    DiscountStrategy.getDiscount(entry.getKey().getDiscountType()).getRule(entry.getKey()),
                    entry.getKey());
        }
        availableCouponList = new ArrayList<>(avaMap.keySet());
        log.debug("经过细筛之后,真正的可用的优惠券个数：{}", availableCouponList.size());
        for (Coupon coupon : availableCouponList) {
            log.debug("优惠券：{}, {}",
                    DiscountStrategy.getDiscount(coupon.getDiscountType()).getRule(coupon),
                    coupon);
        }
        // 排列组合
        List<List<Coupon>> solutions = PermuteUtil.permute(availableCouponList);
        for (Coupon availableCoupon : availableCouponList) {
            // 添加单卷到方案中
            solutions.add(List.of(availableCoupon));
        }
        log.debug("排列组合");
        for (List<Coupon> solution : solutions) {
            List<Long> cIds = solution.stream().map(Coupon::getId).collect(Collectors.toList());
            log.debug("{}", cIds);
        }

        // 4.计算每一种组合的优惠明细
        log.debug("开始计算 每一种组合的优惠明细");
        List<CouponDiscountDTO> dtoList = new ArrayList<>();
        for (List<Coupon> solution : solutions) {
            CouponDiscountDTO dto = calculateSolutionDiscount(avaMap, courseDTOS, solution);
            log.debug("优惠方案最终优惠：{} 方案中使用了那些ids有{} 规则{}", dto.getDiscountAmount(), dto.getIds(), dto.getRules());
            dtoList.add(dto);
        }


        // 5.使用多线程改造第4步 并行计算每一种组合的优惠明细


        // 6.筛选最优解
        return dtoList;
    }

    /**
     * 计算每一个优惠的 优惠信息
     *
     * @param avaMap     优惠券和可用课程的映射集合
     * @param courseDTOS 订单中所有的课程
     * @param solutions  方案
     * @return
     */
    private CouponDiscountDTO calculateSolutionDiscount(Map<Coupon, List<OrderCourseDTO>> avaMap,
                                                        List<OrderCourseDTO> courseDTOS,
                                                        List<Coupon> solutions) {
        // 1.创建  方案结果dto对象
        CouponDiscountDTO dto = new CouponDiscountDTO();
        // 2.初始化商品id和商品折扣明细的映射，初筛的折扣明细全部都设置为0
        Map<Long, Integer> detailMap = courseDTOS.stream().collect(Collectors.toMap(OrderCourseDTO::getId, orderCourseDTO -> 0));
        // 3.计算该方案的优惠信息
        // 3.1循环方案中优惠券
        for (Coupon coupon : solutions) {
            // 3.2取出该优惠券对应的可用课程
            List<OrderCourseDTO> availableCourses = avaMap.get(coupon);
            // 3.3计算可用课程的总金额(商品价格-该商品的折扣明细)
            int totalAmount = availableCourses
                    .stream()
                    .mapToInt(value -> value.getPrice() - detailMap.get(value.getId())).sum();
            // 3.4判断优惠券是否可用
            Discount discount = DiscountStrategy.getDiscount(coupon.getDiscountType());
            if (!discount.canUse(totalAmount, coupon)) {
                // 卷不可用，跳出循环，继续处理下一个卷
                continue;
            }
            // 3.5计算该优惠券使用后的折扣值
            int discountAmount = discount.calculateDiscount(totalAmount, coupon);
            // 3.6更新商品的折扣明细(更新商品id的商品折扣明细) 更新到detailMap中
            calculateDetailDiscount(detailMap, availableCourses, totalAmount, discountAmount);
            // 3.7累加每一个优惠券的优惠金额 赋值给方案结果CouponDiscountDTO对象的discountAmount字段
            // 只要执行到当前这句话，就意味着这个优惠券生效了
            dto.getIds().add(coupon.getId());
            dto.getRules().add(discount.getRule(coupon));
            // 不能覆盖，应该是所有生效的优惠券累加的结果
            dto.setDiscountAmount(dto.getDiscountAmount() + discountAmount);
        }


        return null;
    }

    /**
     * 计算商品 折扣明细
     *
     * @param detailMap        商品id和商品的优惠明细 映射
     * @param availableCourses 当前优惠券可用的课程集合
     * @param totalAmount      可用的课程的总金额
     * @param discountAmount   当前优惠券能优惠的金额
     */
    private void calculateDetailDiscount(Map<Long, Integer> detailMap,
                                         List<OrderCourseDTO> availableCourses,
                                         int totalAmount,
                                         int discountAmount) {
        // 目的: 本方法就是优惠券在使用后 计算每个商品的折扣明细
        // 规则: 前面的商品按照比例计算，最后一个商品折扣明细 = 总结的优惠金额-前面商品的优惠的总金额
        // 循环可用的商品
        int times = 0;// 代表已处理的商品个数
        int remainDiscount = discountAmount;// 代表剩余的钱
        for (OrderCourseDTO availableCourse : availableCourses) {
            times++;
            int discount = 0;
            if (times == availableCourses.size()) {
                // 说明是最后一个课程
                discount = remainDiscount;
            } else {
                // 是前面的课程 按照比例计算 先乘再除
                discount = availableCourse.getPrice() * discountAmount / totalAmount;
                remainDiscount = remainDiscount - discount;
            }
            // 将商品的折扣明细添加到detailMap中
            detailMap.put(availableCourse.getId(), discount + detailMap.get(availableCourse.getId()));
        }

    }

    /**
     * 细筛，查询每一个优惠券 对应的可用课程
     *
     * @param availableCouponList 初筛之后的优惠券集合
     * @param orderCourses        订单中的课程集合
     * @return
     */
    private Map<Coupon, List<OrderCourseDTO>> findAvailableCoupons(List<Coupon> availableCouponList, List<OrderCourseDTO> orderCourses) {
        Map<Coupon, List<OrderCourseDTO>> map = new HashMap<>();
        // 1.循环遍历初筛后的优惠券集合
        List<OrderCourseDTO> availableCourses = orderCourses;
        for (Coupon coupon : availableCouponList) {
            // 2.找出每一个优惠券的可以课程
            // 2.1判断优惠券是否限定了范围 coupon.specific=true
            if (coupon.getSpecific()) {
                // 2.2查询限定范围 查coupon_scope表 表的条件：couponId
                List<CouponScope> couponScopeList = couponScopeService.lambdaQuery()
                        .eq(CouponScope::getCouponId, coupon.getId())
                        .list();
                // 2.3得到限定范围id集合
                List<Long> couponScopeIds = couponScopeList.stream().map(CouponScope::getBizId).collect(Collectors.toList());
                // 2.4从orderCourses订单中所有的课程集合 筛选 出该范围的课程
                availableCourses = orderCourses
                        .stream()
                        .filter(orderCourseDTO -> couponScopeIds.contains(orderCourseDTO.getCateId())).collect(Collectors.toList());
            }
            if (CollUtils.isEmpty(availableCouponList)) {
                // 说明当前优惠券限定了范围，但是在订单中的课程没有找到可用课程，说明该卷不可用，忽略该卷，进行下一个优惠券的处理
                continue;
            }
            // 3.计算该优惠券 可用课程的总金额
            int totalAmount = availableCourses.stream().mapToInt(OrderCourseDTO::getPrice).sum();

            // 4.判断该优惠券是否可用 如果可用添加到map中
            Discount discount = DiscountStrategy.getDiscount(coupon.getDiscountType());
            if (discount.canUse(totalAmount, coupon)) {
                map.put(coupon, availableCourses);

            }
        }
        return map;
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




