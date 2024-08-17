package com.tianji.learning.mq;

import com.tianji.api.dto.msg.LikedTimesDTO;
import com.tianji.common.constants.MqConstants;
import com.tianji.learning.domain.po.InteractionReply;
import com.tianji.learning.service.IInteractionReplyService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.ExchangeTypes;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
@Slf4j
@RequiredArgsConstructor
public class LikedRecordListener {
    private final IInteractionReplyService interactionReplyService;

    /**
     * QA问答系统 消费者
     *
     * @param list 点赞次数
     */
    @RabbitListener(bindings = @QueueBinding(
            value = @Queue(name = "qa.liked.times.queue", durable = "true"),
            exchange = @Exchange(name = MqConstants.Exchange.LIKE_RECORD_EXCHANGE, type = ExchangeTypes.TOPIC),
            key = MqConstants.Key.QA_LIKED_TIMES_KEY
    ))
    public void onMsg(List<LikedTimesDTO> list) {
        log.debug("LikedRecordListener 监听到消息{}", list);
        // 消息转po
        List<InteractionReply> replyList = new ArrayList<>();
        for (LikedTimesDTO likedTimesDTO : list) {
            InteractionReply interactionReply = new InteractionReply();
            interactionReply.setId(likedTimesDTO.getBizId());
            interactionReply.setLikedTimes(likedTimesDTO.getLikedTimes());
            replyList.add(interactionReply);
        }
        interactionReplyService.saveOrUpdateBatch(replyList);
    }

}
