package com.lottery.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 中奖记录实体
 */
@Data
@TableName("prize_record")
public class PrizeRecord {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long userId;

    private Long activityId;

    private Long prizeId;

    private String prizeName;

    private Integer status;

    private LocalDateTime drawTime;

    private LocalDateTime receiveTime;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
}
