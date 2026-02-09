package com.lottery.dto.response;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 奖品响应
 */
@Data
@Builder
public class PrizeResponse {

    private Long id;
    private String name;
    private String image;
    private Integer totalCount;
    private Integer remainingCount;
    private BigDecimal probability;
    private Integer sortOrder;
    private Integer status;
}
