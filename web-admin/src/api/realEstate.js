import request from '@/utils/request'

export const getRealEstateArchive = (params) => request.get('/real-estate/archive', { params })
