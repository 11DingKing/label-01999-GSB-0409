package com.lottery.service;

import com.lottery.dto.response.ActivityResponse;
import com.lottery.dto.response.PrizeResponse;

import java.util.List;

/**
 * 活动服务接口
 */
public interface ActivityService {

    /**
     * 获取活动列表
     */
    List<ActivityResponse> getActivityList();

    /**
     * 获取活动详情
     */
    ActivityResponse getActivityDetail(Long activityId);

    /**
     * 获取活动奖品列表
     */
    List<PrizeResponse> getActivityPrizes(Long activityId);
}
