import request from './request'

// 获取所有活动列表
export function getActivityList() {
  return request.get('/admin/activity/list')
}

// 获取活动详情
export function getActivityDetail(id) {
  return request.get(`/admin/activity/${id}`)
}

// 创建活动
export function createActivity(data) {
  return request.post('/admin/activity', data)
}

// 更新活动
export function updateActivity(id, data) {
  return request.put(`/admin/activity/${id}`, data)
}

// 删除活动
export function deleteActivity(id) {
  return request.delete(`/admin/activity/${id}`)
}

// 上线活动
export function onlineActivity(id) {
  return request.put(`/admin/activity/${id}/online`)
}

// 下线活动
export function offlineActivity(id) {
  return request.put(`/admin/activity/${id}/offline`)
}
