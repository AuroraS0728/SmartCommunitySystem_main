import request from '@/utils/request'

export const getExpressPackages = (params) => request.get('/express/admin/list', { params })
export const createExpressPackage = (data) => request.post('/express/admin', data)
export const updateExpressPackage = (id, data) => request.put(`/express/admin/${id}`, data)
export const deleteExpressPackage = (id) => request.delete(`/express/admin/${id}`)
