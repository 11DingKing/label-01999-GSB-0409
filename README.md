# 幸运抽奖系统

一个基于 Spring Boot + Vue 3 的在线抽奖平台，支持多活动管理、九宫格抽奖、奖品领取等功能。

## 项目结构

```
├── backend/                 # 后端服务 (Spring Boot)
├── frontend-user/           # 用户端前端 (Vue 3 + Element Plus)
├── docker-compose.yml       # Docker Compose 配置
└── docs/                    # 项目文档
```

## 技术栈

### 后端
- Java 17
- Spring Boot 3.x
- MyBatis Plus
- MySQL 8.0
- Redis 7
- JWT 认证

### 前端
- Vue 3
- Vite 5
- Element Plus
- Pinia
- Vue Router

## 快速开始

### 环境要求
- Docker 20.10+
- Docker Compose 2.0+

### 一键启动

```bash
# 克隆项目
git clone <repository-url>
cd lottery-system

# 启动所有服务
docker-compose up --build -d

# 查看服务状态
docker-compose ps

# 查看日志
docker-compose logs -f
```

### 访问地址

| 服务 | 地址 | 说明 |
|------|------|------|
| 用户端前端 | http://localhost:8081 | 用户抽奖界面 |
| 后端API | http://localhost:8088 | REST API |
| MySQL | localhost:3306 | 数据库 |
| Redis | localhost:6379 | 缓存 |

## 功能特性

### 用户端
- 用户注册/登录
- 活动列表浏览
- 九宫格抽奖
- 中奖记录查看
- 奖品领取
- 个人信息管理

### 系统特性
- JWT Token 认证
- 每日抽奖次数限制
- 奖品库存管理
- 中奖概率配置
- 实时中奖播报

## 开发指南

### 本地开发

#### 后端
```bash
cd backend
mvn spring-boot:run
```

#### 前端
```bash
cd frontend-user
npm install
npm run dev
```

### 构建镜像

```bash
# 构建所有服务
docker-compose build

# 单独构建某个服务
docker-compose build backend
docker-compose build frontend-user
```

### 验证跨平台镜像

```bash
# 验证镜像是否支持 ARM 架构
docker pull --platform linux/arm64 nginx:1.25-alpine
docker pull --platform linux/arm64 eclipse-temurin:17-jre
```

## 配置说明

### 环境变量

| 变量名 | 默认值 | 说明 |
|--------|--------|------|
| MYSQL_HOST | mysql | MySQL 主机 |
| MYSQL_PORT | 3306 | MySQL 端口 |
| MYSQL_DATABASE | lottery_db | 数据库名 |
| MYSQL_USER | root | 数据库用户 |
| MYSQL_PASSWORD | root123456 | 数据库密码 |
| REDIS_HOST | redis | Redis 主机 |
| REDIS_PORT | 6379 | Redis 端口 |
| JWT_SECRET | - | JWT 密钥 |

## 停止服务

```bash
# 停止所有服务
docker-compose down

# 停止并删除数据卷
docker-compose down -v
```

## License

MIT
