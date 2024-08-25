package com.tianji.learning.constants;

public interface RedisConstants {
    /**
     * 签到记录的key的前缀: sign:uid:110:202408
     */
    String SIGN_RECORD_KEY_PREFIX = "sign:uid:";
    /**
     * 积分排行榜key前缀 完整格式为 boards:年月
     */
    String POINTS_BOARD_KEY_PREFIX = "boards:";
    /**
     * 动态表名前缀
     */
    String POINTS_BOARD_TABLE_PREFIX = "points_board_";
}
