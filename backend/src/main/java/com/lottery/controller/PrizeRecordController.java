package com.lottery.controller;

import com.lottery.common.Result;
import com.lottery.dto.response.LatestWinnerResponse;
import com.lottery.dto.response.PrizeRecordResponse;
import com.lottery.service.PrizeRecordService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 中奖记录控制器
 */
@RestController
@RequestMapping("/api/record")
@RequiredArgsConstructor
public class PrizeRecordController {

    private final PrizeRecordService prizeRecordService;

    /**
     * 获取我的中奖记录
     */
    @GetMapping("/my")
    public Result<List<PrizeRecordResponse>> getMyRecords() {
        return Result.success(prizeRecordService.getMyRecords());
    }

    /**
     * 领取奖品
     */
    @PostMapping("/{id}/receive")
    public Result<Void> receivePrize(@PathVariable Long id) {
        prizeRecordService.receivePrize(id);
        return Result.success();
    }

    /**
     * 获取最新中奖滚动列表
     */
    @GetMapping("/latest")
    public Result<List<LatestWinnerResponse>> getLatestWinners() {
        return Result.success(prizeRecordService.getLatestWinners());
    }
}
