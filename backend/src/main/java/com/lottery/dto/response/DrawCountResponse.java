package com.lottery.dto.response;

import lombok.Builder;
import lombok.Data;

/**
 * 抽奖次数响应
 */
@Data
@Builder
public class DrawCountResponse {

    private Integer dailyLimit;
    private Integer dailyUsed;
    private Integer dailyRemaining;
    private Integer totalLimit;
    private Integer totalUsed;
    private Integer totalRemaining;
}
