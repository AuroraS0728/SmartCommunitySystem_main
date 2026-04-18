import request from '@/utils/request'

export const registerWorkerFace = (workerId, imageBase64) =>
  request.post('/face/register', { workerId, imageBase64 })

export const getWorkerFaceStatus = (workerId) =>
  request.get('/face/status', { params: { workerId } })
