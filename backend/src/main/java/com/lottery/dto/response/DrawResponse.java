package com.lottery.dto.response;

import lombok.Builder;
import lombok.Data;

/**
 * 抽奖响应
 */
@Data
@Builder
public class DrawResponse {

    private Boolean won;
    private Long prizeId;
    private String prizeName;
    private String prizeImage;
    private Long recordId;
}
