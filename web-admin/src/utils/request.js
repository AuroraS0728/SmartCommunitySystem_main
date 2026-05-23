import axios from 'axios'

const baseURL = (
  import.meta.env.VITE_API_BASE_URL ||
  'https://springboot-cgsm-248671-6-1423965568.sh.run.tcloudbase.com/api'
).trim()

let redirectingToLogin = false

function clearLoginState() {
  localStorage.removeItem('token')
  localStorage.removeItem('userInfo')
}

function redirectToLogin() {
  if (redirectingToLogin) return
  redirectingToLogin = true
  const base = import.meta.env.BASE_URL || '/'
  const loginPath = `${base.replace(/\/$/, '')}/login`
  window.location.replace(loginPath)
}

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
  (err) => {
    if (err?.response?.status === 401) {
      clearLoginState()
      redirectToLogin()
      return Promise.reject(new Error('登录已失效，请重新登录'))
    }
    return Promise.reject(err)
  }
)

export default request
