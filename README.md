# 幸运抽奖系统

## How to Run

```bash
# 使用Docker Compose一键启动
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
| frontend-user | 8080 | 用户端前端 |
| backend | 8088 | 后端API服务 |
| mysql | 3306 | MySQL数据库 |
| redis | 6379 | Redis缓存 |

访问地址：
- 用户端：http://localhost:8080

## 测试账号

| 用户名 | 密码 | 说明 |
|--------|------|------|
| testuser | 123456 | 测试用户 |

或直接注册新账号使用。

## 题目内容

使用vue3+springboot写一抽奖程序，满足真实产品形态的用户端架构。要求：
- 可运行验证：提供完整启动步骤
- 全功能覆盖：核心需求100%实现
- 模块化设计：Controller -> Service -> Mapper -> Entity 分层
- 健壮性：全局异常处理、日志记录、参数校验
- 美观度：视觉分层、布局对齐、交互反馈
- Docker规范：支持ARM和X86跨平台

## 项目介绍

基于 Vue3 + Spring Boot 3 的抽奖系统，包含完整的用户端功能。

### 技术栈

**后端：**
- Spring Boot 3.2.5
- MyBatis-Plus 3.5.5
- MySQL 8.0
- Redis 7
- JWT认证

**前端：**
- Vue 3.4
- Vite 5
- Pinia
- Element Plus
- SCSS

### 功能特性

- 用户注册/登录
- 活动列表展示
- 转盘抽奖
- 中奖记录查看
- 奖品领取
- 个人中心

### 项目结构

```
├── backend/                 # 后端服务
│   ├── src/main/java/      # Java源码
│   ├── src/main/resources/ # 配置文件和SQL
│   ├── Dockerfile
│   └── pom.xml
├── frontend-user/          # 用户端前端
│   ├── src/
│   ├── Dockerfile
│   └── package.json
├── docs/                   # 设计文档
├── docker-compose.yml
└── README.md
```
