package com.tianji.learning.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.tianji.learning.domain.po.LearningRecord;
import org.apache.ibatis.annotations.Mapper;

/**
 * @author smile67
 * @description 针对表【learning_record(学习记录表)】的数据库操作Mapper
 * @createDate 2024-08-10 21:33:10
 * @Entity com.tianji.learning.domain.po.LearningRecord
 */
@Mapper
public interface LearningRecordMapper extends BaseMapper<LearningRecord> {

}




