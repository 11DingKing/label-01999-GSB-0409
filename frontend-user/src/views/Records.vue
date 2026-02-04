<template>
  <div class="records-page">
    <div class="page-container">
      <!-- 页面标题 -->
      <h1 class="page-title">我的奖品</h1>

      <!-- 奖品列表 -->
      <div class="records-list" v-loading="loading">
        <div
          class="record-card"
          v-for="record in records"
          :key="record.id"
        >
          <div class="record-image">
            <img 
              :src="record.prizeImage || defaultImage" 
              :alt="record.prizeName"
              @error="handleImageError"
            />
          </div>
          <div class="record-info">
            <h3 class="prize-name">{{ record.prizeName }}</h3>
            <p class="activity-name">{{ record.activityName }}</p>
            <p class="draw-time">{{ formatTime(record.drawTime) }}</p>
          </div>
          <div class="record-action">
            <el-tag v-if="record.status === 1" type="success" size="small">
              已领取
            </el-tag>
            <el-button
              v-else
              type="primary"
              size="small"
              :loading="receivingId === record.id"
              @click="handleReceive(record)"
            >
              领取
            </el-button>
          </div>
        </div>

        <el-empty v-if="!loading && records.length === 0" description="暂无中奖记录">
          <el-button type="primary" @click="router.push('/home')">
            去抽奖
          </el-button>
        </el-empty>
      </div>
    </div>

    <!-- 底部导航 -->
    <div class="bottom-nav">
      <div class="nav-item" @click="router.push('/home')">
        <el-icon><HomeFilled /></el-icon>
        <span>首页</span>
      </div>
      <div class="nav-item active">
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
import { getMyRecords, receivePrize } from '@/api/record'

const router = useRouter()

const loading = ref(false)
const records = ref([])
const receivingId = ref(null)

// 默认占位图
const defaultImage = 'https://img.icons8.com/color/200/gift.png'

onMounted(() => {
  fetchRecords()
})

async function fetchRecords() {
  loading.value = true
  try {
    const res = await getMyRecords()
    records.value = res.data
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
    ElMessage.success('领取成功')
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
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
}

.page-container {
  padding: 16px;
  padding-bottom: 80px;
}

.page-title {
  font-size: 24px;
  font-weight: 700;
  color: #fff;
  text-align: center;
  margin-bottom: 24px;
  text-shadow: 0 2px 4px rgba(0, 0, 0, 0.2);
}

.records-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.record-card {
  display: flex;
  align-items: center;
  gap: 12px;
  background: #fff;
  border-radius: 12px;
  padding: 16px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.08);

  .record-image {
    width: 64px;
    height: 64px;
    flex-shrink: 0;

    img {
      width: 100%;
      height: 100%;
      object-fit: contain;
      border-radius: 8px;
    }
  }

  .record-info {
    flex: 1;
    min-width: 0;

    .prize-name {
      font-size: 15px;
      font-weight: 600;
      color: var(--text-primary);
      margin-bottom: 4px;
      overflow: hidden;
      text-overflow: ellipsis;
      white-space: nowrap;
    }

    .activity-name {
      font-size: 12px;
      color: var(--text-secondary);
      margin-bottom: 4px;
      overflow: hidden;
      text-overflow: ellipsis;
      white-space: nowrap;
    }

    .draw-time {
      font-size: 11px;
      color: #999;
    }
  }

  .record-action {
    flex-shrink: 0;
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
</style>
