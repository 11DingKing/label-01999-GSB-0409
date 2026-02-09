package com.lottery.service;

import com.lottery.dto.request.ActivityRequest;
import com.lottery.dto.request.PrizeRequest;
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
     * 获取所有活动列表（包含下线的）
     */
    List<ActivityResponse> getAllActivityList();

    /**
     * 获取活动详情
     */
    ActivityResponse getActivityDetail(Long activityId);

    /**
     * 获取活动奖品列表
     */
    List<PrizeResponse> getActivityPrizes(Long activityId);

    /**
     * 获取活动所有奖品列表（包含下线的）
     */
    List<PrizeResponse> getAllActivityPrizes(Long activityId);

    // ========== 管理接口 ==========

    /**
     * 创建活动
     */
    ActivityResponse createActivity(ActivityRequest request);

    /**
     * 更新活动
     */
    ActivityResponse updateActivity(Long activityId, ActivityRequest request);

    /**
     * 删除活动
     */
    void deleteActivity(Long activityId);

    /**
     * 上线/下线活动
     */
    void updateActivityStatus(Long activityId, Integer status);

    /**
     * 创建奖品
     */
    PrizeResponse createPrize(PrizeRequest request);

    /**
     * 更新奖品
     */
    PrizeResponse updatePrize(Long prizeId, PrizeRequest request);

    /**
     * 删除奖品
     */
    void deletePrize(Long prizeId);

    /**
     * 更新奖品状态
     */
    void updatePrizeStatus(Long prizeId, Integer status);

    /**
     * 更新奖品库存
     */
    void updatePrizeStock(Long prizeId, Integer totalCount);
}
