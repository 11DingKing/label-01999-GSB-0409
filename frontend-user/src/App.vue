<template>
  <div class="app-container">
    <!-- 登录/注册页面不显示侧边栏 -->
    <template v-if="showSidebar">
      <div class="desktop-layout">
        <!-- 侧边栏 -->
        <aside class="desktop-sidebar">
          <div class="sidebar-header">
            <div class="logo-wrapper">
              <span class="logo-icon">🎰</span>
              <span class="logo-text">幸运抽奖</span>
            </div>
          </div>
          
          <nav class="sidebar-nav">
            <router-link to="/home" class="nav-item" :class="{ active: currentRoute === '/home' }">
              <el-icon><HomeFilled /></el-icon>
              <span>活动大厅</span>
            </router-link>
            <router-link to="/records" class="nav-item" :class="{ active: currentRoute === '/records' }">
              <el-icon><Trophy /></el-icon>
              <span>我的奖品</span>
            </router-link>
            <router-link to="/profile" class="nav-item" :class="{ active: currentRoute === '/profile' }">
              <el-icon><User /></el-icon>
              <span>个人中心</span>
            </router-link>
          </nav>
          
          <div class="sidebar-footer" v-if="userStore.isLoggedIn">
            <div class="user-card">
              <el-avatar :src="userStore.userInfo?.avatar" :size="40" />
              <div class="user-info">
                <span class="user-name">{{ userStore.userInfo?.nickname || userStore.userInfo?.username }}</span>
                <span class="user-id">@{{ userStore.userInfo?.username }}</span>
              </div>
            </div>
            <el-button text class="logout-btn" @click="handleLogout">
              <el-icon><SwitchButton /></el-icon>
            </el-button>
          </div>
        </aside>
        
        <!-- 主内容区 -->
        <main class="desktop-main">
          <router-view />
        </main>
      </div>
    </template>
    
    <!-- 登录/注册页面 -->
    <template v-else>
      <router-view />
    </template>
  </div>
</template>

<script setup>
import { computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { useUserStore } from '@/stores/user'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()

const currentRoute = computed(() => route.path)

const showSidebar = computed(() => {
  const noSidebarRoutes = ['/login', '/register']
  return !noSidebarRoutes.includes(route.path)
})

function handleLogout() {
  ElMessageBox.confirm('确定要退出登录吗？', '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning',
  }).then(() => {
    userStore.logout()
    router.push('/login')
    ElMessage.success('已退出登录')
  }).catch(() => {})
}
</script>

<style lang="scss" scoped>
.app-container {
  min-height: 100vh;
}

.desktop-sidebar {
  background: linear-gradient(180deg, #1e1b4b 0%, #312e81 100%);
  color: #fff;
  border-right: none;
}

.sidebar-header {
  padding: 24px 20px;
  border-bottom: 1px solid rgba(255, 255, 255, 0.1);
}

.logo-wrapper {
  display: flex;
  align-items: center;
  gap: 12px;
  
  .logo-icon {
    font-size: 32px;
  }
  
  .logo-text {
    font-size: 20px;
    font-weight: 700;
    background: linear-gradient(135deg, #fbbf24 0%, #f59e0b 100%);
    -webkit-background-clip: text;
    -webkit-text-fill-color: transparent;
    background-clip: text;
  }
}

.sidebar-nav {
  flex: 1;
  padding: 16px 12px;
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.nav-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 14px 16px;
  border-radius: 10px;
  color: rgba(255, 255, 255, 0.7);
  font-size: 15px;
  font-weight: 500;
  transition: all 0.2s ease;
  text-decoration: none;
  
  .el-icon {
    font-size: 20px;
  }
  
  &:hover {
    background: rgba(255, 255, 255, 0.1);
    color: #fff;
  }
  
  &.active {
    background: linear-gradient(135deg, #6366f1 0%, #8b5cf6 100%);
    color: #fff;
    box-shadow: 0 4px 12px rgba(99, 102, 241, 0.4);
  }
}

.sidebar-footer {
  padding: 16px;
  border-top: 1px solid rgba(255, 255, 255, 0.1);
  display: flex;
  align-items: center;
  gap: 12px;
}

.user-card {
  flex: 1;
  display: flex;
  align-items: center;
  gap: 12px;
  min-width: 0;
}

.user-info {
  flex: 1;
  min-width: 0;
  display: flex;
  flex-direction: column;
  
  .user-name {
    font-size: 14px;
    font-weight: 600;
    color: #fff;
    overflow: hidden;
    text-overflow: ellipsis;
    white-space: nowrap;
  }
  
  .user-id {
    font-size: 12px;
    color: rgba(255, 255, 255, 0.5);
    overflow: hidden;
    text-overflow: ellipsis;
    white-space: nowrap;
  }
}

.logout-btn {
  color: rgba(255, 255, 255, 0.6);
  padding: 8px;
  
  &:hover {
    color: #ef4444;
    background: rgba(239, 68, 68, 0.1);
  }
  
  .el-icon {
    font-size: 20px;
  }
}

.desktop-main {
  background: #f8fafc;
}
</style>
