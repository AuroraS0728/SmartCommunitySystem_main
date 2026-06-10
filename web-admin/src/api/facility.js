import request from '@/utils/request'

export const getFacilities = (params) => request.get('/facility/admin/list', { params })
export const createFacility = (data) => request.post('/facility/admin', data)
export const updateFacility = (id, data) => request.put(`/facility/admin/${id}`, data)
export const deleteFacility = (id) => request.delete(`/facility/admin/${id}`)
