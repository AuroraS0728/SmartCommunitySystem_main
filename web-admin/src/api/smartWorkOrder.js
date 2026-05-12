import request from '@/utils/request'

export const getSmartWorkOrders = (params) => request.get('/smart-work-orders/page', { params })
export const getSmartWorkOrderDetail = (id) => request.get(`/smart-work-orders/${id}`)
export const dispatchSmartWorkOrder = (id, data) => request.post(`/smart-work-orders/${id}/dispatch`, data)
export const updateSmartWorkOrderStatus = (id, data) => request.post(`/smart-work-orders/${id}/status`, data)
export const updateSmartWorkOrderPriority = (id, data) => request.post(`/smart-work-orders/${id}/priority`, data)
export const scanSmartWorkOrderSla = () => request.post('/smart-work-orders/sla/scan')
