package com.tianji.learning.mq;

import com.tianji.api.dto.trade.OrderBasicDTO;
import com.tianji.common.constants.MqConstants;
import com.tianji.common.utils.CollUtils;
import com.tianji.learning.service.ILearningLessonService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.ExchangeTypes;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;


@Component
@Slf4j
@RequiredArgsConstructor // 使用构造器 Lombok是在编译期间生成相应的方法
public class LessonChangeListener {

    private final ILearningLessonService learningLessonService;


    @RabbitListener(bindings = @QueueBinding(
            value = @Queue(value = "learning.lesson.pay.queue", durable = "true"),
            exchange = @Exchange(value = MqConstants.Exchange.ORDER_EXCHANGE, type = ExchangeTypes.TOPIC),
            key = MqConstants.Key.ORDER_PAY_KEY))
    public void onLessonChange(OrderBasicDTO dto) {
        log.info("LessonChangeListener接受到消息了 用户{}，添加课程{}", dto.getUserId(), dto.getCourseIds());
        // 1.校验
        if (dto.getUserId() == null
                || dto.getOrderId() == null
                || CollUtils.isEmpty(dto.getCourseIds())
        ) {
            // 不要抛出异常，否则开启重试机制
            return;
        }
        // 2.调用service，保存课程到课表
        learningLessonService.addUserLesson(dto.getUserId(), dto.getCourseIds());
    }
}