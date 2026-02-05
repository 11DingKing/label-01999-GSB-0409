# 幸运抽奖系统

## How to Run

```bash
# 克隆项目
git clone <repository-url>
cd lottery-system

# 一键启动所有服务
docker-compose up --build -d

# 查看服务状态
docker-compose ps

# 查看日志
docker-compose logs -f

# 停止服务
docker-compose down
```

## Services

| 服务 | 端口 | 说明 |
|------|------|------|
| frontend-user | 8081 | 用户端前端 |
| backend | 8088 | 后端 API 服务 |
| mysql | 3306 | MySQL 数据库 |
| redis | 6379 | Redis 缓存 |

## 测试账号

| 用户名 | 密码 | 说明 |
|--------|------|------|
| testuser | 123456 | 测试用户账号 |

## 题目内容

使用vue3+springboot写一抽奖程序

- 个人信息管理

## 项目介绍

基于 Spring Boot + Vue 3 的在线抽奖平台。

### 技术栈

- 后端：Java 17、Spring Boot 3.x、MyBatis Plus、MySQL 8.0、Redis 7、JWT
- 前端：Vue 3、Vite 5、Element Plus、Pinia

### 项目结构

```
├── backend/           # 后端服务 (Spring Boot)
├── frontend-user/     # 用户端前端 (Vue 3)
├── docs/              # 项目文档
├── docker-compose.yml
└── README.md
```

### 核心功能

- 多活动管理
- 九宫格抽奖动画
- 每日抽奖次数限制
- 奖品库存管理
- 中奖概率配置
- 实时中奖播报
