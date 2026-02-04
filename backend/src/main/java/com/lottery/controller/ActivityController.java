package com.lottery.controller;

import com.lottery.common.Result;
import com.lottery.dto.response.ActivityResponse;
import com.lottery.dto.response.PrizeResponse;
import com.lottery.service.ActivityService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 活动控制器
 */
@RestController
@RequestMapping("/api/activity")
@RequiredArgsConstructor
public class ActivityController {

    private final ActivityService activityService;

    /**
     * 获取活动列表
     */
    @GetMapping("/list")
    public Result<List<ActivityResponse>> getActivityList() {
        return Result.success(activityService.getActivityList());
    }

    /**
     * 获取活动详情
     */
    @GetMapping("/{id}")
    public Result<ActivityResponse> getActivityDetail(@PathVariable Long id) {
        return Result.success(activityService.getActivityDetail(id));
    }

    /**
     * 获取活动奖品列表
     */
    @GetMapping("/{id}/prizes")
    public Result<List<PrizeResponse>> getActivityPrizes(@PathVariable Long id) {
        return Result.success(activityService.getActivityPrizes(id));
    }
}
