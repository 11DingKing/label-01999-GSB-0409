# 幸运抽奖系统

## How to Run

```bash
# 一键启动所有服务
docker-compose up --build -d

# 查看服务状态
docker-compose ps

# 停止服务
docker-compose down
```

## Services

| 服务 | 端口 | 说明 |
|------|------|------|
| frontend-user | 8081 | 用户端前端 |
| frontend-admin | 8082 | 管理后台前端 |
| backend | 8088 | 后端 API 服务 |
| mysql | 3306 | MySQL 数据库 |
| redis | 6379 | Redis 缓存 |

## 测试账号

| 用户名 | 密码 | 角色 |
|--------|------|------|
| testuser | 123456 | 普通用户 |
| admin | admin123 | 管理员 |

## 题目内容

使用vue3+springboot写一抽奖程序

## 项目介绍

基于 Spring Boot + Vue 3 的在线抽奖平台。

### 技术栈

- 后端：Java 17、Spring Boot 3.x、MyBatis Plus、MySQL 8.0、Redis 7、JWT
- 前端：Vue 3、Vite 5、Element Plus、Pinia

### 项目结构

```
├── backend/           # 后端服务 (Spring Boot)
├── frontend-user/     # 用户端前端 (Vue 3)
├── frontend-admin/    # 管理后台前端 (Vue 3)
└── docker-compose.yml
```

### 核心功能

- 用户端：注册登录、活动列表、九宫格抽奖、中奖记录、奖品领取
- 管理端：活动CRUD、奖品CRUD、概率配置、库存管理、图片上传
