package com.lottery.dto.response;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 中奖记录响应
 */
@Data
@Builder
public class PrizeRecordResponse {

    private Long id;
    private Long userId;
    private String username;
    private String nickname;
    private Long prizeId;
    private String prizeName;
    private String prizeImage;
    private Long activityId;
    private String activityName;
    private Integer status;
    private String statusText;
    private LocalDateTime drawTime;
    private LocalDateTime receiveTime;
}
