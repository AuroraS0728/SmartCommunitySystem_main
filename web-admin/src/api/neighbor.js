import request from '@/utils/request'

export const getForumList = (params) => request.get('/neighbor/forum/list', { params })
