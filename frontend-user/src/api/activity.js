import request from './request'

// 获取活动列表
export function getActivityList() {
  return request.get('/activity/list')
}

// 获取活动详情
export function getActivityDetail(id) {
  return request.get(`/activity/${id}`)
}

// 获取活动奖品列表
export function getActivityPrizes(id) {
  return request.get(`/activity/${id}/prizes`)
}
