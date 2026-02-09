<template>
  <div class="login-container">
    <div class="login-bg">
      <div class="bg-shape shape-1"></div>
      <div class="bg-shape shape-2"></div>
      <div class="bg-shape shape-3"></div>
    </div>
    
    <div class="login-box">
      <div class="login-header">
        <div class="logo">🎰</div>
        <h2 class="title">抽奖系统管理后台</h2>
        <p class="subtitle">请使用管理员账号登录</p>
      </div>
      
      <el-form ref="formRef" :model="form" :rules="rules" class="login-form">
        <el-form-item prop="username">
          <el-input 
            v-model="form.username" 
            placeholder="用户名" 
            size="large"
            :prefix-icon="User"
          />
        </el-form-item>
        
        <el-form-item prop="password">
          <el-input 
            v-model="form.password" 
            type="password" 
            placeholder="密码" 
            size="large"
            :prefix-icon="Lock"
            show-password 
            @keyup.enter="handleLogin" 
          />
        </el-form-item>
        
        <el-form-item>
          <el-button 
            type="primary" 
            size="large" 
            :loading="loading" 
            class="login-btn"
            @click="handleLogin"
          >
            {{ loading ? '登录中...' : '登 录' }}
          </el-button>
        </el-form-item>
      </el-form>
      
      <div class="login-footer">
        <el-divider>
          <span class="divider-text">测试账号</span>
        </el-divider>
        <div class="test-account">
          <el-tag type="info" effect="plain">admin / admin123</el-tag>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { User, Lock } from '@element-plus/icons-vue'
import { login } from '../api/auth'
import { useUserStore } from '../stores/user'

const router = useRouter()
const userStore = useUserStore()
const formRef = ref()
const loading = ref(false)

const form = reactive({
  username: '',
  password: ''
})

const rules = {
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }]
}

async function handleLogin() {
  await formRef.value.validate()
  loading.value = true
  try {
    const res = await login(form)
    if (res.data.role !== 'ADMIN') {
      ElMessage.error('该账户没有管理员权限')
      return
    }
    userStore.setToken(res.data.token)
    userStore.setUserInfo(res.data)
    ElMessage.success('登录成功')
    router.push('/')
  } finally {
    loading.value = false
  }
}
</script>

<style scoped lang="scss">
.login-container {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: var(--bg-gradient);
  position: relative;
  overflow: hidden;
}

.login-bg {
  position: absolute;
  inset: 0;
  overflow: hidden;
  
  .bg-shape {
    position: absolute;
    border-radius: 50%;
    background: rgba(255, 255, 255, 0.1);
    animation: float 20s infinite ease-in-out;
  }
  
  .shape-1 {
    width: 400px;
    height: 400px;
    top: -100px;
    left: -100px;
  }
  
  .shape-2 {
    width: 300px;
    height: 300px;
    bottom: -50px;
    right: -50px;
    animation-delay: -5s;
  }
  
  .shape-3 {
    width: 200px;
    height: 200px;
    top: 50%;
    right: 20%;
    animation-delay: -10s;
  }
}

@keyframes float {
  0%, 100% { transform: translateY(0) rotate(0deg); }
  50% { transform: translateY(-30px) rotate(10deg); }
}

.login-box {
  width: 420px;
  padding: 48px 40px;
  background: var(--card-bg);
  border-radius: var(--border-radius-lg);
  box-shadow: var(--shadow-xl);
  position: relative;
  z-index: 1;
}

.login-header {
  text-align: center;
  margin-bottom: 32px;
  
  .logo { font-size: 48px; margin-bottom: 16px; }
  
  .title {
    font-size: 24px;
    font-weight: 700;
    color: var(--text-primary);
    margin-bottom: 8px;
  }
  
  .subtitle {
    font-size: 14px;
    color: var(--text-secondary);
  }
}

.login-form {
  :deep(.el-input__wrapper) {
    border-radius: var(--border-radius-sm);
  }
  
  :deep(.el-input__prefix) {
    color: var(--text-muted);
  }
}

.login-btn {
  width: 100%;
  height: 48px;
  font-size: 16px;
  font-weight: 600;
  border-radius: var(--border-radius-sm);
  background: var(--bg-gradient);
  border: none;
  transition: all 0.3s;
  
  &:hover {
    transform: translateY(-2px);
    box-shadow: 0 10px 20px rgba(99, 102, 241, 0.3);
  }
}

.login-footer {
  margin-top: 24px;
  
  .divider-text {
    color: var(--text-muted);
    font-size: 12px;
  }
  
  .test-account {
    text-align: center;
  }
}
</style>
