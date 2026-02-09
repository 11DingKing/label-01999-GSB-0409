<template>
  <div class="page-container">
    <!-- 页面头部 -->
    <div class="page-header">
      <div class="page-title">
        <el-button text @click="router.push('/activity')" class="back-btn">
          <el-icon><ArrowLeft /></el-icon>
        </el-button>
        <div class="title-icon">
          <el-icon><Present /></el-icon>
        </div>
        <span>奖品管理</span>
        <el-tag type="info" effect="plain" style="margin-left: 12px;">{{ activityName }}</el-tag>
      </div>
      <el-button type="primary" size="large" @click="openDialog()">
        <el-icon><Plus /></el-icon>
        新建奖品
      </el-button>
    </div>

    <!-- 活动状态提醒 -->
    <div class="alert-card warning" v-if="activityStatus === 0 && prizeList.length === 0">
      <div class="alert-icon">⚠️</div>
      <div class="alert-content">
        <div class="alert-title">活动未配置奖品</div>
        <div class="alert-text">请添加奖品并配置中奖概率，完成后可上线活动</div>
      </div>
    </div>
    
    <div class="alert-card success" v-else-if="activityStatus === 0 && prizeList.length > 0 && isProbabilityValid">
      <div class="alert-icon">✅</div>
      <div class="alert-content">
        <div class="alert-title">奖品配置完成</div>
        <div class="alert-text">概率配置正常，可以上线活动了</div>
      </div>
      <el-button type="primary" @click="handleOnlineActivity">
        <el-icon><Check /></el-icon>
        立即上线
      </el-button>
    </div>

    <!-- 概率统计 -->
    <div class="card probability-card">
      <div class="card-body">
        <div class="prob-stats">
          <div class="prob-stat-item">
            <span class="label">奖品数量</span>
            <span class="value">{{ prizeList.length }}</span>
          </div>
          <div class="prob-stat-item">
            <span class="label">概率总和</span>
            <span class="value" :class="{ warning: !isProbabilityValid }">
              {{ totalProbability.toFixed(2) }}%
            </span>
          </div>
          <div class="prob-stat-item">
            <span class="label">配置状态</span>
            <el-tag :type="isProbabilityValid ? 'success' : 'warning'" effect="dark">
              {{ isProbabilityValid ? '正常' : '需调整' }}
            </el-tag>
          </div>
        </div>
        <div class="prob-visual">
          <div 
            v-for="(prize, index) in prizeList" 
            :key="prize.id"
            class="prob-segment"
            :style="{ 
              width: (prize.probability * 100) + '%',
              backgroundColor: getSegmentColor(index)
            }"
            :title="`${prize.name}: ${(prize.probability * 100).toFixed(2)}%`"
          ></div>
        </div>
        <div class="prob-legend">
          <div class="legend-item" v-for="(prize, index) in prizeList" :key="prize.id">
            <span class="legend-color" :style="{ backgroundColor: getSegmentColor(index) }"></span>
            <span class="legend-name">{{ prize.name }}</span>
            <span class="legend-value">{{ (prize.probability * 100).toFixed(2) }}%</span>
          </div>
        </div>
      </div>
    </div>

    <!-- 奖品列表 -->
    <div class="card">
      <div class="card-header">
        <span>奖品列表</span>
        <el-button text type="primary" @click="fetchList">
          <el-icon><Refresh /></el-icon>
          刷新
        </el-button>
      </div>
      <div class="card-body" style="padding: 0;">
        <el-table :data="prizeList" v-loading="loading" stripe>
          <el-table-column label="奖品信息" min-width="260">
            <template #default="{ row }">
              <div class="prize-info">
                <el-image 
                  :src="getImageUrl(row.image)" 
                  class="prize-image"
                  fit="contain"
                  :preview-src-list="[getImageUrl(row.image)]"
                  preview-teleported
                >
                  <template #error>
                    <div class="image-placeholder">🎁</div>
                  </template>
                </el-image>
                <div class="prize-detail">
                  <div class="prize-name">{{ row.name }}</div>
                  <div class="prize-id">ID: {{ row.id }} · 排序: {{ row.sortOrder }}</div>
                </div>
              </div>
            </template>
          </el-table-column>
          
          <el-table-column label="中奖概率" width="200">
            <template #default="{ row }">
              <div class="probability-bar">
                <div class="bar">
                  <div class="fill" :style="{ width: (row.probability * 100) + '%' }"></div>
                </div>
                <span class="value">{{ (row.probability * 100).toFixed(2) }}%</span>
              </div>
            </template>
          </el-table-column>
          
          <el-table-column label="库存" width="160">
            <template #default="{ row }">
              <div class="stock-info">
                <div class="stock-numbers">
                  <span class="remaining">{{ row.remainingCount }}</span>
                  <span class="separator">/</span>
                  <span class="total">{{ row.totalCount }}</span>
                </div>
                <div class="stock-display">
                  <div class="stock-bar">
                    <div 
                      class="fill" 
                      :class="getStockClass(row)"
                      :style="{ width: getStockPercent(row) + '%' }"
                    ></div>
                  </div>
                </div>
              </div>
            </template>
          </el-table-column>
          
          <el-table-column label="状态" width="90" align="center">
            <template #default="{ row }">
              <el-tag 
                :type="row.status === 1 ? 'success' : 'info'" 
                effect="dark"
                round
                size="small"
              >
                {{ row.status === 1 ? '启用' : '禁用' }}
              </el-tag>
            </template>
          </el-table-column>
          
          <el-table-column label="操作" width="260" fixed="right">
            <template #default="{ row }">
              <div class="action-buttons">
                <el-button type="primary" link @click="openDialog(row)">编辑</el-button>
                <el-button type="primary" link @click="openStockDialog(row)">补货</el-button>
                <el-button 
                  v-if="row.status === 1" 
                  type="warning" 
                  link 
                  @click="handleOffline(row.id)"
                >
                  禁用
                </el-button>
                <el-button 
                  v-else 
                  type="success" 
                  link 
                  @click="handleOnline(row.id)"
                >
                  启用
                </el-button>
                <el-popconfirm 
                  title="确定删除该奖品？" 
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
      :title="isEdit ? '编辑奖品' : '新建奖品'" 
      width="550px"
      destroy-on-close
    >
      <el-form 
        ref="formRef" 
        :model="form" 
        :rules="rules" 
        label-width="100px"
      >
        <el-form-item label="奖品名称" prop="name">
          <el-input v-model="form.name" placeholder="请输入奖品名称" maxlength="50" show-word-limit />
        </el-form-item>
        
        <el-form-item label="奖品图片" prop="image">
          <div class="image-uploader">
            <el-upload
              :show-file-list="false"
              :before-upload="beforeUpload"
              :http-request="handleUpload"
              accept="image/*"
            >
              <img v-if="form.image" :src="getImageUrl(form.image)" class="preview-image" />
              <div v-else class="upload-trigger">
                <el-icon class="upload-icon"><Plus /></el-icon>
                <span class="upload-text">上传图片</span>
              </div>
            </el-upload>
          </div>
        </el-form-item>
        
        <el-form-item label="中奖概率" prop="probability">
          <div class="probability-input">
            <el-slider 
              v-model="form.probability" 
              :min="0" 
              :max="100" 
              :step="0.01"
              style="flex: 1; margin-right: 16px;"
            />
            <el-input-number 
              v-model="form.probability" 
              :min="0" 
              :max="100" 
              :precision="2" 
              :step="0.1"
              style="width: 120px;"
            />
            <span style="margin-left: 8px;">%</span>
          </div>
        </el-form-item>
        
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="奖品数量" prop="totalCount">
              <el-input-number v-model="form.totalCount" :min="0" style="width: 100%" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="排序" prop="sortOrder">
              <el-input-number v-model="form.sortOrder" :min="0" style="width: 100%" />
            </el-form-item>
          </el-col>
        </el-row>
      </el-form>
      
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitLoading" @click="handleSubmit">
          {{ isEdit ? '保存修改' : '创建奖品' }}
        </el-button>
      </template>
    </el-dialog>

    <!-- 补货对话框 -->
    <el-dialog v-model="stockDialogVisible" title="库存补货" width="450px" destroy-on-close>
      <div class="stock-dialog-content">
        <div class="current-stock">
          <div class="stock-label">当前库存</div>
          <div class="stock-value">
            <span class="remaining">{{ stockForm.remainingCount }}</span>
            <span class="separator">/</span>
            <span class="total">{{ stockForm.totalCount }}</span>
          </div>
        </div>
        
        <el-divider />
        
        <el-form label-width="100px">
          <el-form-item label="新总数量">
            <el-input-number 
              v-model="stockForm.newTotalCount" 
              :min="stockForm.totalCount - stockForm.remainingCount"
              style="width: 100%"
            />
            <div class="form-tip">
              补货后剩余: {{ stockForm.newTotalCount - stockForm.totalCount + stockForm.remainingCount }}
            </div>
          </el-form-item>
        </el-form>
      </div>
      
      <template #footer>
        <el-button @click="stockDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="stockLoading" @click="handleStockSubmit">确认补货</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Present, Plus, ArrowLeft, Refresh, Check } from '@element-plus/icons-vue'
import { getActivityDetail, onlineActivity } from '../api/activity'
import { getPrizeList, createPrize, updatePrize, deletePrize, onlinePrize, offlinePrize, updatePrizeStock } from '../api/prize'
import { uploadFile, getImageUrl } from '../api/file'

const route = useRoute()
const router = useRouter()
const activityId = route.params.id
const activityName = ref('')
const activityStatus = ref(0)
const loading = ref(false)
const prizeList = ref([])
const dialogVisible = ref(false)
const isEdit = ref(false)
const editId = ref(null)
const formRef = ref()
const submitLoading = ref(false)

const form = reactive({
  name: '',
  image: '',
  probability: 0,
  totalCount: 0,
  sortOrder: 0
})

const rules = {
  name: [{ required: true, message: '请输入奖品名称', trigger: 'blur' }],
  probability: [{ required: true, message: '请输入中奖概率', trigger: 'blur' }],
  totalCount: [{ required: true, message: '请输入奖品数量', trigger: 'blur' }]
}

const stockDialogVisible = ref(false)
const stockLoading = ref(false)
const stockForm = reactive({
  id: null,
  totalCount: 0,
  remainingCount: 0,
  newTotalCount: 0
})

const totalProbability = computed(() => {
  return prizeList.value.reduce((sum, p) => sum + (p.probability * 100), 0)
})

const isProbabilityValid = computed(() => {
  return Math.abs(totalProbability.value - 100) <= 0.01
})

const segmentColors = ['#6366f1', '#10b981', '#f59e0b', '#ef4444', '#8b5cf6', '#06b6d4', '#ec4899', '#84cc16']

function getSegmentColor(index) {
  return segmentColors[index % segmentColors.length]
}

function getStockPercent(row) {
  if (row.totalCount === 0) return 0
  return (row.remainingCount / row.totalCount) * 100
}

function getStockClass(row) {
  const percent = getStockPercent(row)
  if (percent > 50) return 'high'
  if (percent > 20) return 'medium'
  return 'low'
}

onMounted(() => {
  fetchActivity()
  fetchList()
})

async function fetchActivity() {
  const res = await getActivityDetail(activityId)
  activityName.value = res.data.name
  activityStatus.value = res.data.status
}

async function fetchList() {
  loading.value = true
  try {
    const res = await getPrizeList(activityId)
    prizeList.value = res.data
  } finally {
    loading.value = false
  }
}

function openDialog(row) {
  isEdit.value = !!row
  editId.value = row?.id || null
  if (row) {
    form.name = row.name
    form.image = row.image
    form.probability = row.probability * 100
    form.totalCount = row.totalCount
    form.sortOrder = row.sortOrder
  } else {
    form.name = ''
    form.image = ''
    form.probability = 0
    form.totalCount = 0
    form.sortOrder = prizeList.value.length
  }
  dialogVisible.value = true
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
    form.image = res.data.url
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
      activityId: Number(activityId),
      name: form.name,
      image: form.image,
      probability: form.probability / 100,
      totalCount: form.totalCount,
      sortOrder: form.sortOrder,
      status: 1
    }
    if (isEdit.value) {
      await updatePrize(editId.value, data)
      ElMessage.success('更新成功')
    } else {
      await createPrize(data)
      ElMessage.success('创建成功')
    }
    dialogVisible.value = false
    fetchList()
    
    // 检查是否可以上线
    setTimeout(checkCanOnline, 500)
  } finally {
    submitLoading.value = false
  }
}

async function checkCanOnline() {
  if (activityStatus.value === 0 && prizeList.value.length > 0 && isProbabilityValid.value) {
    try {
      await ElMessageBox.confirm(
        '奖品配置已完成，是否立即上线活动？',
        '上线确认',
        {
          confirmButtonText: '立即上线',
          cancelButtonText: '稍后再说',
          type: 'success'
        }
      )
      await handleOnlineActivity()
    } catch {
      // 用户取消
    }
  }
}

async function handleOnlineActivity() {
  try {
    await onlineActivity(activityId)
    ElMessage.success('活动已上线')
    activityStatus.value = 1
    fetchActivity()
  } catch (e) {
    ElMessage.error('上线失败')
  }
}

function openStockDialog(row) {
  stockForm.id = row.id
  stockForm.totalCount = row.totalCount
  stockForm.remainingCount = row.remainingCount
  stockForm.newTotalCount = row.totalCount
  stockDialogVisible.value = true
}

async function handleStockSubmit() {
  stockLoading.value = true
  try {
    await updatePrizeStock(stockForm.id, stockForm.newTotalCount)
    ElMessage.success('补货成功')
    stockDialogVisible.value = false
    fetchList()
  } finally {
    stockLoading.value = false
  }
}

async function handleOnline(id) {
  await onlinePrize(id)
  ElMessage.success('启用成功')
  fetchList()
}

async function handleOffline(id) {
  await offlinePrize(id)
  ElMessage.success('禁用成功')
  fetchList()
}

async function handleDelete(id) {
  await deletePrize(id)
  ElMessage.success('删除成功')
  fetchList()
}
</script>

<style scoped lang="scss">
.back-btn {
  margin-right: 8px;
  .el-icon { font-size: 20px; }
}

.probability-card {
  margin-bottom: 24px;
  
  .prob-stats {
    display: flex;
    gap: 48px;
    margin-bottom: 20px;
  }
  
  .prob-stat-item {
    display: flex;
    align-items: center;
    gap: 12px;
    
    .label {
      color: var(--text-secondary);
      font-size: 14px;
    }
    
    .value {
      font-size: 24px;
      font-weight: 700;
      color: var(--text-primary);
      
      &.warning { color: var(--warning-color); }
    }
  }
  
  .prob-visual {
    height: 12px;
    background: #e2e8f0;
    border-radius: 6px;
    overflow: hidden;
    display: flex;
    margin-bottom: 16px;
    
    .prob-segment {
      height: 100%;
      transition: width 0.3s;
      &:first-child { border-radius: 6px 0 0 6px; }
      &:last-child { border-radius: 0 6px 6px 0; }
    }
  }
  
  .prob-legend {
    display: flex;
    flex-wrap: wrap;
    gap: 16px;
  }
  
  .legend-item {
    display: flex;
    align-items: center;
    gap: 8px;
    font-size: 13px;
    
    .legend-color {
      width: 12px;
      height: 12px;
      border-radius: 3px;
    }
    
    .legend-name { color: var(--text-secondary); }
    .legend-value { font-weight: 600; color: var(--text-primary); }
  }
}

.prize-info {
  display: flex;
  align-items: center;
  gap: 16px;
  padding: 8px 0;
}

.prize-image {
  width: 64px;
  height: 64px;
  border-radius: 8px;
  flex-shrink: 0;
  background: #f8fafc;
  border: 1px solid var(--border-color);
  cursor: pointer;
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

.prize-detail {
  flex: 1;
}

.prize-name {
  font-weight: 600;
  color: var(--text-primary);
  margin-bottom: 4px;
}

.prize-id {
  font-size: 12px;
  color: var(--text-muted);
}

.stock-info {
  .stock-numbers {
    margin-bottom: 6px;
    font-size: 14px;
    
    .remaining { font-weight: 600; color: var(--text-primary); }
    .separator { color: var(--text-muted); margin: 0 4px; }
    .total { color: var(--text-secondary); }
  }
}

.probability-input {
  display: flex;
  align-items: center;
}

.stock-dialog-content {
  .current-stock {
    text-align: center;
    padding: 20px;
    background: #f8fafc;
    border-radius: 12px;
    
    .stock-label {
      color: var(--text-secondary);
      margin-bottom: 8px;
    }
    
    .stock-value {
      font-size: 36px;
      
      .remaining { font-weight: 700; color: var(--text-primary); }
      .separator { color: var(--text-muted); margin: 0 8px; }
      .total { color: var(--text-secondary); }
    }
  }
}
</style>
