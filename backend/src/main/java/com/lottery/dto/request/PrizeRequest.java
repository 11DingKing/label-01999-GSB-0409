package com.lottery.dto.request;

import lombok.Data;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

/**
 * 奖品创建/更新请求
 */
@Data
public class PrizeRequest {

    @NotNull(message = "活动ID不能为空")
    private Long activityId;

    @NotBlank(message = "奖品名称不能为空")
    private String name;

    private String image;

    @NotNull(message = "奖品总数不能为空")
    @Min(value = 0, message = "奖品总数不能小于0")
    private Integer totalCount;

    @NotNull(message = "中奖概率不能为空")
    @DecimalMin(value = "0", message = "概率不能小于0")
    @DecimalMax(value = "100", message = "概率不能大于100")
    private BigDecimal probability;

    private Integer sortOrder = 0;

    private Integer status = 1;
}
