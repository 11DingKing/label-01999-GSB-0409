import request from './request'

export function login(data) {
  return request.post('/user/login', data)
}

export function getUserInfo() {
  return request.get('/user/info')
}
