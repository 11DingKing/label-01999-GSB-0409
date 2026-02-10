<template>
  <div class="page-container">
    <!-- 页面头部 -->
    <div class="page-header">
      <div class="page-title">
        <div class="title-icon">
          <el-icon><Tickets /></el-icon>
        </div>
        <span>活动管理</span>
      </div>
      <el-button type="primary" size="large" @click="openDialog()">
        <el-icon><Plus /></el-icon>
        新建活动
      </el-button>
    </div>

    <!-- 统计卡片 -->
    <div class="stat-cards">
      <div class="stat-card">
        <div class="stat-icon primary">📊</div>
        <div class="stat-content">
          <div class="stat-value">{{ activityList.length }}</div>
          <div class="stat-label">活动总数</div>
        </div>
      </div>
      <div class="stat-card">
        <div class="stat-icon success">✅</div>
        <div class="stat-content">
          <div class="stat-value">{{ onlineCount }}</div>
          <div class="stat-label">已上线</div>
        </div>
      </div>
      <div class="stat-card">
        <div class="stat-icon warning">⏳</div>
        <div class="stat-content">
          <div class="stat-value">{{ runningCount }}</div>
          <div class="stat-label">进行中</div>
        </div>
      </div>
      <div class="stat-card">
        <div class="stat-icon danger">📝</div>
        <div class="stat-content">
          <div class="stat-value">{{ draftCount }}</div>
          <div class="stat-label">待上线</div>
        </div>
      </div>
    </div>

    <!-- 活动列表 -->
    <div class="card">
      <div class="card-header">
        <span>活动列表</span>
        <el-button text type="primary" @click="fetchList">
          <el-icon><Refresh /></el-icon>
          刷新
        </el-button>
      </div>
      <div class="card-body" style="padding: 0;">
        <el-table :data="activityList" v-loading="loading" stripe>
          <el-table-column label="活动信息" min-width="300">
            <template #default="{ row }">
              <div class="activity-info">
                <el-image 
                  :src="getImageUrl(row.coverImage)" 
                  class="activity-cover"
                  fit="cover"
                >
                  <template #error>
                    <div class="image-placeholder">📷</div>
                  </template>
                </el-image>
                <div class="activity-detail">
                  <div class="activity-name">{{ row.name }}</div>
                  <div class="activity-desc">{{ row.description || '暂无描述' }}</div>
                  <!-- 提醒标签 -->
                  <div class="activity-alerts" v-if="row.status === 0">
                    <el-tag size="small" type="warning" v-if="!row.hasPrizes">
                      <el-icon style="margin-right: 4px;"><Warning /></el-icon>
                      <span>未配置奖品</span>
                    </el-tag>
                    <el-tag size="small" type="info" v-else>
                      <el-icon style="margin-right: 4px;"><Check /></el-icon>
                      <span>可上线</span>
                    </el-tag>
                  </div>
                </div>
              </div>
            </template>
          </el-table-column>
          
          <el-table-column label="活动时间" min-width="180">
            <template #default="{ row }">
              <div class="time-range">
                <div class="time-item">
                  <span class="time-label">开始</span>
                  <span>{{ formatTime(row.startTime) }}</span>
                </div>
                <div class="time-item">
                  <span class="time-label">结束</span>
                  <span>{{ formatTime(row.endTime) }}</span>
                </div>
              </div>
            </template>
          </el-table-column>
          
          <el-table-column label="抽奖限制" width="120">
            <template #default="{ row }">
              <div class="limit-info">
                <div>每日 {{ row.dailyLimit }} 次</div>
                <div class="text-muted">{{ row.totalLimit ? `总计 ${row.totalLimit} 次` : '不限总次数' }}</div>
              </div>
            </template>
          </el-table-column>
          
          <el-table-column label="状态" width="100" align="center">
            <template #default="{ row }">
              <el-tag 
                :type="getStatusType(row)" 
                effect="dark"
                round
              >
                {{ row.status === 0 ? '未上线' : row.statusText }}
              </el-tag>
            </template>
          </el-table-column>
          
          <el-table-column label="操作" width="280" fixed="right">
            <template #default="{ row }">
              <div class="action-buttons">
                <el-button type="primary" link @click="goToPrizes(row)">
                  <el-icon><Present /></el-icon>
                  奖品
                </el-button>
                <el-button type="primary" link @click="openDialog(row)">
                  <el-icon><Edit /></el-icon>
                  编辑
                </el-button>
                <el-button 
                  v-if="row.status === 1" 
                  type="warning" 
                  link 
                  @click="handleOffline(row.id)"
                >
                  下线
                </el-button>
                <el-button 
                  v-else 
                  type="success" 
                  link 
                  @click="handleOnline(row)"
                >
                  上线
                </el-button>
                <el-popconfirm 
                  title="确定删除该活动？" 
                  @confirm="handleDelete(row.id)"
                  confirm-button-type="danger"
                >
                  <template #reference>
                    <el-button type="danger" link>删除</el-button>
                  </template>
                </el-popconfirm>
              </div>
            </template>
          </el-table-column>
        </el-table>
      </div>
    </div>

    <!-- 新建/编辑对话框 -->
    <el-dialog 
      v-model="dialogVisible" 
      :title="isEdit ? '编辑活动' : '新建活动'" 
      width="600px"
      destroy-on-close
    >
      <el-form 
        ref="formRef" 
        :model="form" 
        :rules="rules" 
        label-width="100px"
      >
        <el-form-item label="活动名称" prop="name">
          <el-input v-model="form.name" placeholder="请输入活动名称" maxlength="50" show-word-limit />
        </el-form-item>
        
        <el-form-item label="活动描述" prop="description">
          <el-input 
            v-model="form.description" 
            type="textarea" 
            :rows="3" 
            placeholder="请输入活动描述"
            maxlength="200"
            show-word-limit
          />
        </el-form-item>
        
        <el-form-item label="封面图片" prop="coverImage">
          <div class="image-uploader">
            <el-upload
              :show-file-list="false"
              :before-upload="beforeUpload"
              :http-request="handleUpload"
              accept="image/*"
            >
              <img v-if="form.coverImage" :src="getImageUrl(form.coverImage)" class="preview-image" />
              <div v-else class="upload-trigger">
                <el-icon class="upload-icon"><Plus /></el-icon>
                <span class="upload-text">点击上传封面</span>
              </div>
            </el-upload>
            <div class="upload-tip" v-if="!form.coverImage">支持 JPG、PNG 格式，建议尺寸 800x400</div>
          </div>
        </el-form-item>
        
        <el-form-item label="活动时间" prop="timeRange" required>
          <el-date-picker 
            v-model="form.timeRange" 
            type="datetimerange" 
            range-separator="至" 
            start-placeholder="开始时间" 
            end-placeholder="结束时间" 
            value-format="YYYY-MM-DDTHH:mm:ss" 
            style="width: 100%"
          />
        </el-form-item>
        
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="每日限制" prop="dailyLimit">
              <el-input-number v-model="form.dailyLimit" :min="1" :max="100" style="width: 100%" @change="onDailyLimitChange" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="总限制" prop="totalLimit">
              <el-input-number v-model="form.totalLimit" :min="0" style="width: 100%" />
              <div class="form-tip">0表示不限制，非0时不能小于每日限制</div>
            </el-form-item>
          </el-col>
        </el-row>
      </el-form>
      
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitLoading" @click="handleSubmit">
          {{ isEdit ? '保存修改' : '创建活动' }}
        </el-button>
      </template>
    </el-dialog>

    <!-- 上线确认对话框 -->
    <el-dialog v-model="onlineDialogVisible" title="上线确认" width="450px">
      <div class="online-confirm">
        <div class="confirm-icon" :class="canOnline ? 'success' : 'warning'">
          {{ canOnline ? '✅' : '⚠️' }}
        </div>
        <div class="confirm-content">
          <h3>{{ canOnline ? '活动已准备就绪' : '活动配置不完整' }}</h3>
          <p v-if="canOnline">确定要上线「{{ currentActivity?.name }}」吗？上线后用户即可参与抽奖。</p>
          <p v-else>请先为活动配置奖品后再上线。</p>
        </div>
      </div>
      <template #footer>
        <el-button @click="onlineDialogVisible = false">取消</el-button>
        <el-button v-if="!canOnline" type="primary" @click="goToPrizesFromDialog">
          去配置奖品
        </el-button>
        <el-button v-else type="primary" :loading="onlineLoading" @click="confirmOnline">
          确认上线
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted, watch } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Tickets, Plus, Present, Edit, Refresh, Warning, Check } from '@element-plus/icons-vue'
import { getActivityList, createActivity, updateActivity, deleteActivity, onlineActivity, offlineActivity } from '../api/activity'
import { getPrizeList } from '../api/prize'
import { uploadFile, getImageUrl } from '../api/file'

const router = useRouter()
const route = useRoute()
const loading = ref(false)
const activityList = ref([])
const dialogVisible = ref(false)
const isEdit = ref(false)
const editId = ref(null)
const formRef = ref()
const submitLoading = ref(false)

// 上线确认
const onlineDialogVisible = ref(false)
const onlineLoading = ref(false)
const currentActivity = ref(null)
const canOnline = ref(false)

const form = reactive({
  name: '',
  description: '',
  coverImage: '',
  timeRange: [],
  dailyLimit: 3,
  totalLimit: 0
})

const rules = {
  name: [{ required: true, message: '请输入活动名称', trigger: 'blur' }],
  timeRange: [{ required: true, message: '请选择活动时间', trigger: 'change' }],
  totalLimit: [{
    validator: (rule, value, callback) => {
      if (value > 0 && value < form.dailyLimit) {
        callback(new Error('总限制不能小于每日限制'))
      } else {
        callback()
      }
    },
    trigger: 'change'
  }]
}

const onlineCount = computed(() => activityList.value.filter(a => a.status === 1).length)
const runningCount = computed(() => activityList.value.filter(a => a.statusText === '进行中').length)
const draftCount = computed(() => activityList.value.filter(a => a.status === 0).length)

onMounted(() => {
  fetchList()
})

// 监听路由变化，每次进入页面都刷新数据
watch(() => route.path, (newPath) => {
  if (newPath === '/activity') {
    fetchList()
  }
})

async function fetchList() {
  loading.value = true
  try {
    const res = await getActivityList()
    // 检查每个活动是否有奖品
    const activities = res.data
    for (const activity of activities) {
      try {
        const prizeRes = await getPrizeList(activity.id)
        activity.hasPrizes = prizeRes.data && prizeRes.data.length > 0
      } catch {
        activity.hasPrizes = false
      }
    }
    activityList.value = activities
  } finally {
    loading.value = false
  }
}

function formatTime(time) {
  if (!time) return ''
  return time.replace('T', ' ').substring(0, 16)
}

function getStatusType(row) {
  if (row.status === 0) return 'info'
  const map = {
    '进行中': 'success',
    '未开始': 'warning',
    '已结束': 'info',
    '已下线': 'danger'
  }
  return map[row.statusText] || 'info'
}

function openDialog(row) {
  isEdit.value = !!row
  editId.value = row?.id || null
  if (row) {
    form.name = row.name
    form.description = row.description
    form.coverImage = row.coverImage
    form.timeRange = [row.startTime, row.endTime]
    form.dailyLimit = row.dailyLimit
    form.totalLimit = row.totalLimit
  } else {
    form.name = ''
    form.description = ''
    form.coverImage = ''
    form.timeRange = []
    form.dailyLimit = 3
    form.totalLimit = 0
  }
  dialogVisible.value = true
}

function onDailyLimitChange() {
  // 每日限制变化时，重新校验总限制
  if (formRef.value) {
    formRef.value.validateField('totalLimit')
  }
}

function beforeUpload(file) {
  const isImage = file.type.startsWith('image/')
  const isLt2M = file.size / 1024 / 1024 < 2
  if (!isImage) {
    ElMessage.error('只能上传图片文件!')
    return false
  }
  if (!isLt2M) {
    ElMessage.error('图片大小不能超过 2MB!')
    return false
  }
  return true
}

async function handleUpload(options) {
  try {
    const res = await uploadFile(options.file)
    form.coverImage = res.data.url
    ElMessage.success('上传成功')
  } catch (e) {
    ElMessage.error('上传失败')
  }
}

async function handleSubmit() {
  await formRef.value.validate()
  submitLoading.value = true
  try {
    const data = {
      name: form.name,
      description: form.description,
      coverImage: form.coverImage,
      startTime: form.timeRange[0],
      endTime: form.timeRange[1],
      dailyLimit: form.dailyLimit,
      totalLimit: form.totalLimit,
      status: isEdit.value ? undefined : 0  // 新建活动默认下线状态
    }
    if (isEdit.value) {
      await updateActivity(editId.value, data)
      ElMessage.success('更新成功')
    } else {
      const res = await createActivity(data)
      ElMessage.success('创建成功')
      // 提示去配置奖品
      ElMessage({
        message: '活动已创建，请配置奖品后再上线',
        type: 'warning',
        duration: 3000
      })
      dialogVisible.value = false
      fetchList()
      // 跳转到奖品配置
      setTimeout(() => {
        router.push(`/activity/${res.data.id}/prizes`)
      }, 500)
      return
    }
    dialogVisible.value = false
    fetchList()
  } finally {
    submitLoading.value = false
  }
}

async function handleOnline(row) {
  currentActivity.value = row
  canOnline.value = row.hasPrizes
  onlineDialogVisible.value = true
}

async function confirmOnline() {
  onlineLoading.value = true
  try {
    await onlineActivity(currentActivity.value.id)
    ElMessage.success('上线成功')
    onlineDialogVisible.value = false
    fetchList()
  } finally {
    onlineLoading.value = false
  }
}

function goToPrizesFromDialog() {
  onlineDialogVisible.value = false
  router.push(`/activity/${currentActivity.value.id}/prizes`)
}

async function handleOffline(id) {
  await offlineActivity(id)
  ElMessage.success('下线成功')
  fetchList()
}

async function handleDelete(id) {
  await deleteActivity(id)
  ElMessage.success('删除成功')
  fetchList()
}

function goToPrizes(row) {
  router.push(`/activity/${row.id}/prizes`)
}
</script>

<style scoped lang="scss">
.activity-info {
  display: flex;
  align-items: flex-start;
  gap: 16px;
  padding: 12px 0;
}

.activity-cover {
  width: 120px;
  height: 80px;
  border-radius: 8px;
  flex-shrink: 0;
  background: #f1f5f9;
}

.image-placeholder {
  width: 100%;
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  background: #f1f5f9;
  color: #94a3b8;
  font-size: 24px;
}

.activity-detail {
  flex: 1;
  min-width: 0;
}

.activity-name {
  font-weight: 600;
  color: var(--text-primary);
  margin-bottom: 4px;
  font-size: 15px;
}

.activity-desc {
  font-size: 13px;
  color: var(--text-secondary);
  margin-bottom: 8px;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.activity-alerts {
  display: flex;
  gap: 8px;
  
  .el-tag {
    display: inline-flex;
    align-items: center;
    white-space: nowrap;
  }
}

.time-range {
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.time-item {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 13px;
  color: var(--text-secondary);
  
  .time-label {
    font-size: 12px;
    color: var(--text-muted);
    background: #f1f5f9;
    padding: 2px 6px;
    border-radius: 4px;
  }
}

.limit-info {
  font-size: 13px;
  
  .text-muted {
    color: var(--text-muted);
    font-size: 12px;
    margin-top: 2px;
  }
}

.upload-tip {
  font-size: 12px;
  color: var(--text-muted);
  margin-top: 8px;
}

.online-confirm {
  text-align: center;
  padding: 20px 0;
  
  .confirm-icon {
    font-size: 48px;
    margin-bottom: 16px;
  }
  
  .confirm-content {
    h3 {
      font-size: 18px;
      font-weight: 600;
      color: var(--text-primary);
      margin-bottom: 8px;
    }
    
    p {
      font-size: 14px;
      color: var(--text-secondary);
    }
  }
}
</style>
