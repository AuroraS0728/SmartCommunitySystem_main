import request from '@/utils/request'

export const rechargePoints = (data) => request.post('/points/recharge', data)
export const consumePoints = (data) => request.post('/points/consume', data)
export const getPointsBalance = () => request.get('/points/balance')
export const getPointsRecords = () => request.get('/points/records')
export const getRechargeRecords = (params) => request.get('/points/recharge-records', { params })
