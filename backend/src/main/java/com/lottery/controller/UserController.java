package com.lottery.controller;

import com.lottery.common.Result;
import com.lottery.dto.request.LoginRequest;
import com.lottery.dto.request.RegisterRequest;
import com.lottery.dto.request.UpdateUserRequest;
import com.lottery.dto.response.LoginResponse;
import com.lottery.dto.response.UserInfoResponse;
import com.lottery.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 用户控制器
 */
@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    /**
     * 用户注册
     */
    @PostMapping("/register")
    public Result<LoginResponse> register(@Valid @RequestBody RegisterRequest request) {
        return Result.success(userService.register(request));
    }

    /**
     * 用户登录
     */
    @PostMapping("/login")
    public Result<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        return Result.success(userService.login(request));
    }

    /**
     * 获取当前用户信息
     */
    @GetMapping("/info")
    public Result<UserInfoResponse> getUserInfo() {
        return Result.success(userService.getCurrentUserInfo());
    }

    /**
     * 更新用户信息
     */
    @PutMapping("/update")
    public Result<Void> updateUserInfo(@Valid @RequestBody UpdateUserRequest request) {
        userService.updateUserInfo(request);
        return Result.success();
    }
}
