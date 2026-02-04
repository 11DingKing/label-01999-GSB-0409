package com.lottery.dto.response;

import lombok.Builder;
import lombok.Data;

/**
 * 用户信息响应
 */
@Data
@Builder
public class UserInfoResponse {

    private Long id;
    private String username;
    private String nickname;
    private String phone;
    private String avatar;
}
