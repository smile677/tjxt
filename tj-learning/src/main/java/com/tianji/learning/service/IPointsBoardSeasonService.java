package com.tianji.learning.service;

import com.tianji.learning.domain.po.PointsBoardSeason;
import com.baomidou.mybatisplus.extension.service.IService;

/**
* @author smile67
* @description 针对表【points_board_season】的数据库操作Service
* @createDate 2024-08-17 23:10:07
*/
public interface IPointsBoardSeasonService extends IService<PointsBoardSeason> {
    void createPointsBoardLastestTable(Integer id);
}
