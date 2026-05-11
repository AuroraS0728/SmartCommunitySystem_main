import request from '@/utils/request'

export const getEmergencyKeywords = (params) => request.get('/emergency-keyword/list', { params })
export const createEmergencyKeyword = (data) => request.post('/emergency-keyword', data)
export const updateEmergencyKeyword = (id, data) => request.put(`/emergency-keyword/${id}`, data)
export const deleteEmergencyKeyword = (id) => request.delete(`/emergency-keyword/${id}`)

export const analyzeComplaint = (id, data) => request.post(`/complaint/${id}/analyze`, data || {})

export const getCreditLogs = (params) => request.get('/credit/logs', { params })
export const adjustCredit = (data) => request.post('/credit/adjust', data)

export const getPaymentReminders = (params) => request.get('/payment-reminder/list', { params })
export const createPaymentReminder = (data) => request.post('/payment-reminder', data)
export const generatePaymentReminders = () => request.post('/payment-reminder/generate')
export const sendPaymentReminder = (id) => request.post(`/payment-reminder/${id}/send`)
export const deletePaymentReminder = (id) => request.delete(`/payment-reminder/${id}`)

export const getPropertyTasks = (params) => request.get('/property-task/list', { params })
export const createPropertyTask = (data) => request.post('/property-task', data)
export const updatePropertyTask = (id, data) => request.put(`/property-task/${id}`, data)
export const completePropertyTask = (id) => request.post(`/property-task/${id}/complete`)
export const deletePropertyTask = (id) => request.delete(`/property-task/${id}`)

export const updateRepairPriority = (id, data) => request.post(`/repair/${id}/priority`, data)
export const scanRepairSla = () => request.post('/repair/sla/scan')
