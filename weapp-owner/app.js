const LOGIN_PAGE = '/pages/auth/login'
const OWNER_HOME_PAGE = '/pages/index/index'
const SECURITY_HOME_PAGE = '/pages/security/home/home'
const WORKER_HOME_PAGE = '/pages/worker/home/home'

function toRole(value) {
  const role = Number(value)
  if (role === 2 || role === 3) {
    return role
  }
  return 1
}

App({
  globalData: {
    baseUrl: 'http://localhost:8080/api',
    token: wx.getStorageSync('token') || '',
    userInfo: wx.getStorageSync('userInfo') || null,
    role: toRole(wx.getStorageSync('role') || wx.getStorageSync('userInfo')?.role)
  },

  onLaunch() {
    if (this.globalData.token) {
      this.goHome()
    }
  },

  ensureLogin() {
    if (this.globalData.token) {
      return Promise.resolve(this.globalData.token)
    }
    this.gotoLogin()
    return Promise.reject(new Error('请先登录'))
  },

  loginWithAccount(account, password, role) {
    const targetRole = toRole(role)
    return new Promise((resolve, reject) => {
      wx.request({
        url: `${this.globalData.baseUrl}/auth/account-login`,
        method: 'POST',
        data: { account, password, role: targetRole },
        success: (res) => {
          const body = res.data || {}
          if (body.code !== 200 || !body.data?.token) {
            reject(new Error(body.message || '登录失败'))
            return
          }
          const userInfo = body.data.userInfo || null
          this.globalData.token = body.data.token
          this.globalData.userInfo = userInfo
          this.globalData.role = toRole(userInfo?.role || targetRole)
          wx.setStorageSync('token', this.globalData.token)
          wx.setStorageSync('userInfo', userInfo)
          wx.setStorageSync('role', this.globalData.role)
          resolve(body.data)
        },
        fail: reject
      })
    })
  },

  logout({ redirect = true } = {}) {
    this.globalData.token = ''
    this.globalData.userInfo = null
    this.globalData.role = 1
    wx.removeStorageSync('token')
    wx.removeStorageSync('userInfo')
    wx.removeStorageSync('role')
    if (redirect) {
      this.gotoLogin()
    }
  },

  gotoLogin() {
    const pages = getCurrentPages()
    const current = pages[pages.length - 1]
    if (current && `/${current.route}` === LOGIN_PAGE) {
      return
    }
    wx.reLaunch({ url: LOGIN_PAGE })
  },

  goHome() {
    let homePage = OWNER_HOME_PAGE
    if (this.globalData.role === 2) {
      homePage = SECURITY_HOME_PAGE
    } else if (this.globalData.role === 3) {
      homePage = WORKER_HOME_PAGE
    }
    const pages = getCurrentPages()
    const current = pages[pages.length - 1]
    if (current && `/${current.route}` === homePage) {
      return
    }
    wx.reLaunch({ url: homePage })
  }
})
