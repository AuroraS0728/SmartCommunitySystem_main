import request from '@/utils/request'

export const getVisitorRecords = () => request.get('/access/visitor-records')
export const getVisitorBlacklist = () => request.get('/access/blacklist')
export const addVisitorBlacklist = (data) => request.post('/access/blacklist', data)
export const removeVisitorBlacklist = (id) => request.delete(`/access/blacklist/${id}`)
