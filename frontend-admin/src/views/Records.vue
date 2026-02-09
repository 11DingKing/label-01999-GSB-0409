<template>
  <div class="page-container">
    <!-- 页面头部 -->
    <div class="page-header">
      <div class="page-title">
        <div class="title-icon">
          <el-icon><Trophy /></el-icon>
        </div>
        <span>中奖记录</span>
      </div>
      <el-select v-model="selectedActivity" placeholder="全部活动" clearable style="width: 200px;" @change="fetchData">
        <el-option v-for="a in activityList" :key="a.id" :label="a.name" :value="a.id" />
      </el-select>
    </div>

    <!-- 统计卡片 -->
    <div class="stat-cards">
      <div class="stat-card">
        <div class="stat-icon primary">🎁</div>
        <div class="stat-content">
          <div class="stat-value">{{ stats.totalWins || 0 }}</div>
          <div class="stat-label">中奖总数</div>
        </div>
      </div>
      <div class="stat-card">
        <div class="stat-icon success">✅</div>
        <div class="stat-content">
          <div class="stat-value">{{ stats.receivedCount || 0 }}</div>
          <div class="stat-label">已领取</div>
        </div>
      </div>
      <div class="stat-card">
        <div class="stat-icon warning">⏳</div>
        <div class="stat-content">
          <div class="stat-value">{{ stats.pendingCount || 0 }}</div>
          <div class="stat-label">待领取</div>
        </div>
      </div>
    </div>

    <!-- 筛选 -->
    <div class="filter-section">
      <div class="filter-tabs">
        <div 
          class="filter-tab" 
          :class="{ active: filterStatus === null }"
          @click="filterStatus = null"
        >
          <span>全部</span>
          <span class="count">{{ records.length }}</span>
        </div>
        <div 
          class="filter-tab" 
          :class="{ active: filterStatus === 0 }"
          @click="filterStatus = 0"
        >
          <span>待领取</span>
          <span class="count">{{ pendingRecords.length }}</span>
        </div>
        <div 
          class="filter-tab" 
          :class="{ active: filterStatus === 1 }"
          @click="filterStatus = 1"
        >
          <span>已领取</span>
          <span class="count">{{ receivedRecords.length }}</span>
        </div>
      </div>
    </div>

    <!-- 记录列表 -->
    <div class="card">
      <div class="card-header">
        <span>中奖记录列表</span>
        <el-button text type="primary" @click="fetchData">
          <el-icon><Refresh /></el-icon>
          刷新
        </el-button>
      </div>
      <div class="card-body" style="padding: 0;">
        <el-table :data="filteredRecords" v-loading="loading" stripe>
          <el-table-column label="用户信息" min-width="180">
            <template #default="{ row }">
              <div class="user-info">
                <el-avatar :size="40" class="user-avatar">
                  {{ row.nickname?.charAt(0) || 'U' }}
                </el-avatar>
                <div class="user-detail">
                  <div class="user-nickname">{{ row.nickname || '-' }}</div>
                  <div class="user-username">{{ row.username }}</div>
                </div>
              </div>
            </template>
          </el-table-column>
          
          <el-table-column label="奖品信息" min-width="200">
            <template #default="{ row }">
              <div class="prize-info">
                <el-image 
                  :src="getImageUrl(row.prizeImage)" 
                  class="prize-image"
                  fit="contain"
                >
                  <template #error>
                    <div class="image-placeholder">🎁</div>
                  </template>
                </el-image>
                <div class="prize-detail">
                  <div class="prize-name">{{ row.prizeName }}</div>
                  <div class="prize-activity">{{ row.activityName }}</div>
                </div>
              </div>
            </template>
          </el-table-column>
          
          <el-table-column label="中奖时间" width="180">
            <template #default="{ row }">
              <div class="time-info">
                <div>{{ formatTime(row.drawTime) }}</div>
              </div>
            </template>
          </el-table-column>
          
          <el-table-column label="领取状态" width="120" align="center">
            <template #default="{ row }">
              <el-tag 
                :type="row.status === 1 ? 'success' : 'warning'" 
                effect="dark"
                round
              >
                {{ row.statusText }}
              </el-tag>
            </template>
          </el-table-column>
          
          <el-table-column label="领取时间" width="180">
            <template #default="{ row }">
              <span v-if="row.receiveTime">{{ formatTime(row.receiveTime) }}</span>
              <span v-else class="text-muted">-</span>
            </template>
          </el-table-column>
        </el-table>
        
        <el-empty v-if="!loading && filteredRecords.length === 0" description="暂无中奖记录" />
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { Trophy, Refresh } from '@element-plus/icons-vue'
import { getAllRecords, getRecordStats } from '../api/record'
import { getActivityList } from '../api/activity'
import { getImageUrl } from '../api/file'

const loading = ref(false)
const records = ref([])
const stats = ref({})
const activityList = ref([])
const selectedActivity = ref(null)
const filterStatus = ref(null)

const pendingRecords = computed(() => records.value.filter(r => r.status === 0))
const receivedRecords = computed(() => records.value.filter(r => r.status === 1))

const filteredRecords = computed(() => {
  if (filterStatus.value === null) return records.value
  return records.value.filter(r => r.status === filterStatus.value)
})

onMounted(() => {
  fetchActivities()
  fetchData()
})

async function fetchActivities() {
  try {
    const res = await getActivityList()
    activityList.value = res.data
  } catch (e) {
    console.error('获取活动列表失败', e)
  }
}

async function fetchData() {
  loading.value = true
  try {
    const [recordsRes, statsRes] = await Promise.all([
      getAllRecords(selectedActivity.value),
      getRecordStats(selectedActivity.value)
    ])
    records.value = recordsRes.data
    stats.value = statsRes.data
  } finally {
    loading.value = false
  }
}

function formatTime(time) {
  if (!time) return ''
  return time.replace('T', ' ').substring(0, 19)
}
</script>

<style scoped lang="scss">
.filter-section {
  margin-bottom: 24px;
}

.filter-tabs {
  display: inline-flex;
  background: var(--card-bg);
  border-radius: var(--border-radius);
  padding: 4px;
  box-shadow: var(--shadow-sm);
}

.filter-tab {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 10px 20px;
  border-radius: var(--border-radius-sm);
  cursor: pointer;
  transition: all 0.2s;
  color: var(--text-secondary);
  
  .count {
    font-size: 12px;
    padding: 2px 8px;
    border-radius: 10px;
    background: #f1f5f9;
  }
  
  &:hover {
    background: #f8fafc;
  }
  
  &.active {
    background: var(--bg-gradient);
    color: #fff;
    
    .count {
      background: rgba(255, 255, 255, 0.2);
      color: #fff;
    }
  }
}

.user-info {
  display: flex;
  align-items: center;
  gap: 12px;
}

.user-avatar {
  background: var(--bg-gradient);
  color: #fff;
  font-weight: 600;
}

.user-detail {
  .user-nickname {
    font-weight: 600;
    color: var(--text-primary);
  }
  
  .user-username {
    font-size: 12px;
    color: var(--text-muted);
  }
}

.prize-info {
  display: flex;
  align-items: center;
  gap: 12px;
}

.prize-image {
  width: 48px;
  height: 48px;
  border-radius: 8px;
  background: #f8fafc;
  border: 1px solid var(--border-color);
}

.image-placeholder {
  width: 100%;
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 20px;
}

.prize-detail {
  .prize-name {
    font-weight: 600;
    color: var(--text-primary);
  }
  
  .prize-activity {
    font-size: 12px;
    color: var(--text-muted);
  }
}

.time-info {
  font-size: 13px;
  color: var(--text-secondary);
}

.text-muted {
  color: var(--text-muted);
}
</style>
