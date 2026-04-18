import request from '@/utils/request'

export const getOverview = () => request.get('/statistics/overview')
