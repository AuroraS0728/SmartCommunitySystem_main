import request from '@/utils/request'

export const getWorkerList = () => request.get('/worker/list')
export const getWorkerPerformance = (workerId) => request.get('/worker/performance', { params: { workerId } })
export const addWorker = (data) => request.post('/worker/add', data)
export const updateWorker = (id, data) => request.put(`/worker/${id}`, data)
export const deleteWorker = (id) => request.delete(`/worker/${id}`)
