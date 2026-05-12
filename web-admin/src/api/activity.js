import request from '@/utils/request'

export const getAdminActivities = (params) => request.get('/activity/admin/list', { params })
export const createActivity = (data) => request.post('/activity/admin', data)
export const updateActivity = (id, data) => request.put(`/activity/admin/${id}`, data)
export const getActivityRegistrations = (id) => request.get(`/activity/admin/${id}/registrations`)
export const reviewActivityRegistration = (id, data) =>
  request.post(`/activity/admin/registrations/${id}/review`, data)

export const uploadActivityAsset = (file) => {
  const formData = new FormData()
  formData.append('file', file)
  return request.post('/activity/admin/assets', formData, {
    headers: { 'Content-Type': 'multipart/form-data' }
  })
}
