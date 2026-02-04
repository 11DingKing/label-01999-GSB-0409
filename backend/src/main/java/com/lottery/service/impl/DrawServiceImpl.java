package com.lottery.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.lottery.common.ResultCode;
import com.lottery.dto.response.DrawCountResponse;
import com.lottery.dto.response.DrawResponse;
import com.lottery.entity.Activity;
import com.lottery.entity.Prize;
import com.lottery.entity.PrizeRecord;
import com.lottery.entity.UserDrawCount;
import com.lottery.exception.BusinessException;
import com.lottery.mapper.ActivityMapper;
import com.lottery.mapper.PrizeMapper;
import com.lottery.mapper.PrizeRecordMapper;
import com.lottery.mapper.UserDrawCountMapper;
import com.lottery.service.DrawService;
import com.lottery.util.UserContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Random;
import java.util.concurrent.TimeUnit;

/**
 * 抽奖服务实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DrawServiceImpl implements DrawService {

    private final ActivityMapper activityMapper;
    private final PrizeMapper prizeMapper;
    private final PrizeRecordMapper prizeRecordMapper;
    private final UserDrawCountMapper userDrawCountMapper;
    private final RedisTemplate<String, Object> redisTemplate;

    private static final String DRAW_LOCK_KEY = "lottery:draw:lock:";
    private final Random random = new Random();

    @Override
    @Transactional
    public DrawResponse draw(Long activityId) {
        Long userId = UserContext.getUserId();
        
        // 分布式锁防止并发抽奖
        String lockKey = DRAW_LOCK_KEY + userId + ":" + activityId;
        Boolean locked = redisTemplate.opsForValue().setIfAbsent(lockKey, "1", 5, TimeUnit.SECONDS);
        if (Boolean.FALSE.equals(locked)) {
            throw new BusinessException("操作太频繁，请稍后再试");
        }

        try {
            // 1. 验证活动
            Activity activity = validateActivity(activityId);

            // 2. 验证抽奖次数
            UserDrawCount drawCount = validateAndUpdateDrawCount(userId, activity);

            // 3. 执行抽奖算法
            Prize wonPrize = executeDraw(activityId);

            // 4. 记录抽奖结果
            PrizeRecord record = saveDrawRecord(userId, activityId, wonPrize);

            // 5. 构建响应
            if (wonPrize != null && !"谢谢参与".equals(wonPrize.getName())) {
                log.info("用户中奖: userId={}, activityId={}, prizeId={}, prizeName={}", 
                        userId, activityId, wonPrize.getId(), wonPrize.getName());
                return DrawResponse.builder()
                        .won(true)
                        .prizeId(wonPrize.getId())
                        .prizeName(wonPrize.getName())
                        .prizeImage(wonPrize.getImage())
                        .recordId(record.getId())
                        .build();
            } else {
                log.info("用户未中奖: userId={}, activityId={}", userId, activityId);
                // 获取"谢谢参与"奖品的图片
                String thanksImage = wonPrize != null ? wonPrize.getImage() : "https://img.icons8.com/fluency/200/good-luck.png";
                return DrawResponse.builder()
                        .won(false)
                        .prizeName("谢谢参与")
                        .prizeImage(thanksImage)
                        .recordId(record.getId())
                        .build();
            }
        } finally {
            redisTemplate.delete(lockKey);
        }
    }

    @Override
    public DrawCountResponse getRemainingDrawCount(Long activityId) {
        Long userId = UserContext.getUserId();
        Activity activity = activityMapper.selectById(activityId);
        if (activity == null) {
            throw new BusinessException(ResultCode.ACTIVITY_NOT_EXIST);
        }

        LocalDate today = LocalDate.now();
        UserDrawCount drawCount = userDrawCountMapper.selectOne(
                new LambdaQueryWrapper<UserDrawCount>()
                        .eq(UserDrawCount::getUserId, userId)
                        .eq(UserDrawCount::getActivityId, activityId)
                        .eq(UserDrawCount::getDrawDate, today)
        );

        int dailyUsed = drawCount != null ? drawCount.getDailyCount() : 0;
        int totalUsed = 0;
        
        // 计算总使用次数
        if (activity.getTotalLimit() > 0) {
            List<UserDrawCount> allCounts = userDrawCountMapper.selectList(
                    new LambdaQueryWrapper<UserDrawCount>()
                            .eq(UserDrawCount::getUserId, userId)
                            .eq(UserDrawCount::getActivityId, activityId)
            );
            totalUsed = allCounts.stream().mapToInt(UserDrawCount::getDailyCount).sum();
        }

        int dailyRemaining = Math.max(0, activity.getDailyLimit() - dailyUsed);
        int totalRemaining = activity.getTotalLimit() > 0 ? 
                Math.max(0, activity.getTotalLimit() - totalUsed) : -1;

        return DrawCountResponse.builder()
                .dailyLimit(activity.getDailyLimit())
                .dailyUsed(dailyUsed)
                .dailyRemaining(dailyRemaining)
                .totalLimit(activity.getTotalLimit())
                .totalUsed(totalUsed)
                .totalRemaining(totalRemaining)
                .build();
    }

    private Activity validateActivity(Long activityId) {
        Activity activity = activityMapper.selectById(activityId);
        if (activity == null) {
            throw new BusinessException(ResultCode.ACTIVITY_NOT_EXIST);
        }
        if (activity.getStatus() != 1) {
            throw new BusinessException(ResultCode.ACTIVITY_OFFLINE);
        }

        LocalDateTime now = LocalDateTime.now();
        if (now.isBefore(activity.getStartTime())) {
            throw new BusinessException(ResultCode.ACTIVITY_NOT_START);
        }
        if (now.isAfter(activity.getEndTime())) {
            throw new BusinessException(ResultCode.ACTIVITY_ENDED);
        }

        return activity;
    }

    private UserDrawCount validateAndUpdateDrawCount(Long userId, Activity activity) {
        LocalDate today = LocalDate.now();
        
        UserDrawCount drawCount = userDrawCountMapper.selectOne(
                new LambdaQueryWrapper<UserDrawCount>()
                        .eq(UserDrawCount::getUserId, userId)
                        .eq(UserDrawCount::getActivityId, activity.getId())
                        .eq(UserDrawCount::getDrawDate, today)
        );

        if (drawCount == null) {
            drawCount = new UserDrawCount();
            drawCount.setUserId(userId);
            drawCount.setActivityId(activity.getId());
            drawCount.setDrawDate(today);
            drawCount.setDailyCount(0);
            drawCount.setTotalCount(0);
        }

        // 检查每日限制
        if (drawCount.getDailyCount() >= activity.getDailyLimit()) {
            throw new BusinessException(ResultCode.DRAW_LIMIT_EXCEEDED);
        }

        // 检查总次数限制
        if (activity.getTotalLimit() > 0) {
            List<UserDrawCount> allCounts = userDrawCountMapper.selectList(
                    new LambdaQueryWrapper<UserDrawCount>()
                            .eq(UserDrawCount::getUserId, userId)
                            .eq(UserDrawCount::getActivityId, activity.getId())
            );
            int totalUsed = allCounts.stream().mapToInt(UserDrawCount::getDailyCount).sum();
            if (totalUsed >= activity.getTotalLimit()) {
                throw new BusinessException(ResultCode.DRAW_TOTAL_LIMIT_EXCEEDED);
            }
        }

        // 更新抽奖次数
        drawCount.setDailyCount(drawCount.getDailyCount() + 1);
        drawCount.setTotalCount(drawCount.getTotalCount() + 1);
        
        if (drawCount.getId() == null) {
            userDrawCountMapper.insert(drawCount);
        } else {
            userDrawCountMapper.updateById(drawCount);
        }

        return drawCount;
    }

    private Prize executeDraw(Long activityId) {
        List<Prize> prizes = prizeMapper.selectList(
                new LambdaQueryWrapper<Prize>()
                        .eq(Prize::getActivityId, activityId)
                        .eq(Prize::getStatus, 1)
                        .gt(Prize::getRemainingCount, 0)
                        .orderByAsc(Prize::getSortOrder)
        );

        if (prizes.isEmpty()) {
            return null;
        }

        // 概率抽奖算法
        double randomValue = random.nextDouble();
        double cumulativeProbability = 0;

        for (Prize prize : prizes) {
            cumulativeProbability += prize.getProbability().doubleValue();
            if (randomValue <= cumulativeProbability) {
                // 尝试扣减库存
                int updated = prizeMapper.decreaseStock(prize.getId());
                if (updated > 0) {
                    return prize;
                }
                // 库存不足，继续下一个奖品
            }
        }

        // 未中奖，返回"谢谢参与"奖品
        return prizes.stream()
                .filter(p -> "谢谢参与".equals(p.getName()))
                .findFirst()
                .orElse(null);
    }

    private PrizeRecord saveDrawRecord(Long userId, Long activityId, Prize prize) {
        PrizeRecord record = new PrizeRecord();
        record.setUserId(userId);
        record.setActivityId(activityId);
        record.setDrawTime(LocalDateTime.now());
        
        if (prize != null) {
            record.setPrizeId(prize.getId());
            record.setPrizeName(prize.getName());
            record.setStatus("谢谢参与".equals(prize.getName()) ? 1 : 0);
        } else {
            record.setPrizeName("谢谢参与");
            record.setStatus(1);
        }

        prizeRecordMapper.insert(record);
        return record;
    }
}
