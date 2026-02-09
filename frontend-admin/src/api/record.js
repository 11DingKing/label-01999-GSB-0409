import request from './request'

// 获取所有中奖记录
export function getAllRecords(activityId) {
  return request.get('/admin/record/list', { params: { activityId } })
}

// 获取中奖统计
export function getRecordStats(activityId) {
  return request.get('/admin/record/stats', { params: { activityId } })
}
