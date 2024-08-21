package com.tianji.promotion.service.impl;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tianji.common.domain.dto.PageDTO;
import com.tianji.common.exceptions.BadRequestException;
import com.tianji.common.exceptions.BizIllegalException;
import com.tianji.common.utils.BeanUtils;
import com.tianji.common.utils.CollUtils;
import com.tianji.common.utils.StringUtils;
import com.tianji.promotion.domain.dto.CouponFormDTO;
import com.tianji.promotion.domain.dto.CouponIssueFormDTO;
import com.tianji.promotion.domain.po.Coupon;
import com.tianji.promotion.domain.po.CouponScope;
import com.tianji.promotion.domain.query.CouponQuery;
import com.tianji.promotion.domain.vo.CouponPageVO;
import com.tianji.promotion.enums.CouponStatus;
import com.tianji.promotion.enums.ObtainType;
import com.tianji.promotion.service.ICouponScopeService;
import com.tianji.promotion.service.ICouponService;
import com.tianji.promotion.mapper.CouponMapper;
import com.tianji.promotion.service.IExchangeCodeService;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

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
        log.debug("发放优惠券 线程名："+Thread.currentThread().getName());
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

        // 5.如果优惠券的 领取方式为 制定发送 且 优惠券之前的状态是待发放，需要生成兑换码
        if (coupon.getObtainWay() == ObtainType.ISSUE && coupon.getStatus() == CouponStatus.DRAFT) {
            // 兑换码的兑换的截止时间，就是优惠券领取的截止时间；该时间是从前端传的
            coupon.setIssueEndTime(dto.getIssueEndTime());
            // 异步生成兑换码
            exchangeCodeService.asyncGenerateExchangeCode(coupon);
        }
    }
}




