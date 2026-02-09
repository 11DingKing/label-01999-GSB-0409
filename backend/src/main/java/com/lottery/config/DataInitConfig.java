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
        initTestUser();
        initAdminUser();
    }

    private void initTestUser() {
        User testUser = userMapper.selectOne(
                new LambdaQueryWrapper<User>().eq(User::getUsername, "testuser")
        );
        
        if (testUser != null) {
            testUser.setPassword(passwordEncoder.encode("123456"));
            testUser.setRole("USER");
            userMapper.updateById(testUser);
            log.info("测试用户密码已更新");
        } else {
            User user = new User();
            user.setUsername("testuser");
            user.setPassword(passwordEncoder.encode("123456"));
            user.setNickname("测试用户");
            user.setPhone("13800138000");
            user.setAvatar("https://api.dicebear.com/7.x/avataaars/svg?seed=test");
            user.setRole("USER");
            user.setStatus(1);
            userMapper.insert(user);
            log.info("测试用户已创建");
        }
    }

    private void initAdminUser() {
        User adminUser = userMapper.selectOne(
                new LambdaQueryWrapper<User>().eq(User::getUsername, "admin")
        );
        
        if (adminUser != null) {
            adminUser.setPassword(passwordEncoder.encode("admin123"));
            adminUser.setRole("ADMIN");
            userMapper.updateById(adminUser);
            log.info("管理员账户密码已更新");
        } else {
            User user = new User();
            user.setUsername("admin");
            user.setPassword(passwordEncoder.encode("admin123"));
            user.setNickname("系统管理员");
            user.setPhone("13900139000");
            user.setAvatar("https://api.dicebear.com/7.x/avataaars/svg?seed=admin");
            user.setRole("ADMIN");
            user.setStatus(1);
            userMapper.insert(user);
            log.info("管理员账户已创建: username=admin, password=admin123");
        }
    }
}
