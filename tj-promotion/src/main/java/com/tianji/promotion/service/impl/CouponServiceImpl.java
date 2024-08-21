package com.tianji.promotion.service.impl;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tianji.common.domain.dto.PageDTO;
import com.tianji.common.exceptions.BadRequestException;
import com.tianji.common.utils.BeanUtils;
import com.tianji.common.utils.CollUtils;
import com.tianji.common.utils.StringUtils;
import com.tianji.promotion.domain.dto.CouponFormDTO;
import com.tianji.promotion.domain.po.Coupon;
import com.tianji.promotion.domain.po.CouponScope;
import com.tianji.promotion.domain.query.CouponQuery;
import com.tianji.promotion.domain.vo.CouponPageVO;
import com.tianji.promotion.service.ICouponScopeService;
import com.tianji.promotion.service.ICouponService;
import com.tianji.promotion.mapper.CouponMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
        if (CollUtils.isEmpty(records)){
            return PageDTO.empty(page.getTotal(), page.getPages());
        }
        // 2.封装vo
        return PageDTO.of(page,BeanUtils.copyList(records, CouponPageVO.class));
    }
}




