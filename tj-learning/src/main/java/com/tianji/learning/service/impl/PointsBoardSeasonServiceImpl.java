package com.tianji.learning.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tianji.learning.domain.po.PointsBoardSeason;
import com.tianji.learning.service.IPointsBoardSeasonService;
import com.tianji.learning.mapper.PointsBoardSeasonMapper;
import org.springframework.stereotype.Service;

import static com.tianji.learning.constants.LearningConstants.POINTS_BOARD_TABLE_PREFIX;

/**
 * @author smile67
 * @description 针对表【points_board_season】的数据库操作Service实现
 * @createDate 2024-08-17 23:10:07
 */
@Service
public class PointsBoardSeasonServiceImpl extends ServiceImpl<PointsBoardSeasonMapper, PointsBoardSeason>
        implements IPointsBoardSeasonService {
    // 方法作用: 创建上赛季的表
    @Override
    public void createPointsBoardLastestTable(Integer seasonId) {
        getBaseMapper().createPointsBoardTable(POINTS_BOARD_TABLE_PREFIX + seasonId);
    }
}




