const LOGIN_PAGE = "/pages/auth/login"
const HOME_PAGE = "/pages/task/list"
const WORKER_ROLE = 3

App({
  globalData: {
    baseUrl: "http://localhost:8080/api",
    token: wx.getStorageSync("token") || "",
    userInfo: wx.getStorageSync("userInfo") || null,
    role: WORKER_ROLE
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
    return Promise.reject(new Error("请先登录"))
  },
  loginWithAccount(account, password) {
    return new Promise((resolve, reject) => {
      wx.request({
        url: `${this.globalData.baseUrl}/auth/account-login`,
        method: "POST",
        data: { account, password, role: WORKER_ROLE },
        success: (res) => {
          const body = res.data || {}
          if (body.code !== 200 || !body.data?.token) {
            reject(new Error(body.message || "登录失败"))
            return
          }
          this.globalData.token = body.data.token
          this.globalData.userInfo = body.data.userInfo || null
          wx.setStorageSync("token", this.globalData.token)
          wx.setStorageSync("userInfo", this.globalData.userInfo)
          resolve(body.data)
        },
        fail: reject
      })
    })
  },
  logout({ redirect = true } = {}) {
    this.globalData.token = ""
    this.globalData.userInfo = null
    wx.removeStorageSync("token")
    wx.removeStorageSync("userInfo")
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
    const pages = getCurrentPages()
    const current = pages[pages.length - 1]
    if (current && `/${current.route}` === HOME_PAGE) {
      return
    }
    wx.reLaunch({ url: HOME_PAGE })
  }
})
