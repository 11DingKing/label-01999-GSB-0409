package com.lottery.service;

import com.lottery.dto.request.LoginRequest;
import com.lottery.dto.request.RegisterRequest;
import com.lottery.dto.request.UpdateUserRequest;
import com.lottery.dto.response.LoginResponse;
import com.lottery.dto.response.UserInfoResponse;

/**
 * 用户服务接口
 */
public interface UserService {

    /**
     * 用户注册
     */
    LoginResponse register(RegisterRequest request);

    /**
     * 用户登录
     */
    LoginResponse login(LoginRequest request);

    /**
     * 获取当前用户信息
     */
    UserInfoResponse getCurrentUserInfo();

    /**
     * 更新用户信息
     */
    void updateUserInfo(UpdateUserRequest request);
}
