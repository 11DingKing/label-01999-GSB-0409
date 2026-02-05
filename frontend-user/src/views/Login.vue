<template>
  <div class="login-page">
    <div class="login-wrapper">
      <!-- 左侧装饰区 -->
      <div class="login-banner">
        <div class="banner-content">
          <div class="banner-icon">🎰</div>
          <h1 class="banner-title">幸运抽奖平台</h1>
          <p class="banner-desc">参与精彩活动，赢取丰厚奖品</p>
          <div class="banner-features">
            <div class="feature-item">
              <el-icon><Present /></el-icon>
              <span>海量奖品</span>
            </div>
            <div class="feature-item">
              <el-icon><Timer /></el-icon>
              <span>每日抽奖</span>
            </div>
            <div class="feature-item">
              <el-icon><Trophy /></el-icon>
              <span>中奖率高</span>
            </div>
          </div>
        </div>
      </div>
      
      <!-- 右侧登录表单 -->
      <div class="login-form-wrapper">
        <div class="login-container">
          <div class="login-header">
            <h2>欢迎回来</h2>
            <p>请登录您的账号</p>
          </div>

          <el-form
            ref="formRef"
            :model="form"
            :rules="rules"
            class="login-form"
            @submit.prevent="handleLogin"
            size="large"
          >
            <el-form-item prop="username">
              <el-input
                v-model="form.username"
                placeholder="请输入用户名"
                prefix-icon="User"
                clearable
              />
            </el-form-item>

            <el-form-item prop="password">
              <el-input
                v-model="form.password"
                type="password"
                placeholder="请输入密码"
                prefix-icon="Lock"
                show-password
              />
            </el-form-item>

            <el-form-item>
              <el-button
                type="primary"
                :loading="loading"
                class="login-btn"
                @click="handleLogin"
              >
                {{ loading ? '登录中...' : '登 录' }}
              </el-button>
            </el-form-item>
          </el-form>

          <div class="login-footer">
            <span>还没有账号？</span>
            <router-link to="/register">立即注册</router-link>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import { useUserStore } from '@/stores/user'

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()

const formRef = ref(null)
const loading = ref(false)

const form = reactive({
  username: '',
  password: '',
})

const rules = {
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' },
    { min: 4, max: 20, message: '用户名长度需在4-20个字符之间', trigger: 'blur' },
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, max: 20, message: '密码长度需在6-20个字符之间', trigger: 'blur' },
  ],
}

async function handleLogin() {
  if (!formRef.value) return
  
  await formRef.value.validate(async (valid) => {
    if (!valid) return
    
    loading.value = true
    try {
      await userStore.login(form)
      ElMessage.success('登录成功，欢迎回来！')
      
      const redirect = route.query.redirect || '/home'
      router.push(redirect)
    } catch (error) {
      console.error('登录失败:', error)
    } finally {
      loading.value = false
    }
  })
}
</script>

<style lang="scss" scoped>
.login-page {
  min-height: 100vh;
  background: #f8fafc;
}

.login-wrapper {
  display: flex;
  min-height: 100vh;
}

.login-banner {
  flex: 1;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 48px;
  position: relative;
  overflow: hidden;
  
  &::before {
    content: '';
    position: absolute;
    top: -50%;
    left: -50%;
    width: 200%;
    height: 200%;
    background: radial-gradient(circle, rgba(255,255,255,0.1) 0%, transparent 60%);
    animation: rotate 30s linear infinite;
  }
}

@keyframes rotate {
  from { transform: rotate(0deg); }
  to { transform: rotate(360deg); }
}

.banner-content {
  position: relative;
  z-index: 1;
  text-align: center;
  color: #fff;
  
  .banner-icon {
    font-size: 80px;
    margin-bottom: 24px;
    animation: bounce 2s ease-in-out infinite;
  }
  
  .banner-title {
    font-size: 36px;
    font-weight: 700;
    margin-bottom: 12px;
    text-shadow: 0 2px 4px rgba(0,0,0,0.2);
  }
  
  .banner-desc {
    font-size: 18px;
    opacity: 0.9;
    margin-bottom: 48px;
  }
}

@keyframes bounce {
  0%, 100% { transform: translateY(0); }
  50% { transform: translateY(-10px); }
}

.banner-features {
  display: flex;
  gap: 32px;
  justify-content: center;
  
  .feature-item {
    display: flex;
    align-items: center;
    gap: 8px;
    padding: 12px 20px;
    background: rgba(255,255,255,0.15);
    border-radius: 24px;
    backdrop-filter: blur(10px);
    font-size: 14px;
    
    .el-icon {
      font-size: 18px;
      color: #fbbf24;
    }
  }
}

.login-form-wrapper {
  width: 520px;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 48px;
  background: #fff;
}

.login-container {
  width: 100%;
  max-width: 380px;
}

.login-header {
  margin-bottom: 40px;

  h2 {
    font-size: 28px;
    font-weight: 700;
    color: var(--text-primary);
    margin-bottom: 8px;
  }

  p {
    font-size: 15px;
    color: var(--text-secondary);
  }
}

.login-form {
  .el-form-item {
    margin-bottom: 24px;
  }

  .el-input {
    --el-input-height: 48px;
  }

  .login-btn {
    width: 100%;
    height: 48px;
    font-size: 16px;
    font-weight: 600;
    border-radius: 10px;
    background: linear-gradient(135deg, #6366f1 0%, #8b5cf6 100%);
    border: none;
    
    &:hover {
      background: linear-gradient(135deg, #4f46e5 0%, #7c3aed 100%);
    }
  }
}

.login-footer {
  text-align: center;
  font-size: 14px;
  color: var(--text-secondary);
  margin-top: 24px;

  a {
    color: var(--primary-color);
    font-weight: 500;
    margin-left: 4px;

    &:hover {
      text-decoration: underline;
    }
  }
}

@media (max-width: 1024px) {
  .login-banner {
    display: none;
  }
  
  .login-form-wrapper {
    width: 100%;
  }
}
</style>
