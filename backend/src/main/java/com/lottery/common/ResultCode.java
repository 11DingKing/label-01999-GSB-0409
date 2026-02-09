package com.lottery.common;

import lombok.Getter;

/**
 * 响应状态码枚举
 */
@Getter
public enum ResultCode {

    SUCCESS(200, "操作成功"),
    FAILED(500, "操作失败"),
    VALIDATE_FAILED(400, "参数校验失败"),
    UNAUTHORIZED(401, "未登录或token已过期"),
    FORBIDDEN(403, "没有相关权限"),
    NOT_FOUND(404, "资源不存在"),

    // 业务错误码
    USER_NOT_EXIST(1001, "用户不存在"),
    USER_PASSWORD_ERROR(1002, "密码错误"),
    USER_DISABLED(1003, "用户已被禁用"),
    USER_ALREADY_EXIST(1004, "用户名已存在"),

    ACTIVITY_NOT_EXIST(2001, "活动不存在"),
    ACTIVITY_NOT_START(2002, "活动未开始"),
    ACTIVITY_ENDED(2003, "活动已结束"),
    ACTIVITY_OFFLINE(2004, "活动已下线"),

    DRAW_LIMIT_EXCEEDED(3001, "今日抽奖次数已用完"),
    DRAW_TOTAL_LIMIT_EXCEEDED(3002, "抽奖总次数已用完"),
    PRIZE_STOCK_EMPTY(3003, "奖品库存不足"),

    RECORD_NOT_EXIST(4001, "记录不存在"),
    RECORD_ALREADY_RECEIVED(4002, "奖品已领取"),

    PRIZE_NOT_EXIST(5001, "奖品不存在");

    private final Integer code;
    private final String message;

    ResultCode(Integer code, String message) {
        this.code = code;
        this.message = message;
    }
}
