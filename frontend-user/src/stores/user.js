import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { login as loginApi, register as registerApi, getUserInfo } from '@/api/user'

export const useUserStore = defineStore('user', () => {
  const token = ref('')
  const userInfo = ref(null)

  const isLoggedIn = computed(() => !!token.value)

  // 从本地存储初始化
  function initFromStorage() {
    const storedToken = localStorage.getItem('token')
    const storedUserInfo = localStorage.getItem('userInfo')
    
    if (storedToken) {
      token.value = storedToken
    }
    if (storedUserInfo) {
      try {
        userInfo.value = JSON.parse(storedUserInfo)
      } catch (e) {
        console.error('解析用户信息失败', e)
      }
    }
  }

  // 登录
  async function login(data) {
    const res = await loginApi(data)
    token.value = res.data.token
    userInfo.value = {
      id: res.data.userId,
      username: res.data.username,
      nickname: res.data.nickname,
      avatar: res.data.avatar,
    }
    
    localStorage.setItem('token', res.data.token)
    localStorage.setItem('userInfo', JSON.stringify(userInfo.value))
    
    return res
  }

  // 注册
  async function register(data) {
    const res = await registerApi(data)
    token.value = res.data.token
    userInfo.value = {
      id: res.data.userId,
      username: res.data.username,
      nickname: res.data.nickname,
      avatar: res.data.avatar,
    }
    
    localStorage.setItem('token', res.data.token)
    localStorage.setItem('userInfo', JSON.stringify(userInfo.value))
    
    return res
  }

  // 登出
  function logout() {
    token.value = ''
    userInfo.value = null
    localStorage.removeItem('token')
    localStorage.removeItem('userInfo')
  }

  // 刷新用户信息
  async function refreshUserInfo() {
    const res = await getUserInfo()
    userInfo.value = res.data
    localStorage.setItem('userInfo', JSON.stringify(userInfo.value))
  }

  return {
    token,
    userInfo,
    isLoggedIn,
    initFromStorage,
    login,
    register,
    logout,
    refreshUserInfo,
  }
})
