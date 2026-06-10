import request from '@/utils/request'

export const getBills = (params) => request.get('/fee/bills', { params })
export const getPaymentSubjects = (params) => request.get('/fee/subjects', { params })
export const getCollectionRate = (params) => request.get('/fee/collection-rate', { params })
export const payBill = (data) => request.post('/fee/pay', data)
export const createBill = (data) => request.post('/fee/bill', data)
export const updateBill = (id, data) => request.put(`/fee/bill/${id}`, data)
export const deleteBill = (id) => request.delete(`/fee/bill/${id}`)
export const generateBills = (data) => request.post('/fee/bill/generate', data)
export const applyAnnualDiscount = (data) => request.post('/fee/bill/annual-discount', data)
