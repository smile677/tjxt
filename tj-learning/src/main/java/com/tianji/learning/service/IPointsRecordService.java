package com.tianji.learning.service;

import com.tianji.learning.domain.po.PointsRecord;
import com.baomidou.mybatisplus.extension.service.IService;
import com.tianji.learning.enums.PointsRecordType;
import com.tianji.learning.mq.msg.SignInMessage;

/**
* @author smile67
* @description 针对表【points_record(学习积分记录，每个月底清零)】的数据库操作Service
* @createDate 2024-08-17 23:10:07
*/
public interface IPointsRecordService extends IService<PointsRecord> {

    void addPointRecord(SignInMessage msg, PointsRecordType pointsRecordType);
}
