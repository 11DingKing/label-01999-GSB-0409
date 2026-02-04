package com.lottery.dto.response;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 活动响应
 */
@Data
@Builder
public class ActivityResponse {

    private Long id;
    private String name;
    private String description;
    private String coverImage;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private Integer dailyLimit;
    private Integer totalLimit;
    private Integer status;
    private String statusText;
}
