import request from '@/utils/request'

export const getWorkerList = () => request.get('/worker/list')
export const getWorkerStaffingList = () => request.get('/worker/staffing/list')
export const saveWorkerStaffing = (data) => request.post('/worker/staffing/save', data)
export const getWorkerPerformance = (workerId) => request.get('/worker/performance', { params: { workerId } })
export const addWorker = (data) => request.post('/worker/add', data)
export const updateWorker = (id, data) => request.put(`/worker/${id}`, data)
export const deleteWorker = (id) => request.delete(`/worker/${id}`)
