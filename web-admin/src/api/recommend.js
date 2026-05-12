import request from '@/utils/request'

export const getRecommendRules = () => request.get('/recommend/rules')
export const createRecommendRule = (data) => request.post('/recommend/rules', data)
export const updateRecommendRule = (id, data) => request.post(`/recommend/rules/${id}`, data)
export const deleteRecommendRule = (id) => request.delete(`/recommend/rules/${id}`)
