package com.lottery.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.lottery.common.ResultCode;
import com.lottery.dto.request.ActivityRequest;
import com.lottery.dto.request.PrizeRequest;
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
import org.springframework.transaction.annotation.Transactional;

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
    public List<ActivityResponse> getAllActivityList() {
        List<Activity> activities = activityMapper.selectList(
                new LambdaQueryWrapper<Activity>()
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

    @Override
    public List<PrizeResponse> getAllActivityPrizes(Long activityId) {
        Activity activity = activityMapper.selectById(activityId);
        if (activity == null) {
            throw new BusinessException(ResultCode.ACTIVITY_NOT_EXIST);
        }
        List<Prize> prizes = prizeMapper.selectList(
                new LambdaQueryWrapper<Prize>()
                        .eq(Prize::getActivityId, activityId)
                        .orderByAsc(Prize::getSortOrder)
        );
        return prizes.stream()
                .map(this::convertToPrizeResponse)
                .collect(Collectors.toList());
    }

    // ========== 管理接口实现 ==========

    @Override
    @Transactional
    public ActivityResponse createActivity(ActivityRequest request) {
        validateActivityTime(request.getStartTime(), request.getEndTime());
        
        Activity activity = new Activity();
        activity.setName(request.getName());
        activity.setDescription(request.getDescription());
        activity.setCoverImage(request.getCoverImage());
        activity.setStartTime(request.getStartTime());
        activity.setEndTime(request.getEndTime());
        activity.setDailyLimit(request.getDailyLimit());
        activity.setTotalLimit(request.getTotalLimit());
        activity.setStatus(request.getStatus() != null ? request.getStatus() : 0);
        activity.setCreatedAt(LocalDateTime.now());
        activity.setUpdatedAt(LocalDateTime.now());
        
        activityMapper.insert(activity);
        log.info("创建活动成功: id={}, name={}", activity.getId(), activity.getName());
        
        // 自动创建"谢谢参与"默认奖品
        createDefaultPrize(activity.getId());
        
        return convertToResponse(activity);
    }
    
    /**
     * 创建默认的"谢谢参与"奖品
     */
    private void createDefaultPrize(Long activityId) {
        Prize prize = new Prize();
        prize.setActivityId(activityId);
        prize.setName("谢谢参与");
        prize.setImage("/images/prizes/thanks.svg");
        prize.setTotalCount(999999);
        prize.setRemainingCount(999999);
        prize.setProbability(new java.math.BigDecimal("1.0")); // 100%概率，后续添加其他奖品时调整
        prize.setSortOrder(99);
        prize.setStatus(1);
        prize.setCreatedAt(LocalDateTime.now());
        prize.setUpdatedAt(LocalDateTime.now());
        
        prizeMapper.insert(prize);
        log.info("创建默认奖品成功: activityId={}, prizeName=谢谢参与", activityId);
    }

    @Override
    @Transactional
    public ActivityResponse updateActivity(Long activityId, ActivityRequest request) {
        Activity activity = activityMapper.selectById(activityId);
        if (activity == null) {
            throw new BusinessException(ResultCode.ACTIVITY_NOT_EXIST);
        }
        
        validateActivityTime(request.getStartTime(), request.getEndTime());
        
        activity.setName(request.getName());
        activity.setDescription(request.getDescription());
        activity.setCoverImage(request.getCoverImage());
        activity.setStartTime(request.getStartTime());
        activity.setEndTime(request.getEndTime());
        activity.setDailyLimit(request.getDailyLimit());
        activity.setTotalLimit(request.getTotalLimit());
        activity.setStatus(request.getStatus());
        activity.setUpdatedAt(LocalDateTime.now());
        
        activityMapper.updateById(activity);
        log.info("更新活动成功: id={}", activityId);
        
        return convertToResponse(activity);
    }

    @Override
    @Transactional
    public void deleteActivity(Long activityId) {
        Activity activity = activityMapper.selectById(activityId);
        if (activity == null) {
            throw new BusinessException(ResultCode.ACTIVITY_NOT_EXIST);
        }
        
        // 删除活动下的所有奖品
        prizeMapper.delete(new LambdaQueryWrapper<Prize>()
                .eq(Prize::getActivityId, activityId));
        
        activityMapper.deleteById(activityId);
        log.info("删除活动成功: id={}", activityId);
    }

    @Override
    @Transactional
    public void updateActivityStatus(Long activityId, Integer status) {
        Activity activity = activityMapper.selectById(activityId);
        if (activity == null) {
            throw new BusinessException(ResultCode.ACTIVITY_NOT_EXIST);
        }
        
        activity.setStatus(status);
        activity.setUpdatedAt(LocalDateTime.now());
        activityMapper.updateById(activity);
        log.info("更新活动状态: id={}, status={}", activityId, status);
    }

    @Override
    @Transactional
    public PrizeResponse createPrize(PrizeRequest request) {
        Activity activity = activityMapper.selectById(request.getActivityId());
        if (activity == null) {
            throw new BusinessException(ResultCode.ACTIVITY_NOT_EXIST);
        }
        
        Prize prize = new Prize();
        prize.setActivityId(request.getActivityId());
        prize.setName(request.getName());
        prize.setImage(request.getImage());
        prize.setTotalCount(request.getTotalCount());
        prize.setRemainingCount(request.getTotalCount());
        prize.setProbability(request.getProbability());
        prize.setSortOrder(request.getSortOrder());
        prize.setStatus(request.getStatus());
        prize.setCreatedAt(LocalDateTime.now());
        prize.setUpdatedAt(LocalDateTime.now());
        
        prizeMapper.insert(prize);
        log.info("创建奖品成功: id={}, name={}, activityId={}", 
                prize.getId(), prize.getName(), prize.getActivityId());
        
        return convertToPrizeResponse(prize);
    }

    @Override
    @Transactional
    public PrizeResponse updatePrize(Long prizeId, PrizeRequest request) {
        Prize prize = prizeMapper.selectById(prizeId);
        if (prize == null) {
            throw new BusinessException(ResultCode.PRIZE_NOT_EXIST);
        }
        
        // 如果更换了活动，验证新活动存在
        if (!prize.getActivityId().equals(request.getActivityId())) {
            Activity activity = activityMapper.selectById(request.getActivityId());
            if (activity == null) {
                throw new BusinessException(ResultCode.ACTIVITY_NOT_EXIST);
            }
        }
        
        // 计算库存差值
        int stockDiff = request.getTotalCount() - prize.getTotalCount();
        int newRemaining = prize.getRemainingCount() + stockDiff;
        if (newRemaining < 0) {
            newRemaining = 0;
        }
        
        prize.setActivityId(request.getActivityId());
        prize.setName(request.getName());
        prize.setImage(request.getImage());
        prize.setTotalCount(request.getTotalCount());
        prize.setRemainingCount(newRemaining);
        prize.setProbability(request.getProbability());
        prize.setSortOrder(request.getSortOrder());
        prize.setStatus(request.getStatus());
        prize.setUpdatedAt(LocalDateTime.now());
        
        prizeMapper.updateById(prize);
        log.info("更新奖品成功: id={}", prizeId);
        
        return convertToPrizeResponse(prize);
    }

    @Override
    @Transactional
    public void deletePrize(Long prizeId) {
        Prize prize = prizeMapper.selectById(prizeId);
        if (prize == null) {
            throw new BusinessException(ResultCode.PRIZE_NOT_EXIST);
        }
        
        prizeMapper.deleteById(prizeId);
        log.info("删除奖品成功: id={}", prizeId);
    }

    @Override
    @Transactional
    public void updatePrizeStatus(Long prizeId, Integer status) {
        Prize prize = prizeMapper.selectById(prizeId);
        if (prize == null) {
            throw new BusinessException(ResultCode.PRIZE_NOT_EXIST);
        }
        
        prize.setStatus(status);
        prize.setUpdatedAt(LocalDateTime.now());
        prizeMapper.updateById(prize);
        log.info("更新奖品状态: id={}, status={}", prizeId, status);
    }

    @Override
    @Transactional
    public void updatePrizeStock(Long prizeId, Integer totalCount) {
        Prize prize = prizeMapper.selectById(prizeId);
        if (prize == null) {
            throw new BusinessException(ResultCode.PRIZE_NOT_EXIST);
        }
        
        int stockDiff = totalCount - prize.getTotalCount();
        int newRemaining = prize.getRemainingCount() + stockDiff;
        if (newRemaining < 0) {
            newRemaining = 0;
        }
        
        prize.setTotalCount(totalCount);
        prize.setRemainingCount(newRemaining);
        prize.setUpdatedAt(LocalDateTime.now());
        prizeMapper.updateById(prize);
        log.info("更新奖品库存: id={}, totalCount={}, remainingCount={}", 
                prizeId, totalCount, newRemaining);
    }

    // ========== 私有方法 ==========

    private void validateActivityTime(LocalDateTime startTime, LocalDateTime endTime) {
        if (startTime.isAfter(endTime)) {
            throw new BusinessException(ResultCode.VALIDATE_FAILED, "开始时间不能晚于结束时间");
        }
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
                .status(prize.getStatus())
                .build();
    }
}
