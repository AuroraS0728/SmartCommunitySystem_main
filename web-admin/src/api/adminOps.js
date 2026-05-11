import request from '@/utils/request'

export const getWorkOrders = (params) => request.get('/repair/page', { params })
export const getWorkOrderDetail = (id) => request.get(`/repair/${id}`)
export const autoAssignWorkOrder = (orderId) => request.post(`/repair/autoAssign/${orderId}`)
export const getWorkers = () => request.get('/worker/list')

export const getUsers = (params) => request.get('/user/page', { params })
export const getCreditLogs = (params) => request.get('/credit/logs', { params })

export const getTasks = (params) => request.get('/tasks', { params })
export const completeTask = (id) => request.put(`/tasks/${id}/complete`)

export const getRecommendRules = () => request.get('/recommend/rules')
export const saveRecommendRules = (data) => request.put('/recommend/rules', data)
