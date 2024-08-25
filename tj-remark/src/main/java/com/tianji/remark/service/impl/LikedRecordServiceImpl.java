//package com.tianji.remark.service.impl;
//
//import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
//import com.tianji.common.autoconfigure.mq.RabbitMqHelper;
//import com.tianji.common.constants.MqConstants;
//import com.tianji.common.utils.StringUtils;
//import com.tianji.common.utils.UserContext;
//import com.tianji.remark.domain.dto.LikeRecordFormDTO;
//import com.tianji.api.dto.msg.LikedTimesDTO;
//import com.tianji.remark.domain.po.LikedRecord;
//import com.tianji.remark.service.ILikedRecordService;
//import com.tianji.remark.mapper.LikedRecordMapper;
//import lombok.RequiredArgsConstructor;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.stereotype.Service;
//
//import java.util.List;
//import java.util.Set;
//import java.util.stream.Collectors;
//
///**
// * @author smile67
// * @description 针对表【liked_record(点赞记录表)】的数据库操作Service实现
// * @createDate 2024-08-16 11:51:27
// */
//@Service
//@RequiredArgsConstructor
//@Slf4j
//public class LikedRecordServiceImpl extends ServiceImpl<LikedRecordMapper, LikedRecord>
//        implements ILikedRecordService {
//    private final RabbitMqHelper rabbitMqHelper;
//
//    @Override
//    public void addLikeRecord(LikeRecordFormDTO dto) {
//        // 1.获取当前登录用户
//        Long userId = UserContext.getUser();
//        // 2.判断是否点赞 dto.liked 为true则则是点赞
//        /*boolean flag = true;
//        if (dto.getLiked()) {
//            // 2.1 点赞逻辑
//            flag = liked(dto, userId);
//        } else {
//            // 2.2 取消点赞逻辑
//            flag = unliked(dto, userId);
//        }*/
//        boolean flag = dto.getLiked() ? liked(dto, userId) : unliked(dto, userId);
//        // 说明点赞或者取消点赞 失败
//        if (!flag) {
//            return;
//        }
//        // 3.统计该业务id的总点赞数
//        Integer totalLikesNum = this.lambdaQuery()
//                .eq(LikedRecord::getBizId, dto.getBizId())
//                .count();
//        // 4.发生消息到mq
//        String routeKey = StringUtils.format(MqConstants.Key.LIKED_TIMES_KEY_TEMPLATE, dto.getBizType());
//        LikedTimesDTO msg = LikedTimesDTO.of(dto.getBizId(), totalLikesNum);
//        log.debug("发送的点赞消息：{}", msg);
//        rabbitMqHelper.send(
//                MqConstants.Exchange.LIKE_RECORD_EXCHANGE,
//                routeKey,
//                msg
//        );
//    }
//
//    @Override
//    public Set<Long> getLikesStatusByBizIds(List<Long> bizIds) {
//        // 1.获取用户id
//        Long userId = UserContext.getUser();
//        // 2.查点赞记录表 in bizIds
//        List<LikedRecord> recordList = this.lambdaQuery()
//                .eq(LikedRecord::getUserId, userId)
//                .in(LikedRecord::getBizId, bizIds)
//                .list();
//        // 3.将查询到的bizIds转成集合返回
//        return recordList.stream().map(LikedRecord::getBizId).collect(Collectors.toSet());
//    }
//
//    // 取消点赞
//    private boolean unliked(LikeRecordFormDTO dto, Long userId) {
//        LikedRecord likedRecord = this.lambdaQuery()
//                .eq(LikedRecord::getUserId, userId)
//                .eq(LikedRecord::getBizId, dto.getBizId())
//                .one();
//        if (likedRecord == null) {
//            // 说明没有点过赞 取消点赞失败
//            return false;
//        }
//        // 删除点赞记录
//        return this.removeById(likedRecord.getId());
//    }
//
//    // 点赞
//    private boolean liked(LikeRecordFormDTO dto, Long userId) {
//        LikedRecord likedRecord = this.lambdaQuery()
//                .eq(LikedRecord::getBizId, dto.getBizId())
//                .eq(LikedRecord::getUserId, userId)
//                .one();
//        if (likedRecord != null) {
//            // 说明之前点赞过，此次点赞操作失败
//            return false;
//        }
//        // 保存点赞记录到表中
//        LikedRecord likedRecord1 = new LikedRecord();
//        likedRecord1.setBizId(dto.getBizId());
//        likedRecord1.setUserId(userId);
//        likedRecord1.setBizType(dto.getBizType());
//        return this.save(likedRecord1);
//    }
//}
//
//
//
//
