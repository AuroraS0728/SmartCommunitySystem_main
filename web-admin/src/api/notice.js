import request from '@/utils/request'

export const getNoticeList = () => request.get('/notice/list')
export const publishNotice = (data) => request.post('/notice/publish', data)
export const updateNotice = (id, data) => request.put(`/notice/${id}`, data)
export const deleteNotice = (id) => request.delete(`/notice/${id}`)
