package com.lottery.controller;

import com.lottery.common.Result;
import com.lottery.dto.request.PrizeRequest;
import com.lottery.dto.response.PrizeResponse;
import com.lottery.service.ActivityService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;

/**
 * 奖品管理控制器（管理端）
 */
@RestController
@RequestMapping("/api/admin/prize")
@RequiredArgsConstructor
public class AdminPrizeController {

    private final ActivityService activityService;

    /**
     * 获取活动的所有奖品（包含下线的）
     */
    @GetMapping("/activity/{activityId}")
    public Result<List<PrizeResponse>> getActivityPrizes(@PathVariable Long activityId) {
        return Result.success(activityService.getAllActivityPrizes(activityId));
    }

    /**
     * 创建奖品
     */
    @PostMapping
    public Result<PrizeResponse> createPrize(@Valid @RequestBody PrizeRequest request) {
        return Result.success(activityService.createPrize(request));
    }

    /**
     * 更新奖品
     */
    @PutMapping("/{id}")
    public Result<PrizeResponse> updatePrize(
            @PathVariable Long id,
            @Valid @RequestBody PrizeRequest request) {
        return Result.success(activityService.updatePrize(id, request));
    }

    /**
     * 删除奖品
     */
    @DeleteMapping("/{id}")
    public Result<Void> deletePrize(@PathVariable Long id) {
        activityService.deletePrize(id);
        return Result.success();
    }

    /**
     * 上线奖品
     */
    @PutMapping("/{id}/online")
    public Result<Void> onlinePrize(@PathVariable Long id) {
        activityService.updatePrizeStatus(id, 1);
        return Result.success();
    }

    /**
     * 下线奖品
     */
    @PutMapping("/{id}/offline")
    public Result<Void> offlinePrize(@PathVariable Long id) {
        activityService.updatePrizeStatus(id, 0);
        return Result.success();
    }

    /**
     * 更新奖品库存
     */
    @PutMapping("/{id}/stock")
    public Result<Void> updatePrizeStock(
            @PathVariable Long id,
            @RequestParam Integer totalCount) {
        activityService.updatePrizeStock(id, totalCount);
        return Result.success();
    }
}
