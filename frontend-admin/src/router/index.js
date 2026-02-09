import { createRouter, createWebHistory } from 'vue-router'

const routes = [
  {
    path: '/login',
    name: 'Login',
    component: () => import('../views/Login.vue'),
    meta: { title: '登录' }
  },
  {
    path: '/',
    component: () => import('../views/Layout.vue'),
    redirect: '/activity',
    children: [
      {
        path: 'activity',
        name: 'Activity',
        component: () => import('../views/Activity.vue'),
        meta: { title: '活动管理' }
      },
      {
        path: 'activity/:id/prizes',
        name: 'Prize',
        component: () => import('../views/Prize.vue'),
        meta: { title: '奖品管理' }
      },
      {
        path: 'records',
        name: 'Records',
        component: () => import('../views/Records.vue'),
        meta: { title: '中奖记录' }
      }
    ]
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

router.beforeEach((to, from, next) => {
  document.title = to.meta.title ? `${to.meta.title} - 抽奖管理系统` : '抽奖管理系统'
  const token = localStorage.getItem('admin_token')
  if (to.path !== '/login' && !token) {
    next('/login')
  } else {
    next()
  }
})

export default router
