import request from '@/utils/request'

export const getOperationLogs = (params) => request.get('/system/logs', { params })
