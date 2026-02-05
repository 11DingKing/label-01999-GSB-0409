<template>
  <div class="profile-page">
    <div class="page-container">
      <div class="page-header">
        <h1 class="page-title">个人中心</h1>
        <p class="page-subtitle">管理您的账户信息</p>
      </div>

      <div class="profile-layout">
        <!-- 用户信息卡片 -->
        <div class="user-card">
          <div class="user-avatar-section">
            <el-avatar :src="userStore.userInfo?.avatar" :size="100" />
            <div class="user-basic">
              <h2 class="nickname">{{ userStore.userInfo?.nickname || '未设置昵称' }}</h2>
              <p class="username">@{{ userStore.userInfo?.username }}</p>
            </div>
          </div>
          <div class="user-stats">
            <div class="stat-item">
              <span class="stat-value">{{ userStore.userInfo?.phone || '-' }}</span>
              <span class="stat-label">手机号</span>
            </div>
          </div>
        </div>

        <!-- 功能区域 -->
        <div class="action-section">
          <div class="section-title">账户设置</div>
          <div class="action-list">
            <div class="action-item" @click="showEditDialog = true">
              <div class="action-icon">
                <el-icon><Edit /></el-icon>
              </div>
              <div class="action-content">
                <span class="action-title">编辑资料</span>
                <span class="action-desc">修改昵称、手机号等信息</span>
              </div>
              <el-icon class="action-arrow"><ArrowRight /></el-icon>
            </div>
            <div class="action-item" @click="router.push('/records')">
              <div class="action-icon prize">
                <el-icon><Trophy /></el-icon>
              </div>
              <div class="action-content">
                <span class="action-title">我的奖品</span>
                <span class="action-desc">查看和领取中奖记录</span>
              </div>
              <el-icon class="action-arrow"><ArrowRight /></el-icon>
            </div>
          </div>
        </div>

        <!-- 退出登录 -->
        <div class="logout-section">
          <el-button type="danger" plain size="large" class="logout-btn" @click="handleLogout">
            <el-icon><SwitchButton /></el-icon>
            退出登录
          </el-button>
        </div>
      </div>
    </div>

    <!-- 编辑资料弹窗 -->
    <el-dialog v-model="showEditDialog" title="编辑资料" width="480px" :close-on-click-modal="false">
      <el-form ref="editFormRef" :model="editForm" :rules="editRules" label-position="top" size="large">
        <el-form-item label="昵称" prop="nickname">
          <el-input v-model="editForm.nickname" placeholder="请输入昵称" maxlength="20" show-word-limit clearable />
        </el-form-item>
        <el-form-item label="手机号" prop="phone">
          <el-input v-model="editForm.phone" placeholder="请输入11位手机号" maxlength="11" clearable>
            <template #prefix>
              <span class="phone-prefix">+86</span>
            </template>
          </el-input>
          <template #error="{ error }">
            <div class="custom-error">
              <el-icon><WarningFilled /></el-icon>
              <span>{{ error }}</span>
            </div>
          </template>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showEditDialog = false" size="large">取消</el-button>
        <el-button type="primary" :loading="saving" @click="handleSave" size="large">保存修改</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, watch } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { useUserStore } from '@/stores/user'
import { updateUserInfo } from '@/api/user'

const router = useRouter()
const userStore = useUserStore()

const showEditDialog = ref(false)
const saving = ref(false)
const editFormRef = ref(null)

const editForm = reactive({
  nickname: '',
  phone: '',
})

const validatePhone = (rule, value, callback) => {
  if (!value) {
    callback()
    return
  }
  const phoneReg = /^1[3-9]\d{9}$/
  if (!phoneReg.test(value)) {
    callback(new Error('手机号格式不正确，请输入1开头的11位数字，第二位需为3-9'))
  } else {
    callback()
  }
}

const editRules = {
  nickname: [
    { max: 20, message: '昵称长度不能超过20个字符', trigger: 'blur' },
  ],
  phone: [
    { validator: validatePhone, trigger: 'blur' },
  ],
}

watch(showEditDialog, (val) => {
  if (val && userStore.userInfo) {
    editForm.nickname = userStore.userInfo.nickname || ''
    editForm.phone = userStore.userInfo.phone || ''
  }
})

async function handleSave() {
  if (!editFormRef.value) return
  
  await editFormRef.value.validate(async (valid) => {
    if (!valid) return
    
    saving.value = true
    try {
      await updateUserInfo(editForm)
      await userStore.refreshUserInfo()
      ElMessage.success('资料更新成功')
      showEditDialog.value = false
    } catch (error) {
      console.error('保存失败:', error)
    } finally {
      saving.value = false
    }
  })
}

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
.profile-page {
  min-height: 100vh;
  background: #f8fafc;
}

.page-container {
  max-width: 800px;
  margin: 0 auto;
  padding: 32px;
}

.page-header {
  margin-bottom: 32px;
  
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

.profile-layout {
  display: flex;
  flex-direction: column;
  gap: 24px;
}

.user-card {
  background: linear-gradient(135deg, #6366f1 0%, #8b5cf6 100%);
  border-radius: 20px;
  padding: 32px;
  color: #fff;
  box-shadow: 0 10px 40px rgba(99, 102, 241, 0.3);
}

.user-avatar-section {
  display: flex;
  align-items: center;
  gap: 24px;
  margin-bottom: 24px;
  padding-bottom: 24px;
  border-bottom: 1px solid rgba(255, 255, 255, 0.2);
  
  .el-avatar {
    border: 4px solid rgba(255, 255, 255, 0.3);
  }
  
  .user-basic {
    .nickname {
      font-size: 24px;
      font-weight: 700;
      margin-bottom: 4px;
    }
    
    .username {
      font-size: 14px;
      opacity: 0.8;
    }
  }
}

.user-stats {
  display: flex;
  gap: 32px;
  
  .stat-item {
    display: flex;
    flex-direction: column;
    gap: 4px;
    
    .stat-value {
      font-size: 18px;
      font-weight: 600;
    }
    
    .stat-label {
      font-size: 13px;
      opacity: 0.8;
    }
  }
}

.action-section {
  background: #fff;
  border-radius: 16px;
  padding: 24px;
  box-shadow: var(--shadow);
  
  .section-title {
    font-size: 16px;
    font-weight: 600;
    color: var(--text-primary);
    margin-bottom: 16px;
    padding-left: 4px;
  }
}

.action-list {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.action-item {
  display: flex;
  align-items: center;
  gap: 16px;
  padding: 16px;
  border-radius: 12px;
  cursor: pointer;
  transition: all 0.2s;
  
  &:hover {
    background: #f8fafc;
  }
  
  .action-icon {
    width: 48px;
    height: 48px;
    border-radius: 12px;
    background: linear-gradient(135deg, #e0e7ff 0%, #c7d2fe 100%);
    display: flex;
    align-items: center;
    justify-content: center;
    
    .el-icon {
      font-size: 24px;
      color: #6366f1;
    }
    
    &.prize {
      background: linear-gradient(135deg, #fef3c7 0%, #fde68a 100%);
      
      .el-icon {
        color: #f59e0b;
      }
    }
  }
  
  .action-content {
    flex: 1;
    display: flex;
    flex-direction: column;
    gap: 4px;
    
    .action-title {
      font-size: 15px;
      font-weight: 600;
      color: var(--text-primary);
    }
    
    .action-desc {
      font-size: 13px;
      color: var(--text-secondary);
    }
  }
  
  .action-arrow {
    color: var(--text-muted);
    font-size: 18px;
  }
}

.logout-section {
  .logout-btn {
    width: 100%;
    height: 52px;
    font-size: 16px;
    border-radius: 12px;
  }
}

.phone-prefix {
  color: var(--text-secondary);
  font-size: 14px;
}

.custom-error {
  display: flex;
  align-items: center;
  gap: 4px;
  color: var(--error-color);
  font-size: 12px;
  margin-top: 4px;
  
  .el-icon {
    font-size: 14px;
  }
}

:deep(.el-dialog) {
  border-radius: 16px;
  
  .el-dialog__header {
    padding: 20px 24px;
    border-bottom: 1px solid var(--border-color);
  }
  
  .el-dialog__body {
    padding: 24px;
  }
  
  .el-dialog__footer {
    padding: 16px 24px;
    border-top: 1px solid var(--border-color);
  }
}

@media (max-width: 768px) {
  .page-container {
    padding: 16px;
  }
  
  .user-avatar-section {
    flex-direction: column;
    text-align: center;
  }
}
</style>
