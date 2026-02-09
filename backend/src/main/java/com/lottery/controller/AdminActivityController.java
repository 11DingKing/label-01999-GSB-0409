package com.lottery.controller;

import com.lottery.common.Result;
import com.lottery.dto.request.ActivityRequest;
import com.lottery.dto.response.ActivityResponse;
import com.lottery.service.ActivityService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;

/**
 * 活动管理控制器（管理端）
 */
@RestController
@RequestMapping("/api/admin/activity")
@RequiredArgsConstructor
public class AdminActivityController {

    private final ActivityService activityService;

    /**
     * 获取所有活动列表（包含下线的）
     */
    @GetMapping("/list")
    public Result<List<ActivityResponse>> getAllActivityList() {
        return Result.success(activityService.getAllActivityList());
    }

    /**
     * 获取活动详情
     */
    @GetMapping("/{id}")
    public Result<ActivityResponse> getActivityDetail(@PathVariable Long id) {
        return Result.success(activityService.getActivityDetail(id));
    }

    /**
     * 创建活动
     */
    @PostMapping
    public Result<ActivityResponse> createActivity(@Valid @RequestBody ActivityRequest request) {
        return Result.success(activityService.createActivity(request));
    }

    /**
     * 更新活动
     */
    @PutMapping("/{id}")
    public Result<ActivityResponse> updateActivity(
            @PathVariable Long id,
            @Valid @RequestBody ActivityRequest request) {
        return Result.success(activityService.updateActivity(id, request));
    }

    /**
     * 删除活动
     */
    @DeleteMapping("/{id}")
    public Result<Void> deleteActivity(@PathVariable Long id) {
        activityService.deleteActivity(id);
        return Result.success();
    }

    /**
     * 上线活动
     */
    @PutMapping("/{id}/online")
    public Result<Void> onlineActivity(@PathVariable Long id) {
        activityService.updateActivityStatus(id, 1);
        return Result.success();
    }

    /**
     * 下线活动
     */
    @PutMapping("/{id}/offline")
    public Result<Void> offlineActivity(@PathVariable Long id) {
        activityService.updateActivityStatus(id, 0);
        return Result.success();
    }
}
