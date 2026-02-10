<template>
  <div class="records-page">
    <div class="page-container">
      <!-- 页面头部 -->
      <div class="page-header">
        <div class="header-content">
          <h1 class="page-title">我的奖品</h1>
          <p class="page-subtitle">查看和领取您的中奖记录</p>
        </div>
      </div>

      <!-- 统计卡片 -->
      <div class="stats-row">
        <div class="stat-card">
          <div class="stat-icon total">
            <el-icon><Present /></el-icon>
          </div>
          <div class="stat-info">
            <span class="stat-value">{{ records.length }}</span>
            <span class="stat-label">中奖总数</span>
          </div>
        </div>
        <div class="stat-card">
          <div class="stat-icon pending">
            <el-icon><Clock /></el-icon>
          </div>
          <div class="stat-info">
            <span class="stat-value">{{ pendingCount }}</span>
            <span class="stat-label">待领取</span>
          </div>
        </div>
        <div class="stat-card">
          <div class="stat-icon received">
            <el-icon><CircleCheck /></el-icon>
          </div>
          <div class="stat-info">
            <span class="stat-value">{{ receivedCount }}</span>
            <span class="stat-label">已领取</span>
          </div>
        </div>
      </div>

      <!-- 筛选区域 -->
      <div class="filter-section">
        <div class="filter-tabs">
          <div 
            class="filter-tab" 
            :class="{ active: filterStatus === null }"
            @click="filterStatus = null"
          >
            <span class="tab-text">全部</span>
            <span class="tab-count">{{ records.length }}</span>
          </div>
          <div 
            class="filter-tab" 
            :class="{ active: filterStatus === 0 }"
            @click="filterStatus = 0"
          >
            <span class="tab-text">待领取</span>
            <span class="tab-count">{{ pendingCount }}</span>
          </div>
          <div 
            class="filter-tab" 
            :class="{ active: filterStatus === 1 }"
            @click="filterStatus = 1"
          >
            <span class="tab-text">已领取</span>
            <span class="tab-count">{{ receivedCount }}</span>
          </div>
        </div>
      </div>

      <!-- 奖品列表 -->
      <div class="records-section" v-loading="loading">
        <div class="records-grid" v-if="filteredRecords.length > 0">
          <div class="record-card" v-for="record in filteredRecords" :key="record.id">
            <div class="card-header">
              <span class="activity-tag">{{ record.activityName }}</span>
              <span class="status-badge" :class="record.status === 1 ? 'received' : 'pending'">
                {{ record.status === 1 ? '已领取' : '待领取' }}
              </span>
            </div>
            <div class="card-body">
              <div class="prize-image">
                <img :src="record.prizeImage || defaultImage" :alt="record.prizeName" @error="handleImageError" />
              </div>
              <div class="prize-info">
                <h3 class="prize-name">{{ record.prizeName }}</h3>
                <div class="prize-meta">
                  <div class="meta-item">
                    <el-icon><Calendar /></el-icon>
                    <span>{{ formatTime(record.drawTime) }}</span>
                  </div>
                </div>
              </div>
            </div>
            <div class="card-footer">
              <el-button
                v-if="record.status !== 1"
                type="primary"
                size="large"
                :loading="receivingId === record.id"
                @click="handleReceive(record)"
              >
                <el-icon><Check /></el-icon>
                立即领取
              </el-button>
              <div v-else class="received-status">
                <el-icon><CircleCheck /></el-icon>
                <span>已领取</span>
              </div>
            </div>
          </div>
        </div>

        <div class="empty-state" v-if="!loading && filteredRecords.length === 0">
          <div class="empty-icon">🎁</div>
          <h3 class="empty-title">{{ emptyTitle }}</h3>
          <p class="empty-desc">{{ emptyDesc }}</p>
          <el-button type="primary" size="large" @click="router.push('/home')">
            <el-icon><Present /></el-icon>
            去抽奖
          </el-button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, watch } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import { getMyRecords, receivePrize } from '@/api/record'
import { getImageUrl } from '@/utils/image'

const router = useRouter()
const route = useRoute()

const loading = ref(false)
const records = ref([])
const receivingId = ref(null)
const filterStatus = ref(null)

const defaultImage = '/api/images/prizes/thanks.svg'

const filteredRecords = computed(() => {
  if (filterStatus.value === null) return records.value
  return records.value.filter(r => r.status === filterStatus.value)
})

const pendingCount = computed(() => records.value.filter(r => r.status === 0).length)
const receivedCount = computed(() => records.value.filter(r => r.status === 1).length)

const emptyTitle = computed(() => {
  if (filterStatus.value === 0) return '暂无待领取的奖品'
  if (filterStatus.value === 1) return '暂无已领取的奖品'
  return '暂无中奖记录'
})

const emptyDesc = computed(() => {
  if (filterStatus.value === 0) return '您的奖品都已领取完毕'
  if (filterStatus.value === 1) return '领取奖品后会显示在这里'
  return '快去参与抽奖活动，赢取丰厚奖品吧！'
})

onMounted(() => {
  fetchRecords()
})

// 监听路由变化，每次进入页面都刷新数据
watch(() => route.path, (newPath) => {
  if (newPath === '/records') {
    fetchRecords()
  }
})

async function fetchRecords() {
  loading.value = true
  try {
    const res = await getMyRecords()
    records.value = res.data.map(r => ({ ...r, prizeImage: getImageUrl(r.prizeImage) }))
  } catch (error) {
    console.error('获取中奖记录失败:', error)
  } finally {
    loading.value = false
  }
}

async function handleReceive(record) {
  receivingId.value = record.id
  try {
    await receivePrize(record.id)
    ElMessage.success('领取成功！')
    record.status = 1
  } catch (error) {
    console.error('领取失败:', error)
  } finally {
    receivingId.value = null
  }
}

function formatTime(dateStr) {
  const date = new Date(dateStr)
  return `${date.getFullYear()}-${String(date.getMonth() + 1).padStart(2, '0')}-${String(date.getDate()).padStart(2, '0')} ${String(date.getHours()).padStart(2, '0')}:${String(date.getMinutes()).padStart(2, '0')}`
}

function handleImageError(e) {
  e.target.src = defaultImage
}
</script>

<style lang="scss" scoped>
.records-page {
  min-height: 100vh;
  background: #f5f7fa;
}

.page-container {
  max-width: 1200px;
  margin: 0 auto;
  padding: 32px;
}

.page-header {
  margin-bottom: 32px;
  
  .page-title {
    font-size: 32px;
    font-weight: 700;
    color: #1e293b;
    margin-bottom: 8px;
  }
  
  .page-subtitle {
    font-size: 15px;
    color: #64748b;
  }
}

.stats-row {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 24px;
  margin-bottom: 32px;
}

.stat-card {
  background: #fff;
  border-radius: 16px;
  padding: 24px;
  display: flex;
  align-items: center;
  gap: 20px;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.05);
  transition: all 0.3s ease;
  
  &:hover {
    transform: translateY(-2px);
    box-shadow: 0 8px 24px rgba(0, 0, 0, 0.08);
  }
  
  .stat-icon {
    width: 64px;
    height: 64px;
    border-radius: 16px;
    display: flex;
    align-items: center;
    justify-content: center;
    
    .el-icon { font-size: 28px; color: #fff; }
    
    &.total { background: linear-gradient(135deg, #6366f1 0%, #8b5cf6 100%); }
    &.pending { background: linear-gradient(135deg, #f59e0b 0%, #d97706 100%); }
    &.received { background: linear-gradient(135deg, #10b981 0%, #059669 100%); }
  }
  
  .stat-info {
    display: flex;
    flex-direction: column;
    gap: 4px;
    
    .stat-value { font-size: 32px; font-weight: 700; color: #1e293b; }
    .stat-label { font-size: 14px; color: #64748b; }
  }
}

.filter-section {
  margin-bottom: 24px;
}

.filter-tabs {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 0;
  background: #fff;
  border-radius: 12px;
  padding: 6px;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.05);
}

.filter-tab {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 10px;
  padding: 16px 24px;
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.2s ease;
  
  .tab-text { font-size: 15px; font-weight: 500; color: #64748b; }
  .tab-count {
    font-size: 13px;
    padding: 3px 12px;
    border-radius: 12px;
    background: #f1f5f9;
    color: #64748b;
  }
  
  &:hover { background: #f8fafc; }
  
  &.active {
    background: linear-gradient(135deg, #6366f1 0%, #8b5cf6 100%);
    .tab-text { color: #fff; }
    .tab-count { background: rgba(255, 255, 255, 0.2); color: #fff; }
  }
}

.records-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(360px, 1fr));
  gap: 24px;
}

.record-card {
  background: #fff;
  border-radius: 16px;
  overflow: hidden;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.05);
  transition: all 0.3s ease;
  
  &:hover {
    transform: translateY(-4px);
    box-shadow: 0 12px 32px rgba(0, 0, 0, 0.1);
  }
  
  .card-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    padding: 16px 20px;
    background: #f8fafc;
    border-bottom: 1px solid #f1f5f9;
    
    .activity-tag {
      font-size: 13px;
      color: #64748b;
      padding: 4px 12px;
      background: #fff;
      border-radius: 6px;
      border: 1px solid #e2e8f0;
    }
    
    .status-badge {
      font-size: 12px;
      font-weight: 600;
      padding: 6px 14px;
      border-radius: 20px;
      
      &.pending { background: linear-gradient(135deg, #fef3c7 0%, #fde68a 100%); color: #92400e; }
      &.received { background: linear-gradient(135deg, #d1fae5 0%, #a7f3d0 100%); color: #065f46; }
    }
  }
  
  .card-body {
    display: flex;
    gap: 20px;
    padding: 24px;
    
    .prize-image {
      width: 100px;
      height: 100px;
      flex-shrink: 0;
      background: linear-gradient(135deg, #f8fafc 0%, #f1f5f9 100%);
      border-radius: 12px;
      display: flex;
      align-items: center;
      justify-content: center;
      padding: 12px;
      
      img { max-width: 100%; max-height: 100%; object-fit: contain; }
    }
    
    .prize-info {
      flex: 1;
      display: flex;
      flex-direction: column;
      justify-content: center;
      
      .prize-name { font-size: 18px; font-weight: 600; color: #1e293b; margin-bottom: 12px; }
      
      .meta-item {
        display: flex;
        align-items: center;
        gap: 6px;
        font-size: 13px;
        color: #64748b;
        .el-icon { font-size: 16px; color: #94a3b8; }
      }
    }
  }
  
  .card-footer {
    padding: 16px 24px 24px;
    
    .el-button {
      width: 100%;
      height: 48px;
      font-size: 15px;
      font-weight: 600;
      border-radius: 10px;
    }
    
    .received-status {
      display: flex;
      align-items: center;
      justify-content: center;
      gap: 8px;
      height: 48px;
      background: #f0fdf4;
      border-radius: 10px;
      color: #16a34a;
      font-size: 15px;
      font-weight: 600;
      .el-icon { font-size: 20px; }
    }
  }
}

.empty-state {
  text-align: center;
  padding: 80px 40px;
  background: #fff;
  border-radius: 16px;
  
  .empty-icon { font-size: 80px; margin-bottom: 24px; }
  .empty-title { font-size: 20px; font-weight: 600; color: #1e293b; margin-bottom: 8px; }
  .empty-desc { font-size: 14px; color: #64748b; margin-bottom: 32px; }
  .el-button { height: 48px; padding: 0 32px; font-size: 15px; }
}

@media (max-width: 768px) {
  .page-container { padding: 16px; }
  .stats-row { grid-template-columns: 1fr; }
  .filter-tabs { grid-template-columns: 1fr; }
  .records-grid { grid-template-columns: 1fr; }
}
</style>
