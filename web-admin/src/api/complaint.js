import request from '@/utils/request'

export const getComplaintList = (params) => request.get('/complaint/list', { params })
export const replyComplaint = (id, reply) => request.post(`/complaint/${id}/reply`, null, { params: { reply } })
