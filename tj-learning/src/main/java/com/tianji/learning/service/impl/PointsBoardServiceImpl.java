package com.tianji.learning.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tianji.api.client.user.UserClient;
import com.tianji.api.dto.user.UserDTO;
import com.tianji.common.exceptions.BizIllegalException;
import com.tianji.common.utils.CollUtils;
import com.tianji.common.utils.UserContext;
import com.tianji.learning.constants.RedisConstants;
import com.tianji.learning.domain.po.PointsBoard;
import com.tianji.learning.domain.query.PointsBoardQuery;
import com.tianji.learning.domain.vo.PointsBoardItemVO;
import com.tianji.learning.domain.vo.PointsBoardVO;
import com.tianji.learning.service.IPointsBoardService;
import com.tianji.learning.mapper.PointsBoardMapper;
import lombok.AllArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author smile67
 * @description 针对表【points_board(学霸天梯榜)】的数据库操作Service实现
 * @createDate 2024-08-17 23:10:07
 */
@Service
@AllArgsConstructor
public class PointsBoardServiceImpl extends ServiceImpl<PointsBoardMapper, PointsBoard>
        implements IPointsBoardService {
    private final StringRedisTemplate redisTemplate;
    private final UserClient userClient;

    @Override
    public PointsBoardVO queryPointsBoardList(PointsBoardQuery query) {
        // 1.判断是查当前赛季还是历史赛季 query.season 赛季id,为null或者0代表查询当前赛季
        LocalDate now = LocalDate.now();
        String format = now.format(DateTimeFormatter.ofPattern("yyyyMM"));
        String key = RedisConstants.POINTS_BOARD_KEY_PREFIX + format;
        // 查询当前赛季
        boolean isCurrentSeason = query.getSeason() == null || query.getSeason() == 0;
        // 查询历史赛季
        Long season = query.getSeason();
        // 2. 查询我的排名和积分 根据 query.season 判断是查redis还是db
        PointsBoard myBoard;
        if (isCurrentSeason) {
            myBoard = queryMyCurrentBoard(key);
        } else {
            myBoard = queryMyHistoryBoard(season);
        }
        // 3.分页查询赛季列表 根据 query.season 判断是查redis还是db
        List<PointsBoard> list;
        if (isCurrentSeason) {
            list = queryCurrentBoard(key, query.getPageNo(), query.getPageSize());
        } else {
            list = queryHistoryBoard(query);
        }
        // 4.封装用户id集合 远程调用用户服务 获取用户信息 转map
        Set<Long> uIds = list.stream().map(PointsBoard::getUserId).collect(Collectors.toSet());
        List<UserDTO> userDTOS = userClient.queryUserByIds(uIds);
        if (CollUtils.isEmpty(userDTOS)) {
            throw new BizIllegalException("用户不存在");
        }
        Map<Long, UserDTO> userDTOMap = userDTOS.stream().collect(Collectors.toMap(UserDTO::getId, userDTO -> userDTO));

        // 5.封装返回vo
        PointsBoardVO vo = new PointsBoardVO();
        vo.setPoints(myBoard.getPoints());
        vo.setRank(myBoard.getRank());
        List<PointsBoardItemVO> voList = new ArrayList<>();
        for (PointsBoard pointsBoard : list) {
            PointsBoardItemVO pointsBoardItemVO = new PointsBoardItemVO();
            pointsBoardItemVO.setRank(pointsBoard.getRank());
            pointsBoardItemVO.setPoints(pointsBoard.getPoints());
            pointsBoardItemVO.setName(userDTOMap.get(pointsBoard.getUserId()).getName());
            voList.add(pointsBoardItemVO);
        }
        vo.setBoardList(voList);
        return vo;
    }

    // 分页查询历史赛季列表 db
    private List<PointsBoard> queryHistoryBoard(PointsBoardQuery query) {
        // todo
        return null;
    }

    // 分页查询当前赛季列表  zset查 redis
    @Override
    public List<PointsBoard> queryCurrentBoard(String key, Integer pageNo, Integer pageSize) {
        // 1.计算start和stop位置
        int start = (pageNo - 1) * pageSize;
        int end = start + pageSize - 1;
        // 2.利用zrevrange获取分页数据 会按照分数倒序 分页查询
        Set<ZSetOperations.TypedTuple<String>> typedTuples = redisTemplate.opsForZSet().reverseRangeWithScores(key, start, end);
        if (CollUtils.isEmpty(typedTuples)) {
            return CollUtils.emptyList();
        }
        // 3.封装返回结果
        List<PointsBoard> list = new ArrayList<>();
        int rank = start + 1;
        for (ZSetOperations.TypedTuple<String> typedTuple : typedTuples) {
            // 获取userId
            String value = typedTuple.getValue();
            // 总积分值
            Double score = typedTuple.getScore();
            // 判空
            if (value == null || score == null) {
                continue;
            }
            PointsBoard pointsBoard = new PointsBoard();
            pointsBoard.setUserId(Long.valueOf(value));
            pointsBoard.setRank(rank++);
            pointsBoard.setPoints(score.intValue());
            list.add(pointsBoard);
        }
        return list;
    }

    // 查询历史赛季 我的积分和排名 db
    private PointsBoard queryMyHistoryBoard(Long season) {
        // todo
        return null;
    }

    // 查询当前赛季 我的积分和排名 redis
    private PointsBoard queryMyCurrentBoard(String key) {
        Long userId = UserContext.getUser();
        // 获取分值
        Double score = redisTemplate.opsForZSet().score(key, userId.toString());
        // 获取排名
        Long rank = redisTemplate.opsForZSet().reverseRank(key, userId.toString());
        PointsBoard pointsBoard = new PointsBoard();
        pointsBoard.setRank(rank == null ? 0 : rank.intValue() + 1);
        pointsBoard.setPoints(score == null ? 0 : score.intValue());
        return pointsBoard;
    }
}

