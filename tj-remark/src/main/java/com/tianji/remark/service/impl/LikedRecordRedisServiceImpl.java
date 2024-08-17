package com.tianji.remark.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tianji.api.dto.msg.LikedTimesDTO;
import com.tianji.common.autoconfigure.mq.RabbitMqHelper;
import com.tianji.common.constants.MqConstants;
import com.tianji.common.utils.StringUtils;
import com.tianji.common.utils.UserContext;
import com.tianji.remark.domain.dto.LikeRecordFormDTO;
import com.tianji.remark.domain.po.LikedRecord;
import com.tianji.remark.mapper.LikedRecordMapper;
import com.tianji.remark.service.ILikedRecordService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author smile67
 * @description 针对表【liked_record(点赞记录表)】的数据库操作Service实现
 * @createDate 2024-08-16 11:51:27
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class LikedRecordRedisServiceImpl extends ServiceImpl<LikedRecordMapper, LikedRecord>
        implements ILikedRecordService {
    private final RabbitMqHelper rabbitMqHelper;
    private final StringRedisTemplate redisTemplate;

    @Override
    public void addLikeRecord(LikeRecordFormDTO dto) {
        // 1.获取当前登录用户
        Long userId = UserContext.getUser();
        // 2.判断是否点赞 dto.liked 为true则则是点赞
        /*boolean flag = true;
        if (dto.getLiked()) {
            // 2.1 点赞逻辑
            flag = liked(dto, userId);
        } else {
            // 2.2 取消点赞逻辑
            flag = unliked(dto, userId);
        }*/
        boolean flag = dto.getLiked() ? liked(dto, userId) : unliked(dto, userId);
        // 说明点赞或者取消点赞 失败
        if (!flag) {
            return;
        }
        // 3.统计该业务id的总点赞数
       /* Integer totalLikesNum = this.lambdaQuery()
                .eq(LikedRecord::getBizId, dto.getBizId())
                .count();*/
        // 拼接key
        String key = RedisConstants.LIKE_BIZ_KEY_PREFIX + dto.getBizId();
        Long totalLikesNum = redisTemplate.opsForSet().size(key);
        if (totalLikesNum ==null) {
            return;
        }
        // 采用 zset 结构缓存点赞的总数 likes:times:type:QA likes:times:type:NOTE
        String bizTypeTotalLikeKey = RedisConstants.LIKE_COUNT_KEY_PREFIX + dto.getBizId();
        Boolean add = redisTemplate.opsForZSet().add(bizTypeTotalLikeKey, dto.getBizId().toString(), totalLikesNum);
        if (!add) {
            log.debug("统计点赞总数失败");
            return;
        }
        // 4.发生消息到mq
        String routeKey = StringUtils.format(MqConstants.Key.LIKED_TIMES_KEY_TEMPLATE, dto.getBizType());
        LikedTimesDTO msg = LikedTimesDTO.of(dto.getBizId(), totalLikesNum.intValue());
        log.debug("发送的点赞消息：{}", msg);
        rabbitMqHelper.send(
                MqConstants.Exchange.LIKE_RECORD_EXCHANGE,
                routeKey,
                msg
        );
    }

    @Override
    public Set<Long> getLikesStatusByBizIds(List<Long> bizIds) {
        // 1.获取用户id
        Long userId = UserContext.getUser();
        // 2.查点赞记录表 in bizIds
        List<LikedRecord> recordList = this.lambdaQuery()
                .eq(LikedRecord::getUserId, userId)
                .in(LikedRecord::getBizId, bizIds)
                .list();
        // 3.将查询到的bizIds转成集合返回
        return recordList.stream().map(LikedRecord::getBizId).collect(Collectors.toSet());
    }

    // 取消点赞
    private boolean unliked(LikeRecordFormDTO dto, Long userId) {
        // 拼接key
        String key = RedisConstants.LIKE_BIZ_KEY_PREFIX + dto.getBizId();
        // 从set结构中删除当前userId
        Long result = redisTemplate.opsForSet().remove(key, userId.toString());
        // 返回结果
        return result != null && result > 0;
    }

    // 点赞
    private boolean liked(LikeRecordFormDTO dto, Long userId) {
        // 基于Redis做点赞
        // 拼接key
        String key = RedisConstants.LIKE_BIZ_KEY_PREFIX + dto.getBizId();
        // redisTemplate 往Redis的set结构中添加 点赞记录
//        redisTemplate.boundSetOps(key).add(userId.toString());
        Long result = redisTemplate.opsForSet().add(key, userId.toString());
        // 返回结果
        return result != null && result > 0;
    }
}




