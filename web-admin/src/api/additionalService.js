import request from '@/utils/request'

export const getAdditionalServiceOrders = (params) => request.get('/additional-services/admin/orders', { params })
export const updateAdditionalServiceOrderStatus = (id, status) =>
  request.post(`/additional-services/admin/orders/${id}/status`, { status })
