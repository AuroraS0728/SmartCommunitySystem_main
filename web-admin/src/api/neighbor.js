import request from '@/utils/request'

export const getForumList = (params) => request.get('/neighbor/forum/list', { params })

export const getSecondHandList = (params) => request.get('/neighbor/second-hand/list', { params })
export const approveSecondHandImageAudit = (id) =>
  request.post(`/neighbor/admin/second-hand/${id}/image-audit/approve`)
export const rejectSecondHandImageAudit = (id) =>
  request.post(`/neighbor/admin/second-hand/${id}/image-audit/reject`)
