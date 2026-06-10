import request from '@/utils/request'

export const getHouseList = (params) => request.get('/house/list', { params })
export const addHouse = (data) => request.post('/house/add', data)
export const updateHouse = (id, data) => request.put(`/house/${id}`, data)
export const deleteHouse = (id) => request.delete(`/house/${id}`)
export const importHouses = (data) => request.post('/house/import', data, {
  headers: { 'Content-Type': 'multipart/form-data' }
})
export const exportHouses = (params) => request.get('/house/export', { params, responseType: 'blob' })
