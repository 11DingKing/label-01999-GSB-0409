package com.lottery.service;

import com.lottery.dto.response.DrawCountResponse;
import com.lottery.dto.response.DrawResponse;

/**
 * 抽奖服务接口
 */
public interface DrawService {

    /**
     * 执行抽奖
     */
    DrawResponse draw(Long activityId);

    /**
     * 获取剩余抽奖次数
     */
    DrawCountResponse getRemainingDrawCount(Long activityId);
}
