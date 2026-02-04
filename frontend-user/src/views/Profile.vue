<template>
  <div class="profile-page">
    <div class="page-container">
      <!-- 用户信息卡片 -->
      <div class="user-card">
        <div class="user-avatar">
          <el-avatar :src="userStore.userInfo?.avatar" :size="80" />
        </div>
        <div class="user-info">
          <h2 class="nickname">{{ userStore.userInfo?.nickname || '未设置昵称' }}</h2>
          <p class="username">@{{ userStore.userInfo?.username }}</p>
        </div>
      </div>

      <!-- 功能菜单 -->
      <div class="menu-section">
        <div class="menu-item" @click="showEditDialog = true">
          <div class="menu-left">
            <el-icon><Edit /></el-icon>
            <span>编辑资料</span>
          </div>
          <el-icon><ArrowRight /></el-icon>
        </div>
        <div class="menu-item" @click="router.push('/records')">
          <div class="menu-left">
            <el-icon><Trophy /></el-icon>
            <span>我的奖品</span>
          </div>
          <el-icon><ArrowRight /></el-icon>
        </div>
      </div>

      <!-- 退出登录 -->
      <div class="logout-section">
        <el-button type="danger" plain size="large" class="logout-btn" @click="handleLogout">
          退出登录
        </el-button>
      </div>
    </div>

    <!-- 底部导航 -->
    <div class="bottom-nav">
      <div class="nav-item" @click="router.push('/home')">
        <el-icon><HomeFilled /></el-icon>
        <span>首页</span>
      </div>
      <div class="nav-item" @click="router.push('/records')">
        <el-icon><Trophy /></el-icon>
        <span>我的奖品</span>
      </div>
      <div class="nav-item active">
        <el-icon><User /></el-icon>
        <span>我的</span>
      </div>
    </div>

    <!-- 编辑资料弹窗 -->
    <el-dialog v-model="showEditDialog" title="编辑资料" width="90%" :max-width="400">
      <el-form :model="editForm" label-position="top">
        <el-form-item label="昵称">
          <el-input v-model="editForm.nickname" placeholder="请输入昵称" />
        </el-form-item>
        <el-form-item label="手机号">
          <el-input v-model="editForm.phone" placeholder="请输入手机号" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showEditDialog = false">取消</el-button>
        <el-button type="primary" :loading="saving" @click="handleSave">
          保存
        </el-button>
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

const editForm = reactive({
  nickname: '',
  phone: '',
})

watch(showEditDialog, (val) => {
  if (val && userStore.userInfo) {
    editForm.nickname = userStore.userInfo.nickname || ''
    editForm.phone = userStore.userInfo.phone || ''
  }
})

async function handleSave() {
  saving.value = true
  try {
    await updateUserInfo(editForm)
    await userStore.refreshUserInfo()
    ElMessage.success('保存成功')
    showEditDialog.value = false
  } catch (error) {
    console.error('保存失败:', error)
  } finally {
    saving.value = false
  }
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
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
}

.page-container {
  padding: 16px;
  padding-bottom: 80px;
}

.user-card {
  background: #fff;
  border-radius: 16px;
  padding: 24px;
  display: flex;
  flex-direction: column;
  align-items: center;
  margin-bottom: 24px;
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.1);

  .user-avatar {
    margin-bottom: 16px;
  }

  .user-info {
    text-align: center;

    .nickname {
      font-size: 20px;
      font-weight: 600;
      color: var(--text-primary);
      margin-bottom: 4px;
    }

    .username {
      font-size: 14px;
      color: var(--text-secondary);
    }
  }
}

.menu-section {
  background: #fff;
  border-radius: 16px;
  overflow: hidden;
  margin-bottom: 24px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.08);

  .menu-item {
    display: flex;
    align-items: center;
    justify-content: space-between;
    padding: 16px 20px;
    cursor: pointer;
    transition: background 0.3s ease;

    &:not(:last-child) {
      border-bottom: 1px solid #f0f0f0;
    }

    &:hover {
      background: #f9f9f9;
    }

    .menu-left {
      display: flex;
      align-items: center;
      gap: 12px;
      font-size: 15px;
      color: var(--text-primary);

      .el-icon {
        font-size: 20px;
        color: var(--primary-color);
      }
    }

    > .el-icon {
      color: #ccc;
    }
  }
}

.logout-section {
  .logout-btn {
    width: 100%;
    height: 48px;
    font-size: 16px;
    border-radius: 12px;
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
