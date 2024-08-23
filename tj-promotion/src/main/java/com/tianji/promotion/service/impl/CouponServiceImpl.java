package com.tianji.promotion.service.impl;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tianji.common.domain.dto.PageDTO;
import com.tianji.common.exceptions.BadRequestException;
import com.tianji.common.exceptions.BizIllegalException;
import com.tianji.common.utils.*;
import com.tianji.promotion.constants.PromotionConstants;
import com.tianji.promotion.domain.dto.CouponFormDTO;
import com.tianji.promotion.domain.dto.CouponIssueFormDTO;
import com.tianji.promotion.domain.po.Coupon;
import com.tianji.promotion.domain.po.CouponScope;
import com.tianji.promotion.domain.query.CouponQuery;
import com.tianji.promotion.domain.vo.CouponPageVO;
import com.tianji.promotion.domain.vo.CouponVO;
import com.tianji.promotion.enums.CouponStatus;
import com.tianji.promotion.enums.ObtainType;
import com.tianji.promotion.enums.UserCouponStatus;
import com.tianji.promotion.service.ICouponScopeService;
import com.tianji.promotion.service.ICouponService;
import com.tianji.promotion.mapper.CouponMapper;
import com.tianji.promotion.service.IExchangeCodeService;
import com.tianji.promotion.service.IUserCouponService;
import com.tianji.promotion.domain.po.UserCoupon;
import io.swagger.annotations.ApiModelProperty;
import lombok.RequiredArgsConstructor;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.validation.constraints.Future;
import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author smile67
 * @description 针对表【coupon(优惠券的规则信息)】的数据库操作Service实现
 * @createDate 2024-08-19 18:48:06
 */
@Service
@RequiredArgsConstructor
public class CouponServiceImpl extends ServiceImpl<CouponMapper, Coupon>
        implements ICouponService {
    private final ICouponScopeService couponScopeService;
    private final IExchangeCodeService exchangeCodeService;
    private final IUserCouponService userCouponService;
    private final StringRedisTemplate redisTemplate;

    @Override
    @Transactional
    public void saveCoupon(CouponFormDTO dto) {
        // 1.dto转po 保存优惠券 coupon表
        Coupon coupon = BeanUtils.copyBean(dto, Coupon.class);
        this.save(coupon);
        // 2.判断是否限定了范围 dto.specific 如果为false直接return
        if (!dto.getSpecific()) {
            // 说明没有限定限定优惠券的使用范围
            return;
        }
        // 3.如果dto.specific为true 需要校验dto.scope
        List<Long> scopes = dto.getScopes();
        if (CollUtils.isEmpty(scopes)) {
            throw new BadRequestException("分类id不能为空");
        }
        // 4.保存优惠券的限定范围 coupon_scope 批量新增
        List<CouponScope> csList = new ArrayList<>();
        for (Long scope : scopes) {
            CouponScope couponScope = new CouponScope();
            couponScope.setCouponId(coupon.getId());
            couponScope.setBizId(scope);
            couponScope.setType(1);
            csList.add(couponScope);
        }
        couponScopeService.saveBatch(csList);
    }

    @Override
    public PageDTO<CouponPageVO> queryCouponPage(CouponQuery query) {
        // 1.分页查询
        Page<Coupon> page = this.lambdaQuery()
                .eq(query.getType() != null, Coupon::getType, query.getType())
                .eq(query.getStatus() != null, Coupon::getStatus, query.getStatus())
                .like(StringUtils.isNotBlank(query.getName()), Coupon::getName, query.getName())
                .page(query.toMpPageDefaultSortByCreateTimeDesc());
        List<Coupon> records = page.getRecords();
        if (CollUtils.isEmpty(records)) {
            return PageDTO.empty(page.getTotal(), page.getPages());
        }
        // 2.封装vo
        return PageDTO.of(page, BeanUtils.copyList(records, CouponPageVO.class));
    }

    @Override
    public void issueCoupon(Long id, CouponIssueFormDTO dto) {
        log.debug("发放优惠券 线程名：" + Thread.currentThread().getName());
        // 1.校验
        if (id == null || !id.equals(dto.getId())) {
            throw new BadRequestException("非法参数");
        }
        // 2.校验优惠券id是否存在
        Coupon coupon = this.getById(id);
        if (coupon == null) {
            throw new BadRequestException("优惠券不存在");
        }
        // 3.校验优惠券状态 只有待发放和暂停状态的才能发
        if (coupon.getStatus() != CouponStatus.DRAFT && coupon.getStatus() != CouponStatus.UN_ISSUE) {
            throw new BizIllegalException("优惠券状态不正确,只有待发放和暂停中的优惠券才能发放");
        }
        // 优惠券是否是立刻发放
        LocalDateTime now = LocalDateTime.now();
        boolean isBeginIssue = dto.getIssueBeginTime() == null || !dto.getIssueBeginTime().isAfter(now);
        // 4.修改优惠券的 领取时间和结束时间 使用有效期开始时间和介绍时间 天数 状态
        // 方式一：
        /*if (isBeginIssue) {
            coupon.setIssueBeginTime(dto.getIssueBeginTime() == null ? now : dto.getIssueBeginTime());
            coupon.setIssueEndTime(dto.getIssueBeginTime());
            coupon.setTermDays(dto.getTermDays());
            coupon.setTermBeginTime(dto.getTermBeginTime());
            coupon.setTermEndTime(dto.getTermEndTime());
            // 立刻发放 优惠券状态修改为进行中
            coupon.setStatus(CouponStatus.ISSUING);
        } else {
            coupon.setIssueBeginTime(dto.getIssueBeginTime());
            coupon.setIssueEndTime(dto.getIssueEndTime());
            coupon.setTermDays(dto.getTermDays());
            coupon.setTermBeginTime(dto.getTermBeginTime());
            coupon.setTermEndTime(dto.getTermEndTime());
            coupon.setStatus(CouponStatus.UN_ISSUE);
        }*/

        // 方式二：
        Coupon temp = BeanUtils.copyBean(dto, Coupon.class);
        if (isBeginIssue) {
            temp.setStatus(CouponStatus.ISSUING);
            temp.setIssueBeginTime(dto.getIssueBeginTime() == null ? now : dto.getIssueBeginTime());
        } else {
            temp.setStatus(CouponStatus.UN_ISSUE);
        }
        this.updateById(temp);

        // 5.如果优惠券是立即发放将优惠券信息存入Redis的hash中
        if (isBeginIssue) {
            String key = PromotionConstants.COUPON_CACHE_KEY_PREFIX + id;
//            redisTemplate.opsForHash().put(key, "issueBeginTime",String.valueOf(DateUtils.toEpochMilli(dto.getIssueBeginTime())));
//            redisTemplate.opsForHash().put(key, "issueEndTime",String.valueOf(DateUtils.toEpochMilli(dto.getIssueEndTime())));
//            redisTemplate.opsForHash().put(key, "totalNum",String.valueOf(coupon.getTotalNum()));
//            redisTemplate.opsForHash().put(key, "userLimit",String.valueOf(coupon.getUserLimit()));

            Map<String, String> map = new HashMap<>();
            map.put("issueBeginTime", String.valueOf(DateUtils.toEpochMilli(now)));
            map.put("issueEndTime", String.valueOf(DateUtils.toEpochMilli(dto.getIssueEndTime())));
            map.put("totalNum", String.valueOf(coupon.getTotalNum()));
            map.put("userLimit", String.valueOf(coupon.getUserLimit()));
            redisTemplate.opsForHash().putAll(key, map);
        }

        // 6.如果优惠券的 领取方式为 制定发送 且 优惠券之前的状态是待发放，需要生成兑换码
        if (coupon.getObtainWay() == ObtainType.ISSUE && coupon.getStatus() == CouponStatus.DRAFT) {
            // 兑换码的兑换的截止时间，就是优惠券领取的截止时间；该时间是从前端传的
            coupon.setIssueEndTime(dto.getIssueEndTime());
            // 异步生成兑换码
            exchangeCodeService.asyncGenerateExchangeCode(coupon);
        }
    }

    @Override
    public List<CouponVO> queryIssuingCoupons() {
        // 1.查询db coupon 条件：发放中 手动领取
        List<Coupon> couponList = this.lambdaQuery()
                .eq(Coupon::getStatus, CouponStatus.ISSUING)
                .eq(Coupon::getObtainWay, ObtainType.PUBLIC)
                .list();
        if (CollUtils.isEmpty(couponList)) {
            return CollUtils.emptyList();
        }

        // 2.查询用户券user_coupon 条件：当前用户 发放中的优惠券id
        Set<Long> couponIds = couponList.stream().map(Coupon::getId).collect(Collectors.toSet());
        List<UserCoupon> list = userCouponService.lambdaQuery()
                .eq(UserCoupon::getUserId, UserContext.getUser())
                .in(UserCoupon::getCouponId, couponIds)
                .list();

        // 2.1 统计当前用户 针对每一个券 的已领取数量
        // 常规
        /*Map<Long, Long> issueMap = new HashMap<>();
        // 键：优惠券id 值：已领数量
        // 101 2
        // 102 1
        for (UserCoupon userCoupon : list) {
            // 优惠券领取数量
            Long num = issueMap.get(userCoupon.getCouponId());
            if (num == null) {
                issueMap.put(userCoupon.getCouponId(), 1L);
            } else {
                issueMap.put(userCoupon.getCouponId(), num + 1);
            }
        }*/
        // stream
        Map<Long, Long> issueMap = list.stream()
                .collect(Collectors.groupingBy(UserCoupon::getCouponId, Collectors.counting()));
        // 2.2 统计当前用户 针对每一个卷 的已领且未使用的数量
        Map<Long, Long> unuseMap = list.stream()
                .filter(c -> c.getStatus() == UserCouponStatus.UNUSED)
                .collect(Collectors.groupingBy(UserCoupon::getCouponId, Collectors.counting()));
        // 3.po转vo
        List<CouponVO> voList = new ArrayList<>();
        for (Coupon c : couponList) {
            CouponVO vo = BeanUtils.copyBean(c, CouponVO.class);
            // 优惠券还有剩余 （issue_num < total_num）且（统计的用户券表user_coupon取出当前用户已经领取数量<user_limit）
            Long issNum = issueMap.getOrDefault(c.getId(), 0L);
            boolean available = c.getIssueNum() < c.getTotalNum() && issNum < c.getUserLimit();
            // 是否可以领取
            vo.setAvailable(available);
            // 统计的用户券表user_coupon取出当前用户已经领取数量且未使用的卷数量
            boolean receive = unuseMap.getOrDefault(c.getId(), 0L) > 0;
            // 是否可以使用
            vo.setReceived(receive);
            voList.add(vo);
        }

        return voList;
    }
}




