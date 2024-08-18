package com.tianji.learning.service.impl;


import com.tianji.common.autoconfigure.mq.RabbitMqHelper;
import com.tianji.common.constants.MqConstants;
import com.tianji.common.exceptions.BizIllegalException;
import com.tianji.common.utils.CollUtils;
import com.tianji.common.utils.UserContext;
import com.tianji.learning.constants.RedisConstants;
import com.tianji.learning.domain.vo.SignResultVO;
import com.tianji.learning.mq.msg.SignInMessage;
import com.tianji.learning.service.ISignRecordService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.connection.BitFieldSubCommands;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;


import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class SignRecordServiceImpl implements ISignRecordService {
    private final StringRedisTemplate redisTemplate;
    private final RabbitMqHelper mqHelper;

    @Override
    public SignResultVO addSignRecords() {
        // 1.获取用户id
        Long userId = UserContext.getUser();
        // 2.拼接key
//        SimpleDateFormat format = new SimpleDateFormat("yyyyMM");
//        format.format(new Date());
        LocalDate now = LocalDate.now();
        String format = now.format(DateTimeFormatter.ofPattern(":yyyyMM"));
        String key = RedisConstants.SIGN_RECORD_KEY_PREFIX + userId.toString() + format;

        // 3.利用bitset命令,将签到记录保存到redis的bitmap结构中, 需要校验是否已经签到
        int offset = now.getDayOfMonth() - 1;
        // 返回该offset上原来的值
        Boolean setBit = redisTemplate.opsForValue().setBit(key, offset, true);
        if (setBit) {
            // 说明已经签到过了
            throw new BizIllegalException("不能重复签到");
        }
        // 4.计算连续签到的天数 与运算
        int days = countSignDays(key, now.getDayOfMonth());
        // 5.计算连续签到 奖励积分
        int rewardPoints = 0;
        switch (days) {
            case 7:
                rewardPoints = 10;
                break;
            case 14:
                rewardPoints = 20;
                break;
            case 28:
                rewardPoints = 40;
                break;
        }

        //  6.保存积分
        mqHelper.send(MqConstants.Exchange.LEARNING_EXCHANGE,
                MqConstants.Key.SIGN_IN,
                SignInMessage.of(userId, rewardPoints + 1));

        // 7.返回结果
        SignResultVO signResultVO = new SignResultVO();
        // 连续签到天数
        signResultVO.setSignDays(days);
        // 签到得分 需要注意上限问题
//        signResultVO.setSignPoints(realSignPoints);
        // 连续签到奖励积分,连续签到7天以上才有奖励
        signResultVO.setRewardPoints(rewardPoints);
        return signResultVO;
    }

    @Override
    public List<Long> getAllSignRecords() {
        Long userId = UserContext.getUser();
        LocalDate now = LocalDate.now();
        String format = now.format(DateTimeFormatter.ofPattern(":yyyyMM"));
        String key = RedisConstants.SIGN_RECORD_KEY_PREFIX + userId.toString() + format;
        List<Long> bitField = redisTemplate.opsForValue()
                .bitField(key, BitFieldSubCommands
                        .create()
                        .get(BitFieldSubCommands.BitFieldType.unsigned(now.getDayOfMonth()))
                        .valueAt(0));
        if (CollUtils.isEmpty(bitField)) {
            return Collections.emptyList();
        }
        // 本月第一天到今天的签到数据 拿到的十进制数据
        Long num = bitField.get(0);
        log.debug("num  {}", num);
        // 2.num转二进制
        List<Long> binaryDigits = new ArrayList<>(now.getDayOfMonth() + 1);
        // 先填充前导0
        for (int i = 0; i < now.getDayOfMonth(); i++) {
            binaryDigits.add(0L);
        }
        // 从最低有效位开始填充实际的二进制位
        // 从最后一个位置开始
        int index = now.getDayOfMonth() - 1;
        while (num > 0 && index >= 0) {
            binaryDigits.set(index--, num % 2);
            // 右移一位
            num >>= 1;
        }
        return binaryDigits;
    }

    /**
     * 计算连续签到天数
     *
     * @param key        签到记录的key
     * @param dayOfMonth 本月第一天到今天的天数
     * @return
     */
    private int countSignDays(String key, int dayOfMonth) {
        // 1.求本月第一天到今天所有的签到数据 bitfiled 得到的是十进制数据
        // bitfield key get u天数 0
        List<Long> bitField = redisTemplate.opsForValue()
                .bitField(key, BitFieldSubCommands
                        .create()
                        .get(BitFieldSubCommands.BitFieldType.unsigned(dayOfMonth))
                        .valueAt(0));
        if (CollUtils.isEmpty(bitField)) {
            return 0;
        }
        // 本月第一天到今天的签到数据 拿到的十进制数据
        Long num = bitField.get(0);
        log.debug("num  {}", num);
        // 2.num转二进制 从后往前推进共有多少个1 &与运算 右移一位
        // 计数器
        int counter = 0;
        while ((num & 1) == 1) {
            counter++;
            // 无符号右移, 高位补0, 适用于非负数或者处理正整数
            num = num >>> 1;
        }
        return counter;
    }

    public static void main(String[] args) {
        LocalDate now = LocalDate.now();
        String format = now.format(DateTimeFormatter.ofPattern(":yyyyMM"));
        System.out.println("format = " + format);
        String key = RedisConstants.SIGN_RECORD_KEY_PREFIX + 2 + format;
        System.out.println("key = " + key);
    }
}
