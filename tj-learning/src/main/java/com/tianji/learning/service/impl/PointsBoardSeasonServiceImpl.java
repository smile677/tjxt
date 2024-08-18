package com.tianji.learning.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tianji.learning.domain.po.PointsBoardSeason;
import com.tianji.learning.service.IPointsBoardSeasonService;
import com.tianji.learning.mapper.PointsBoardSeasonMapper;
import org.springframework.stereotype.Service;

/**
* @author smile67
* @description 针对表【points_board_season】的数据库操作Service实现
* @createDate 2024-08-17 23:10:07
*/
@Service
public class PointsBoardSeasonServiceImpl extends ServiceImpl<PointsBoardSeasonMapper, PointsBoardSeason>
    implements IPointsBoardSeasonService {
}




