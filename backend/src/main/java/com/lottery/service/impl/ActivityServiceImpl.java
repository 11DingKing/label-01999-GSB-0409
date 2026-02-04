package com.lottery.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.lottery.common.ResultCode;
import com.lottery.dto.response.ActivityResponse;
import com.lottery.dto.response.PrizeResponse;
import com.lottery.entity.Activity;
import com.lottery.entity.Prize;
import com.lottery.exception.BusinessException;
import com.lottery.mapper.ActivityMapper;
import com.lottery.mapper.PrizeMapper;
import com.lottery.service.ActivityService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 活动服务实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ActivityServiceImpl implements ActivityService {

    private final ActivityMapper activityMapper;
    private final PrizeMapper prizeMapper;

    @Override
    public List<ActivityResponse> getActivityList() {
        List<Activity> activities = activityMapper.selectList(
                new LambdaQueryWrapper<Activity>()
                        .eq(Activity::getStatus, 1)
                        .orderByDesc(Activity::getCreatedAt)
        );

        return activities.stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    @Override
    public ActivityResponse getActivityDetail(Long activityId) {
        Activity activity = activityMapper.selectById(activityId);
        if (activity == null) {
            throw new BusinessException(ResultCode.ACTIVITY_NOT_EXIST);
        }
        return convertToResponse(activity);
    }

    @Override
    public List<PrizeResponse> getActivityPrizes(Long activityId) {
        // 验证活动存在
        Activity activity = activityMapper.selectById(activityId);
        if (activity == null) {
            throw new BusinessException(ResultCode.ACTIVITY_NOT_EXIST);
        }

        List<Prize> prizes = prizeMapper.selectList(
                new LambdaQueryWrapper<Prize>()
                        .eq(Prize::getActivityId, activityId)
                        .eq(Prize::getStatus, 1)
                        .orderByAsc(Prize::getSortOrder)
        );

        return prizes.stream()
                .map(this::convertToPrizeResponse)
                .collect(Collectors.toList());
    }

    private ActivityResponse convertToResponse(Activity activity) {
        LocalDateTime now = LocalDateTime.now();
        String statusText;
        
        if (activity.getStatus() != 1) {
            statusText = "已下线";
        } else if (now.isBefore(activity.getStartTime())) {
            statusText = "未开始";
        } else if (now.isAfter(activity.getEndTime())) {
            statusText = "已结束";
        } else {
            statusText = "进行中";
        }

        return ActivityResponse.builder()
                .id(activity.getId())
                .name(activity.getName())
                .description(activity.getDescription())
                .coverImage(activity.getCoverImage())
                .startTime(activity.getStartTime())
                .endTime(activity.getEndTime())
                .dailyLimit(activity.getDailyLimit())
                .totalLimit(activity.getTotalLimit())
                .status(activity.getStatus())
                .statusText(statusText)
                .build();
    }

    private PrizeResponse convertToPrizeResponse(Prize prize) {
        return PrizeResponse.builder()
                .id(prize.getId())
                .name(prize.getName())
                .image(prize.getImage())
                .totalCount(prize.getTotalCount())
                .remainingCount(prize.getRemainingCount())
                .probability(prize.getProbability())
                .sortOrder(prize.getSortOrder())
                .build();
    }
}
