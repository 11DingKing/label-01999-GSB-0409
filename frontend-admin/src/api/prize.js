import request from './request'

// 获取活动的所有奖品
export function getPrizeList(activityId) {
  return request.get(`/admin/prize/activity/${activityId}`)
}

// 创建奖品
export function createPrize(data) {
  return request.post('/admin/prize', data)
}

// 更新奖品
export function updatePrize(id, data) {
  return request.put(`/admin/prize/${id}`, data)
}

// 删除奖品
export function deletePrize(id) {
  return request.delete(`/admin/prize/${id}`)
}

// 上线奖品
export function onlinePrize(id) {
  return request.put(`/admin/prize/${id}/online`)
}

// 下线奖品
export function offlinePrize(id) {
  return request.put(`/admin/prize/${id}/offline`)
}

// 更新奖品库存
export function updatePrizeStock(id, totalCount) {
  return request.put(`/admin/prize/${id}/stock`, null, { params: { totalCount } })
}
