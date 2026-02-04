package com.lottery.controller;

import com.lottery.common.Result;
import com.lottery.dto.response.DrawCountResponse;
import com.lottery.dto.response.DrawResponse;
import com.lottery.service.DrawService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 抽奖控制器
 */
@RestController
@RequestMapping("/api/draw")
@RequiredArgsConstructor
public class DrawController {

    private final DrawService drawService;

    /**
     * 执行抽奖
     */
    @PostMapping("/{activityId}")
    public Result<DrawResponse> draw(@PathVariable Long activityId) {
        return Result.success(drawService.draw(activityId));
    }

    /**
     * 获取剩余抽奖次数
     */
    @GetMapping("/{activityId}/remaining")
    public Result<DrawCountResponse> getRemainingDrawCount(@PathVariable Long activityId) {
        return Result.success(drawService.getRemainingDrawCount(activityId));
    }
}
