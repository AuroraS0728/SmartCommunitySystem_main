import request from '@/utils/request'

export const accountLogin = (data) => request.post('/auth/account-login', data)
export const logout = () => request.post('/auth/logout')
