import request from './request'

// 执行抽奖
export function draw(activityId) {
  return request.post(`/draw/${activityId}`)
}

// 获取剩余抽奖次数
export function getRemainingDrawCount(activityId) {
  return request.get(`/draw/${activityId}/remaining`)
}
