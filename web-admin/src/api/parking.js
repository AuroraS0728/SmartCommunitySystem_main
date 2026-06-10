import request from '@/utils/request'

export const getParkingOrders = () => request.get('/fee/parking/orders')
