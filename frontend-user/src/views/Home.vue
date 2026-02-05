<template>
  <div class="home-page">
    <div class="page-container">
      <!-- 页面头部 -->
      <div class="page-header">
        <div class="header-content">
          <h1 class="page-title">活动大厅</h1>
          <p class="page-subtitle">参与精彩活动，赢取丰厚奖品</p>
        </div>
        <div class="header-stats" v-if="userStore.isLoggedIn">
          <div class="stat-item">
            <span class="stat-value">{{ activities.length }}</span>
            <span class="stat-label">进行中活动</span>
          </div>
        </div>
      </div>

      <!-- 中奖滚动 -->
      <div class="winner-marquee" v-if="latestWinners.length > 0">
        <div class="marquee-label">
          <el-icon><Bell /></el-icon>
          <span>中奖播报</span>
        </div>
        <div class="marquee-wrapper">
          <div class="marquee-content">
            <div class="winner-item" v-for="(winner, index) in [...latestWinners, ...latestWinners]" :key="index">
              <el-avatar :src="winner.avatar" :size="24" />
              <span class="nickname">{{ winner.nickname }}</span>
              <span class="text">抽中了</span>
              <span class="prize">{{ winner.prizeName }}</span>
            </div>
          </div>
        </div>
      </div>

      <!-- 活动列表 -->
      <div class="activity-section">
        <div class="section-header">
          <div class="section-title">
            <el-icon><Present /></el-icon>
            <span>热门活动</span>
          </div>
          <el-button text type="primary" @click="fetchActivities">
            <el-icon><Refresh /></el-icon>
            刷新
          </el-button>
        </div>

        <div class="activity-grid" v-loading="loading">
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
                <div class="meta-item">
                  <el-icon><Clock /></el-icon>
                  <span>{{ formatDate(activity.endTime) }} 截止</span>
                </div>
                <div class="meta-item">
                  <el-icon><Ticket /></el-icon>
                  <span>每日{{ activity.dailyLimit }}次</span>
                </div>
              </div>
              <div class="activity-action">
                <el-button 
                  type="primary" 
                  :disabled="activity.statusText !== '进行中'"
                  class="join-btn"
                >
                  {{ activity.statusText === '进行中' ? '立即参与' : activity.statusText }}
                </el-button>
              </div>
            </div>
          </div>
        </div>

        <el-empty v-if="!loading && activities.length === 0" description="暂无活动">
          <el-button type="primary" @click="fetchActivities">刷新试试</el-button>
        </el-empty>
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
  return `${date.getFullYear()}-${String(date.getMonth() + 1).padStart(2, '0')}-${String(date.getDate()).padStart(2, '0')}`
}
</script>

<style lang="scss" scoped>
.home-page {
  min-height: 100vh;
  background: #f8fafc;
}

.page-container {
  max-width: 1200px;
  margin: 0 auto;
  padding: 32px;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 32px;
  
  .header-content {
    .page-title {
      font-size: 32px;
      font-weight: 700;
      color: var(--text-primary);
      margin-bottom: 8px;
    }
    
    .page-subtitle {
      font-size: 15px;
      color: var(--text-secondary);
    }
  }
  
  .header-stats {
    display: flex;
    gap: 24px;
    
    .stat-item {
      text-align: center;
      padding: 16px 24px;
      background: linear-gradient(135deg, #6366f1 0%, #8b5cf6 100%);
      border-radius: 12px;
      color: #fff;
      
      .stat-value {
        display: block;
        font-size: 28px;
        font-weight: 700;
        margin-bottom: 4px;
      }
      
      .stat-label {
        font-size: 13px;
        opacity: 0.9;
      }
    }
  }
}

.winner-marquee {
  display: flex;
  align-items: center;
  background: linear-gradient(135deg, #fef3c7 0%, #fde68a 100%);
  border-radius: 12px;
  padding: 12px 20px;
  margin-bottom: 32px;
  overflow: hidden;
  
  .marquee-label {
    display: flex;
    align-items: center;
    gap: 6px;
    color: #92400e;
    font-weight: 600;
    font-size: 14px;
    padding-right: 20px;
    border-right: 1px solid rgba(146, 64, 14, 0.2);
    margin-right: 20px;
    flex-shrink: 0;
    
    .el-icon {
      font-size: 18px;
    }
  }
  
  .marquee-wrapper {
    flex: 1;
    overflow: hidden;
  }

  .marquee-content {
    display: flex;
    gap: 40px;
    animation: marquee 30s linear infinite;
  }

  .winner-item {
    display: flex;
    align-items: center;
    gap: 8px;
    white-space: nowrap;
    color: #78350f;
    font-size: 14px;

    .nickname {
      font-weight: 500;
    }
    
    .text {
      color: #92400e;
    }

    .prize {
      color: #dc2626;
      font-weight: 600;
    }
  }
}

@keyframes marquee {
  0% { transform: translateX(0); }
  100% { transform: translateX(-50%); }
}

.activity-section {
  .section-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 24px;
    
    .section-title {
      display: flex;
      align-items: center;
      gap: 8px;
      font-size: 20px;
      font-weight: 600;
      color: var(--text-primary);
      
      .el-icon {
        color: var(--primary-color);
      }
    }
  }
}

.activity-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(360px, 1fr));
  gap: 24px;
}

.activity-card {
  background: #fff;
  border-radius: 16px;
  overflow: hidden;
  box-shadow: var(--shadow);
  cursor: pointer;
  transition: all 0.3s ease;

  &:hover {
    transform: translateY(-4px);
    box-shadow: var(--shadow-xl);
    
    .activity-cover img {
      transform: scale(1.05);
    }
  }

  .activity-cover {
    position: relative;
    height: 200px;
    overflow: hidden;

    img {
      width: 100%;
      height: 100%;
      object-fit: cover;
      transition: transform 0.3s ease;
    }

    .activity-status {
      position: absolute;
      top: 16px;
      right: 16px;
      padding: 6px 16px;
      border-radius: 20px;
      font-size: 13px;
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
        background: #6b7280;
        color: #fff;
      }
    }
  }

  .activity-info {
    padding: 20px;

    .activity-name {
      font-size: 18px;
      font-weight: 600;
      color: var(--text-primary);
      margin-bottom: 8px;
    }

    .activity-desc {
      font-size: 14px;
      color: var(--text-secondary);
      margin-bottom: 16px;
      display: -webkit-box;
      -webkit-line-clamp: 2;
      -webkit-box-orient: vertical;
      overflow: hidden;
      line-height: 1.6;
    }

    .activity-meta {
      display: flex;
      gap: 20px;
      margin-bottom: 16px;

      .meta-item {
        display: flex;
        align-items: center;
        gap: 6px;
        font-size: 13px;
        color: var(--text-secondary);
        
        .el-icon {
          font-size: 16px;
          color: var(--text-muted);
        }
      }
    }
    
    .activity-action {
      .join-btn {
        width: 100%;
        height: 44px;
        font-size: 15px;
        font-weight: 600;
        border-radius: 10px;
      }
    }
  }
}

@media (max-width: 768px) {
  .page-container {
    padding: 16px;
  }
  
  .page-header {
    flex-direction: column;
    gap: 16px;
    
    .header-stats {
      width: 100%;
      
      .stat-item {
        flex: 1;
      }
    }
  }
  
  .activity-grid {
    grid-template-columns: 1fr;
  }
}
</style>
