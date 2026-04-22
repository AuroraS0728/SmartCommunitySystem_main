import axios from 'axios'

const baseURL = (import.meta.env.VITE_API_BASE_URL || '/api').trim()

const request = axios.create({
  baseURL,
  timeout: 15000
})

request.interceptors.request.use((config) => {
  const token = localStorage.getItem('token')
  if (token && !config.url.includes('/auth/')) {
    config.headers.Authorization = `Bearer ${token}`
  }
  return config
})

request.interceptors.response.use(
  (res) => {
    const data = res.data
    if (data.code !== 200) {
      return Promise.reject(new Error(data.message || '璇锋眰澶辫触'))
    }
    return data
  },
  (err) => Promise.reject(err)
)

export default request
