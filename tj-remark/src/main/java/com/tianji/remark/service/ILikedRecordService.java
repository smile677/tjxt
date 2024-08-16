package com.tianji.remark.service;

import com.tianji.remark.domain.dto.LikeRecordFormDTO;
import com.tianji.remark.domain.po.LikedRecord;
import com.baomidou.mybatisplus.extension.service.IService;

/**
* @author smile67
* @description 针对表【liked_record(点赞记录表)】的数据库操作Service
* @createDate 2024-08-16 11:51:27
*/
public interface ILikedRecordService extends IService<LikedRecord> {

    void addLikeRecord(LikeRecordFormDTO dto);
}
