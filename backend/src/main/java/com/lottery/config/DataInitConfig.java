package com.lottery.config;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.lottery.entity.User;
import com.lottery.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

/**
 * 数据初始化配置
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class DataInitConfig implements CommandLineRunner {

    private final UserMapper userMapper;
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    @Override
    public void run(String... args) {
        // 检查测试用户是否存在，如果存在则更新密码
        User testUser = userMapper.selectOne(
                new LambdaQueryWrapper<User>().eq(User::getUsername, "testuser")
        );
        
        if (testUser != null) {
            // 更新为正确的BCrypt加密密码
            testUser.setPassword(passwordEncoder.encode("123456"));
            userMapper.updateById(testUser);
            log.info("测试用户密码已更新");
        } else {
            // 创建测试用户
            User user = new User();
            user.setUsername("testuser");
            user.setPassword(passwordEncoder.encode("123456"));
            user.setNickname("测试用户");
            user.setPhone("13800138000");
            user.setAvatar("https://api.dicebear.com/7.x/avataaars/svg?seed=test");
            user.setStatus(1);
            userMapper.insert(user);
            log.info("测试用户已创建");
        }
    }
}
