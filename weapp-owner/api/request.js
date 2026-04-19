const app = getApp()

function ensureToken() {
  if (app.globalData.token) {
    return Promise.resolve(app.globalData.token)
  }
  if (typeof app.ensureLogin === 'function') {
    return app.ensureLogin()
  }
  return Promise.reject(new Error('请先登录'))
}

function handleUnauthorized() {
  if (typeof app.logout === 'function') {
    app.logout({ redirect: true })
    return
  }
  wx.reLaunch({ url: '/pages/auth/login' })
}

function handleMustChangePassword() {
  if (typeof app.setMustChangePassword === 'function') {
    app.setMustChangePassword(true)
  }
  const pages = getCurrentPages()
  const current = pages[pages.length - 1]
  if (current && current.route === 'pages/auth/change-password') {
    return
  }
  wx.reLaunch({ url: '/pages/auth/change-password' })
}

function request({ url, method = 'GET', data = {}, skipAuth = false }) {
  const doRequest = () =>
    new Promise((resolve, reject) => {
      wx.request({
        url: `${app.globalData.baseUrl}${url}`,
        method,
        data,
        header: { Authorization: app.globalData.token ? `Bearer ${app.globalData.token}` : '' },
        success: (res) => {
          const body = res.data || {}
          const unauthorized = res.statusCode === 401 || body.code === 401 || body.message === 'unauthorized'
          if (unauthorized) {
            handleUnauthorized()
            reject(new Error('登录已失效，请重新登录'))
            return
          }

          const mustChangePassword =
            res.statusCode === 403 || body.code === 403 || body.message === 'must change password'
          if (mustChangePassword) {
            handleMustChangePassword()
            reject(new Error('首次登录必须先修改密码'))
            return
          }

          if (body.code === 200) {
            resolve(body.data)
            return
          }
          reject(new Error(body.message || '请求失败'))
        },
        fail: (error) => reject(error)
      })
    })

  if (skipAuth) {
    return doRequest()
  }
  return ensureToken().then(() => doRequest())
}

module.exports = { request }
