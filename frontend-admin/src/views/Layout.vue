<template>
  <el-container class="admin-layout">
    <!-- 侧边栏 -->
    <el-aside width="240px" class="sidebar">
      <div class="logo">
        <div class="logo-icon">🎰</div>
        <span class="logo-text">抽奖管理系统</span>
      </div>
      
      <el-menu
        :default-active="route.path"
        router
        class="sidebar-menu"
      >
        <el-menu-item index="/activity">
          <el-icon><Tickets /></el-icon>
          <span>活动管理</span>
        </el-menu-item>
        <el-menu-item index="/records">
          <el-icon><Trophy /></el-icon>
          <span>中奖记录</span>
        </el-menu-item>
      </el-menu>
      
      <div class="sidebar-footer">
        <div class="version">v1.0.0</div>
      </div>
    </el-aside>
    
    <!-- 主内容区 -->
    <el-container class="main-container">
      <!-- 顶部导航 -->
      <el-header class="header">
        <div class="header-left">
          <el-breadcrumb separator="/">
            <el-breadcrumb-item :to="{ path: '/' }">首页</el-breadcrumb-item>
            <el-breadcrumb-item v-if="route.meta.title">{{ route.meta.title }}</el-breadcrumb-item>
          </el-breadcrumb>
        </div>
        
        <div class="header-right">
          <el-dropdown trigger="click" @command="handleCommand">
            <div class="user-info">
              <el-avatar :size="36" class="user-avatar">
                {{ userStore.userInfo?.nickname?.charAt(0) || 'A' }}
              </el-avatar>
              <span class="user-name">{{ userStore.userInfo?.nickname || '管理员' }}</span>
              <el-icon><ArrowDown /></el-icon>
            </div>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item command="logout">
                  <el-icon><SwitchButton /></el-icon>
                  退出登录
                </el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>
      </el-header>
      
      <!-- 内容区 -->
      <el-main class="main-content">
        <router-view v-slot="{ Component }">
          <transition name="fade" mode="out-in">
            <component :is="Component" />
          </transition>
        </router-view>
      </el-main>
    </el-container>
  </el-container>
</template>

<script setup>
import { useRoute, useRouter } from 'vue-router'
import { useUserStore } from '../stores/user'
import { Tickets, ArrowDown, SwitchButton, Trophy } from '@element-plus/icons-vue'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()

function handleCommand(command) {
  if (command === 'logout') {
    userStore.logout()
    router.push('/login')
  }
}
</script>

<style scoped lang="scss">
.admin-layout {
  min-height: 100vh;
}

.sidebar {
  background: linear-gradient(180deg, #1e293b 0%, #0f172a 100%);
  display: flex;
  flex-direction: column;
  box-shadow: 2px 0 8px rgba(0, 0, 0, 0.15);
}

.logo {
  height: 64px;
  display: flex;
  align-items: center;
  padding: 0 20px;
  gap: 12px;
  border-bottom: 1px solid rgba(255, 255, 255, 0.1);
  
  .logo-icon { font-size: 28px; }
  
  .logo-text {
    font-size: 16px;
    font-weight: 600;
    color: #fff;
    white-space: nowrap;
  }
}

.sidebar-menu {
  flex: 1;
  border: none;
  background: transparent;
  padding: 12px 0;
  
  :deep(.el-menu-item) {
    height: 48px;
    line-height: 48px;
    margin: 4px 12px;
    border-radius: 8px;
    color: #94a3b8;
    transition: all 0.2s;
    
    &:hover {
      background: rgba(255, 255, 255, 0.08);
      color: #fff;
    }
    
    &.is-active {
      background: var(--bg-gradient);
      color: #fff;
      .el-icon { color: #fff; }
    }
    
    .el-icon {
      margin-right: 8px;
      font-size: 18px;
    }
  }
}

.sidebar-footer {
  padding: 16px;
  border-top: 1px solid rgba(255, 255, 255, 0.1);
  
  .version {
    text-align: center;
    color: #64748b;
    font-size: 12px;
  }
}

.main-container {
  background: var(--bg-color);
}

.header {
  height: 64px;
  background: var(--card-bg);
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 24px;
  box-shadow: var(--shadow-sm);
  z-index: 10;
}

.header-left {
  :deep(.el-breadcrumb__item) {
    .el-breadcrumb__inner {
      color: var(--text-secondary);
      font-weight: 500;
      
      &.is-link:hover { color: var(--primary-color); }
    }
    
    &:last-child .el-breadcrumb__inner { color: var(--text-primary); }
  }
}

.header-right {
  .user-info {
    display: flex;
    align-items: center;
    gap: 8px;
    cursor: pointer;
    padding: 6px 12px;
    border-radius: 8px;
    transition: background 0.2s;
    
    &:hover { background: #f1f5f9; }
  }
  
  .user-avatar {
    background: var(--bg-gradient);
    color: #fff;
    font-weight: 600;
  }
  
  .user-name {
    color: var(--text-primary);
    font-weight: 500;
  }
}

.main-content {
  padding: 0;
  background: var(--bg-color);
  min-height: calc(100vh - 64px);
}

.fade-enter-active,
.fade-leave-active {
  transition: opacity 0.2s ease;
}

.fade-enter-from,
.fade-leave-to {
  opacity: 0;
}
</style>
