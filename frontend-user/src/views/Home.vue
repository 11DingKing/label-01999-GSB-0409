<template>
  <div class="home-page">
    <div class="page-container">
      <!-- 顶部导航 -->
      <div class="top-nav">
        <div class="nav-left">
          <span class="logo">🎰</span>
          <span class="title">幸运抽奖</span>
        </div>
        <div class="nav-right" v-if="userStore.isLoggedIn" @click="showUserMenu = true">
          <el-avatar :src="userStore.userInfo?.avatar" :size="32" class="avatar-btn" />
        </div>
        <div class="nav-right" v-else>
          <el-button type="primary" size="small" @click="router.push('/login')">
            登录
          </el-button>
        </div>
      </div>

      <!-- 用户菜单弹窗 -->
      <el-drawer
        v-model="showUserMenu"
        direction="rtl"
        size="70%"
        :with-header="false"
        class="user-drawer"
      >
        <div class="user-menu">
          <div class="user-header">
            <el-avatar :src="userStore.userInfo?.avatar" :size="64" />
            <div class="user-info">
              <span class="nickname">{{ userStore.userInfo?.nickname || userStore.userInfo?.username }}</span>
              <span class="phone">{{ userStore.userInfo?.phone || '未绑定手机' }}</span>
            </div>
          </div>
          <div class="menu-list">
            <div class="menu-item" @click="goToProfile">
              <el-icon><User /></el-icon>
              <span>个人资料</span>
              <el-icon class="arrow"><ArrowRight /></el-icon>
            </div>
            <div class="menu-item" @click="goToRecords">
              <el-icon><Trophy /></el-icon>
              <span>我的奖品</span>
              <el-icon class="arrow"><ArrowRight /></el-icon>
            </div>
            <div class="menu-item logout" @click="handleLogout">
              <el-icon><SwitchButton /></el-icon>
              <span>退出登录</span>
            </div>
          </div>
        </div>
      </el-drawer>

      <!-- 中奖滚动 -->
      <div class="winner-marquee" v-if="latestWinners.length > 0">
        <div class="marquee-content">
          <div class="winner-item" v-for="(winner, index) in latestWinners" :key="index">
            <el-avatar :src="winner.avatar" :size="20" />
            <span class="nickname">{{ winner.nickname }}</span>
            <span class="text">抽中了</span>
            <span class="prize">{{ winner.prizeName }}</span>
          </div>
        </div>
      </div>

      <!-- 活动列表 -->
      <div class="section-title">
        <el-icon><Present /></el-icon>
        <span>热门活动</span>
      </div>

      <div class="activity-list" v-loading="loading">
        <div
          class="activity-card"
          v-for="activity in activities"
          :key="activity.id"
          @click="goToActivity(activity)"
        >
          <div class="activity-cover">
            <img :src="activity.coverImage" :alt="activity.name" />
            <div class="activity-status" :class="getStatusClass(activity.statusText)">
              {{ activity.statusText }}
            </div>
          </div>
          <div class="activity-info">
            <h3 class="activity-name">{{ activity.name }}</h3>
            <p class="activity-desc">{{ activity.description }}</p>
            <div class="activity-meta">
              <span class="meta-item">
                <el-icon><Clock /></el-icon>
                {{ formatDate(activity.endTime) }} 截止
              </span>
              <span class="meta-item">
                <el-icon><Ticket /></el-icon>
                每日{{ activity.dailyLimit }}次
              </span>
            </div>
          </div>
        </div>

        <el-empty v-if="!loading && activities.length === 0" description="暂无活动" />
      </div>
    </div>

    <!-- 底部导航 -->
    <div class="bottom-nav">
      <div class="nav-item active">
        <el-icon><HomeFilled /></el-icon>
        <span>首页</span>
      </div>
      <div class="nav-item" @click="router.push('/records')">
        <el-icon><Trophy /></el-icon>
        <span>我的奖品</span>
      </div>
      <div class="nav-item" @click="router.push('/profile')">
        <el-icon><User /></el-icon>
        <span>我的</span>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { useUserStore } from '@/stores/user'
import { getActivityList } from '@/api/activity'
import { getLatestWinners } from '@/api/record'

const router = useRouter()
const userStore = useUserStore()

const loading = ref(false)
const activities = ref([])
const latestWinners = ref([])
const showUserMenu = ref(false)

onMounted(() => {
  fetchActivities()
  fetchLatestWinners()
})

async function fetchActivities() {
  loading.value = true
  try {
    const res = await getActivityList()
    activities.value = res.data
  } catch (error) {
    console.error('获取活动列表失败:', error)
  } finally {
    loading.value = false
  }
}

async function fetchLatestWinners() {
  try {
    const res = await getLatestWinners()
    latestWinners.value = res.data
  } catch (error) {
    console.error('获取中奖列表失败:', error)
  }
}

function goToActivity(activity) {
  if (activity.statusText !== '进行中') {
    ElMessage.warning('活动' + activity.statusText)
    return
  }
  
  if (!userStore.isLoggedIn) {
    router.push({ path: '/login', query: { redirect: `/activity/${activity.id}` } })
    return
  }
  
  router.push(`/activity/${activity.id}`)
}

function goToProfile() {
  showUserMenu.value = false
  router.push('/profile')
}

function goToRecords() {
  showUserMenu.value = false
  router.push('/records')
}

function handleLogout() {
  showUserMenu.value = false
  userStore.logout()
  ElMessage.success('已退出登录')
}

function getStatusClass(status) {
  const map = {
    '进行中': 'ongoing',
    '未开始': 'pending',
    '已结束': 'ended',
    '已下线': 'offline',
  }
  return map[status] || ''
}

function formatDate(dateStr) {
  const date = new Date(dateStr)
  return `${date.getMonth() + 1}月${date.getDate()}日`
}
</script>

<style lang="scss" scoped>
.home-page {
  min-height: 100vh;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
}

.page-container {
  padding: 16px;
  padding-bottom: 80px;
}

.top-nav {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 8px 0;
  margin-bottom: 16px;

  .nav-left {
    display: flex;
    align-items: center;
    gap: 8px;

    .logo {
      font-size: 28px;
    }

    .title {
      font-size: 20px;
      font-weight: 700;
      color: #fff;
    }
  }
}

.winner-marquee {
  background: rgba(255, 255, 255, 0.15);
  border-radius: 20px;
  padding: 8px 16px;
  margin-bottom: 24px;
  overflow: hidden;

  .marquee-content {
    display: flex;
    gap: 24px;
    animation: marquee 20s linear infinite;
  }

  .winner-item {
    display: flex;
    align-items: center;
    gap: 6px;
    white-space: nowrap;
    color: #fff;
    font-size: 13px;

    .prize {
      color: var(--secondary-color);
      font-weight: 600;
    }
  }
}

@keyframes marquee {
  0% {
    transform: translateX(0);
  }
  100% {
    transform: translateX(-50%);
  }
}

.section-title {
  display: flex;
  align-items: center;
  gap: 8px;
  color: #fff;
  font-size: 18px;
  font-weight: 600;
  margin-bottom: 16px;
}

.activity-list {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.activity-card {
  background: #fff;
  border-radius: 16px;
  overflow: hidden;
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.1);
  cursor: pointer;
  transition: transform 0.3s ease, box-shadow 0.3s ease;

  &:hover {
    transform: translateY(-4px);
    box-shadow: 0 8px 24px rgba(0, 0, 0, 0.15);
  }

  .activity-cover {
    position: relative;
    height: 160px;

    img {
      height: 100%;
      object-fit: cover;
    }

    .activity-status {
      position: absolute;
      top: 12px;
      right: 12px;
      padding: 4px 12px;
      border-radius: 12px;
      font-size: 12px;
      font-weight: 600;

      &.ongoing {
        background: var(--success-color);
        color: #fff;
      }

      &.pending {
        background: var(--warning-color);
        color: #fff;
      }

      &.ended, &.offline {
        background: #999;
        color: #fff;
      }
    }
  }

  .activity-info {
    padding: 16px;

    .activity-name {
      font-size: 16px;
      font-weight: 600;
      color: var(--text-primary);
      margin-bottom: 8px;
    }

    .activity-desc {
      font-size: 13px;
      color: var(--text-secondary);
      margin-bottom: 12px;
      display: -webkit-box;
      -webkit-line-clamp: 2;
      -webkit-box-orient: vertical;
      overflow: hidden;
    }

    .activity-meta {
      display: flex;
      gap: 16px;

      .meta-item {
        display: flex;
        align-items: center;
        gap: 4px;
        font-size: 12px;
        color: var(--text-secondary);
      }
    }
  }
}

.bottom-nav {
  position: fixed;
  bottom: 0;
  left: 0;
  right: 0;
  height: 64px;
  background: #fff;
  display: flex;
  justify-content: space-around;
  align-items: center;
  box-shadow: 0 -2px 12px rgba(0, 0, 0, 0.08);

  .nav-item {
    display: flex;
    flex-direction: column;
    align-items: center;
    gap: 4px;
    color: var(--text-secondary);
    font-size: 12px;
    cursor: pointer;
    transition: color 0.3s ease;

    .el-icon {
      font-size: 24px;
    }

    &.active {
      color: var(--primary-color);
    }

    &:hover {
      color: var(--primary-color);
    }
  }
}

.avatar-btn {
  cursor: pointer;
  transition: transform 0.2s, box-shadow 0.2s;
  
  &:hover {
    transform: scale(1.1);
    box-shadow: 0 2px 8px rgba(0, 0, 0, 0.2);
  }
}

.user-menu {
  padding: 24px 16px;
  
  .user-header {
    display: flex;
    align-items: center;
    gap: 16px;
    padding-bottom: 24px;
    border-bottom: 1px solid #eee;
    margin-bottom: 16px;
    
    .user-info {
      display: flex;
      flex-direction: column;
      gap: 4px;
      
      .nickname {
        font-size: 18px;
        font-weight: 600;
        color: #333;
      }
      
      .phone {
        font-size: 13px;
        color: #999;
      }
    }
  }
  
  .menu-list {
    display: flex;
    flex-direction: column;
    gap: 8px;
    
    .menu-item {
      display: flex;
      align-items: center;
      gap: 12px;
      padding: 14px 12px;
      border-radius: 12px;
      cursor: pointer;
      transition: background 0.2s;
      
      .el-icon {
        font-size: 20px;
        color: #666;
      }
      
      span {
        flex: 1;
        font-size: 15px;
        color: #333;
      }
      
      .arrow {
        font-size: 16px;
        color: #ccc;
      }
      
      &:hover {
        background: #f5f5f5;
      }
      
      &.logout {
        margin-top: 24px;
        
        .el-icon, span {
          color: #f56c6c;
        }
      }
    }
  }
}
</style>
