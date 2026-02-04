import request from './request'

// 获取我的中奖记录
export function getMyRecords() {
  return request.get('/record/my')
}

// 领取奖品
export function receivePrize(id) {
  return request.post(`/record/${id}/receive`)
}

// 获取最新中奖滚动列表
export function getLatestWinners() {
  return request.get('/record/latest')
}
