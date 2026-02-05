<template>
  <div class="register-page">
    <div class="register-wrapper">
      <!-- 左侧装饰区 -->
      <div class="register-banner">
        <div class="banner-content">
          <div class="banner-icon">🎁</div>
          <h1 class="banner-title">加入我们</h1>
          <p class="banner-desc">注册成为会员，开启好运之旅</p>
          <div class="banner-steps">
            <div class="step-item">
              <div class="step-num">1</div>
              <span>注册账号</span>
            </div>
            <div class="step-line"></div>
            <div class="step-item">
              <div class="step-num">2</div>
              <span>参与活动</span>
            </div>
            <div class="step-line"></div>
            <div class="step-item">
              <div class="step-num">3</div>
              <span>赢取奖品</span>
            </div>
          </div>
        </div>
      </div>
      
      <!-- 右侧注册表单 -->
      <div class="register-form-wrapper">
        <div class="register-container">
          <div class="register-header">
            <h2>创建账号</h2>
            <p>填写以下信息完成注册</p>
          </div>

          <el-form
            ref="formRef"
            :model="form"
            :rules="rules"
            class="register-form"
            @submit.prevent="handleRegister"
            size="large"
            label-position="top"
          >
            <el-form-item prop="username" label="用户名">
              <el-input
                v-model="form.username"
                placeholder="4-20位字母、数字或下划线"
                prefix-icon="User"
                clearable
              />
            </el-form-item>

            <el-form-item prop="password" label="密码">
              <el-input
                v-model="form.password"
                type="password"
                placeholder="6-20位密码"
                prefix-icon="Lock"
                show-password
              />
            </el-form-item>

            <el-form-item prop="confirmPassword" label="确认密码">
              <el-input
                v-model="form.confirmPassword"
                type="password"
                placeholder="请再次输入密码"
                prefix-icon="Lock"
                show-password
              />
            </el-form-item>

            <el-form-item prop="nickname" label="昵称（选填）">
              <el-input
                v-model="form.nickname"
                placeholder="给自己取个昵称吧"
                prefix-icon="UserFilled"
                clearable
              />
            </el-form-item>

            <el-form-item prop="phone" label="手机号（选填）">
              <el-input
                v-model="form.phone"
                placeholder="请输入11位手机号"
                prefix-icon="Phone"
                clearable
                maxlength="11"
              />
            </el-form-item>

            <el-form-item>
              <el-button
                type="primary"
                :loading="loading"
                class="register-btn"
                @click="handleRegister"
              >
                {{ loading ? '注册中...' : '立即注册' }}
              </el-button>
            </el-form-item>
          </el-form>

          <div class="register-footer">
            <span>已有账号？</span>
            <router-link to="/login">立即登录</router-link>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { useUserStore } from '@/stores/user'

const router = useRouter()
const userStore = useUserStore()

const formRef = ref(null)
const loading = ref(false)

const form = reactive({
  username: '',
  password: '',
  confirmPassword: '',
  nickname: '',
  phone: '',
})

const validateConfirmPassword = (rule, value, callback) => {
  if (value !== form.password) {
    callback(new Error('两次输入的密码不一致'))
  } else {
    callback()
  }
}

const validatePhone = (rule, value, callback) => {
  if (!value) {
    callback()
    return
  }
  const phoneReg = /^1[3-9]\d{9}$/
  if (!phoneReg.test(value)) {
    callback(new Error('请输入正确的11位手机号码，需以1开头，第二位为3-9'))
  } else {
    callback()
  }
}

const rules = {
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' },
    { min: 4, max: 20, message: '用户名长度需在4-20个字符之间', trigger: 'blur' },
    { pattern: /^[a-zA-Z0-9_]+$/, message: '用户名只能包含字母、数字和下划线', trigger: 'blur' },
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, max: 20, message: '密码长度需在6-20个字符之间', trigger: 'blur' },
  ],
  confirmPassword: [
    { required: true, message: '请确认密码', trigger: 'blur' },
    { validator: validateConfirmPassword, trigger: 'blur' },
  ],
  nickname: [
    { max: 20, message: '昵称长度不能超过20个字符', trigger: 'blur' },
  ],
  phone: [
    { validator: validatePhone, trigger: 'blur' },
  ],
}

async function handleRegister() {
  if (!formRef.value) return
  
  await formRef.value.validate(async (valid) => {
    if (!valid) return
    
    loading.value = true
    try {
      await userStore.register({
        username: form.username,
        password: form.password,
        nickname: form.nickname || undefined,
        phone: form.phone || undefined,
      })
      ElMessage.success('注册成功，欢迎加入！')
      router.push('/home')
    } catch (error) {
      console.error('注册失败:', error)
    } finally {
      loading.value = false
    }
  })
}
</script>

<style lang="scss" scoped>
.register-page {
  min-height: 100vh;
  background: #f8fafc;
}

.register-wrapper {
  display: flex;
  min-height: 100vh;
}

.register-banner {
  flex: 1;
  background: linear-gradient(135deg, #10b981 0%, #059669 100%);
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
    right: -50%;
    width: 200%;
    height: 200%;
    background: radial-gradient(circle, rgba(255,255,255,0.1) 0%, transparent 60%);
    animation: rotate 30s linear infinite reverse;
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

.banner-steps {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 16px;
  
  .step-item {
    display: flex;
    flex-direction: column;
    align-items: center;
    gap: 8px;
    
    .step-num {
      width: 40px;
      height: 40px;
      border-radius: 50%;
      background: rgba(255,255,255,0.2);
      display: flex;
      align-items: center;
      justify-content: center;
      font-size: 18px;
      font-weight: 700;
    }
    
    span {
      font-size: 14px;
    }
  }
  
  .step-line {
    width: 40px;
    height: 2px;
    background: rgba(255,255,255,0.3);
    margin-bottom: 28px;
  }
}

.register-form-wrapper {
  width: 560px;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 48px;
  background: #fff;
  overflow-y: auto;
}

.register-container {
  width: 100%;
  max-width: 400px;
}

.register-header {
  margin-bottom: 32px;

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

.register-form {
  .el-form-item {
    margin-bottom: 20px;
    
    :deep(.el-form-item__label) {
      font-weight: 500;
      color: var(--text-primary);
      padding-bottom: 6px;
    }
  }

  .el-input {
    --el-input-height: 44px;
  }

  .register-btn {
    width: 100%;
    height: 48px;
    font-size: 16px;
    font-weight: 600;
    border-radius: 10px;
    background: linear-gradient(135deg, #10b981 0%, #059669 100%);
    border: none;
    margin-top: 8px;
    
    &:hover {
      background: linear-gradient(135deg, #059669 0%, #047857 100%);
    }
  }
}

.register-footer {
  text-align: center;
  font-size: 14px;
  color: var(--text-secondary);
  margin-top: 24px;

  a {
    color: #10b981;
    font-weight: 500;
    margin-left: 4px;

    &:hover {
      text-decoration: underline;
    }
  }
}

@media (max-width: 1024px) {
  .register-banner {
    display: none;
  }
  
  .register-form-wrapper {
    width: 100%;
  }
}
</style>
