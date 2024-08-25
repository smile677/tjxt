package com.tianji.promotion.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tianji.promotion.domain.po.Promotion;
import com.tianji.promotion.service.IPromotionService;
import com.tianji.promotion.mapper.PromotionMapper;
import org.springframework.stereotype.Service;

/**
* @author smile67
* @description 针对表【promotion(促销活动，形式多种多样，例如：优惠券)】的数据库操作Service实现
* @createDate 2024-08-19 18:48:06
*/
@Service
public class PromotionServiceImpl extends ServiceImpl<PromotionMapper, Promotion>
    implements IPromotionService {

}




