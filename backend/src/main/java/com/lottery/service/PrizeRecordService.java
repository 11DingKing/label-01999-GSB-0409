package com.lottery.service;

import com.lottery.dto.response.LatestWinnerResponse;
import com.lottery.dto.response.PrizeRecordResponse;

import java.util.List;

/**
 * 中奖记录服务接口
 */
public interface PrizeRecordService {

    /**
     * 获取我的中奖记录
     */
    List<PrizeRecordResponse> getMyRecords();

    /**
     * 领取奖品
     */
    void receivePrize(Long recordId);

    /**
     * 获取最新中奖滚动列表
     */
    List<LatestWinnerResponse> getLatestWinners();
}
