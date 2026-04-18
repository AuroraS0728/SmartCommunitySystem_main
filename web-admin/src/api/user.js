import request from '@/utils/request'

export const getMe = () => request.get('/user/me')
export const getUsers = (params) => request.get('/user/page', { params })
