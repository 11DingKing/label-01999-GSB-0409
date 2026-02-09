package com.lottery.controller;

import com.lottery.common.Result;
import com.lottery.dto.response.PrizeRecordResponse;
import com.lottery.service.PrizeRecordService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 中奖记录管理控制器（管理端）
 */
@RestController
@RequestMapping("/api/admin/record")
@RequiredArgsConstructor
public class AdminRecordController {

    private final PrizeRecordService prizeRecordService;

    /**
     * 获取所有中奖记录
     */
    @GetMapping("/list")
    public Result<List<PrizeRecordResponse>> getAllRecords(
            @RequestParam(required = false) Long activityId) {
        return Result.success(prizeRecordService.getAllRecords(activityId));
    }

    /**
     * 获取中奖统计
     */
    @GetMapping("/stats")
    public Result<Map<String, Object>> getStats(
            @RequestParam(required = false) Long activityId) {
        return Result.success(prizeRecordService.getActivityStats(activityId));
    }
}
