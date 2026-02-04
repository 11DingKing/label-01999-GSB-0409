package com.lottery.dto.response;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 最新中奖者响应
 */
@Data
@Builder
public class LatestWinnerResponse {

    private String nickname;
    private String avatar;
    private String prizeName;
    private LocalDateTime drawTime;
}
