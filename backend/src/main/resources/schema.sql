-- 抽奖系统数据库初始化脚本
-- 设置字符集
SET NAMES utf8mb4;
SET CHARACTER SET utf8mb4;

-- 创建数据库
CREATE DATABASE IF NOT EXISTS lottery_db DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

USE lottery_db;

-- 用户表
DROP TABLE IF EXISTS `user`;
CREATE TABLE `user` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '用户ID',
    `username` VARCHAR(50) NOT NULL COMMENT '用户名',
    `password` VARCHAR(255) NOT NULL COMMENT '密码',
    `nickname` VARCHAR(50) DEFAULT NULL COMMENT '昵称',
    `phone` VARCHAR(20) DEFAULT NULL COMMENT '手机号',
    `avatar` VARCHAR(255) DEFAULT NULL COMMENT '头像URL',
    `role` VARCHAR(20) NOT NULL DEFAULT 'USER' COMMENT '角色：USER-普通用户，ADMIN-管理员',
    `status` TINYINT NOT NULL DEFAULT 1 COMMENT '状态：0-禁用，1-正常',
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_username` (`username`),
    KEY `idx_phone` (`phone`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户表';

-- 活动表
DROP TABLE IF EXISTS `activity`;
CREATE TABLE `activity` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '活动ID',
    `name` VARCHAR(100) NOT NULL COMMENT '活动名称',
    `description` TEXT COMMENT '活动描述',
    `cover_image` VARCHAR(255) DEFAULT NULL COMMENT '封面图片',
    `start_time` DATETIME NOT NULL COMMENT '开始时间',
    `end_time` DATETIME NOT NULL COMMENT '结束时间',
    `daily_limit` INT NOT NULL DEFAULT 3 COMMENT '每日抽奖次数限制',
    `total_limit` INT NOT NULL DEFAULT 0 COMMENT '总抽奖次数限制，0表示不限制',
    `status` TINYINT NOT NULL DEFAULT 1 COMMENT '状态：0-下线，1-上线',
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    KEY `idx_status` (`status`),
    KEY `idx_time` (`start_time`, `end_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='活动表';

-- 奖品表
DROP TABLE IF EXISTS `prize`;
CREATE TABLE `prize` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '奖品ID',
    `activity_id` BIGINT NOT NULL COMMENT '活动ID',
    `name` VARCHAR(100) NOT NULL COMMENT '奖品名称',
    `image` VARCHAR(255) DEFAULT NULL COMMENT '奖品图片',
    `total_count` INT NOT NULL DEFAULT 0 COMMENT '奖品总数',
    `remaining_count` INT NOT NULL DEFAULT 0 COMMENT '剩余数量',
    `probability` DECIMAL(10, 6) NOT NULL DEFAULT 0 COMMENT '中奖概率（0-1）',
    `sort_order` INT NOT NULL DEFAULT 0 COMMENT '排序顺序',
    `status` TINYINT NOT NULL DEFAULT 1 COMMENT '状态：0-禁用，1-启用',
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    KEY `idx_activity_id` (`activity_id`),
    KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='奖品表';

-- 中奖记录表
DROP TABLE IF EXISTS `prize_record`;
CREATE TABLE `prize_record` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '记录ID',
    `user_id` BIGINT NOT NULL COMMENT '用户ID',
    `activity_id` BIGINT NOT NULL COMMENT '活动ID',
    `prize_id` BIGINT DEFAULT NULL COMMENT '奖品ID，NULL表示未中奖',
    `prize_name` VARCHAR(100) DEFAULT NULL COMMENT '奖品名称',
    `status` TINYINT NOT NULL DEFAULT 0 COMMENT '领取状态：0-未领取，1-已领取',
    `draw_time` DATETIME NOT NULL COMMENT '抽奖时间',
    `receive_time` DATETIME DEFAULT NULL COMMENT '领取时间',
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    KEY `idx_user_id` (`user_id`),
    KEY `idx_activity_id` (`activity_id`),
    KEY `idx_prize_id` (`prize_id`),
    KEY `idx_draw_time` (`draw_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='中奖记录表';

-- 用户抽奖次数表
DROP TABLE IF EXISTS `user_draw_count`;
CREATE TABLE `user_draw_count` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT 'ID',
    `user_id` BIGINT NOT NULL COMMENT '用户ID',
    `activity_id` BIGINT NOT NULL COMMENT '活动ID',
    `draw_date` DATE NOT NULL COMMENT '抽奖日期',
    `daily_count` INT NOT NULL DEFAULT 0 COMMENT '当日已抽次数',
    `total_count` INT NOT NULL DEFAULT 0 COMMENT '总已抽次数',
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_user_activity_date` (`user_id`, `activity_id`, `draw_date`),
    KEY `idx_activity_id` (`activity_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户抽奖次数表';

-- 插入测试数据
-- 测试用户会在应用启动时自动创建/更新

-- 测试活动（使用本地图片）
INSERT INTO `activity` (`name`, `description`, `cover_image`, `start_time`, `end_time`, `daily_limit`, `total_limit`) VALUES
('新春抽奖活动', '新春佳节，好礼相送！参与抽奖赢取丰厚奖品！', '/images/activities/spring.svg', '2025-01-01 00:00:00', '2026-12-31 23:59:59', 5, 0),
('周年庆典抽奖', '感恩回馈，周年庆典大抽奖！', '/images/activities/anniversary.svg', '2025-01-01 00:00:00', '2026-12-31 23:59:59', 3, 100);

-- 测试奖品（使用本地图片）
INSERT INTO `prize` (`activity_id`, `name`, `image`, `total_count`, `remaining_count`, `probability`, `sort_order`) VALUES
(1, 'iPhone 15 Pro', '/images/prizes/iphone.svg', 5, 5, 0.01, 1),
(1, 'AirPods Pro', '/images/prizes/airpods.svg', 20, 20, 0.05, 2),
(1, '100元红包', '/images/prizes/redpack-100.svg', 100, 100, 0.10, 3),
(1, '50元红包', '/images/prizes/redpack-50.svg', 200, 200, 0.15, 4),
(1, '10元红包', '/images/prizes/redpack-10.svg', 500, 500, 0.20, 5),
(1, '谢谢参与', '/images/prizes/thanks.svg', 999999, 999999, 0.49, 6),
(2, 'MacBook Pro', '/images/prizes/macbook.svg', 2, 2, 0.005, 1),
(2, 'iPad Air', '/images/prizes/ipad.svg', 10, 10, 0.02, 2),
(2, '200元购物卡', '/images/prizes/giftcard-200.svg', 50, 50, 0.08, 3),
(2, '50元购物卡', '/images/prizes/giftcard-50.svg', 100, 100, 0.15, 4),
(2, '谢谢参与', '/images/prizes/thanks.svg', 999999, 999999, 0.745, 5);
