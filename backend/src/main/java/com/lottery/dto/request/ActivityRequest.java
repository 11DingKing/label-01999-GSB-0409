package com.lottery.dto.request;

import lombok.Data;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;

/**
 * 活动创建/更新请求
 */
@Data
public class ActivityRequest {

    @NotBlank(message = "活动名称不能为空")
    private String name;

    private String description;

    private String coverImage;

    @NotNull(message = "开始时间不能为空")
    private LocalDateTime startTime;

    @NotNull(message = "结束时间不能为空")
    private LocalDateTime endTime;

    @Min(value = 1, message = "每日限制至少为1")
    private Integer dailyLimit = 3;

    @Min(value = 0, message = "总限制不能为负数")
    private Integer totalLimit = 0;

    private Integer status = 1;

    @AssertTrue(message = "总限制不为0时不能小于每日限制")
    public boolean isTotalLimitValid() {
        if (totalLimit == null || totalLimit == 0) {
            return true;
        }
        return dailyLimit == null || totalLimit >= dailyLimit;
    }
}
