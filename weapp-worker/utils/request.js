const app = getApp()

function ensureToken() {
  if (app.globalData.token) {
    return Promise.resolve(app.globalData.token)
  }
  if (typeof app.ensureLogin === "function") {
    return app.ensureLogin()
  }
  return Promise.reject(new Error("请先登录"))
}

function handleUnauthorized() {
  if (typeof app.logout === "function") {
    app.logout({ redirect: true })
    return
  }
  wx.reLaunch({ url: "/pages/auth/login" })
}

module.exports = {
  request: ({ url, method = "GET", data = {} }) =>
    ensureToken().then(
      () =>
        new Promise((resolve, reject) =>
          wx.request({
            url: `${app.globalData.baseUrl}${url}`,
            method,
            data,
            header: { Authorization: app.globalData.token ? `Bearer ${app.globalData.token}` : "" },
            success: (res) => {
              const body = res.data || {}
              const unauthorized = res.statusCode === 401 || body.code === 401 || body.message === "unauthorized"
              if (unauthorized) {
                handleUnauthorized()
                reject(new Error("登录已失效，请重新登录"))
                return
              }
              if (body.code === 200) {
                resolve(body.data)
                return
              }
              reject(new Error(body.message || "请求失败"))
            },
            fail: (error) => reject(error)
          })
        )
    )
}
