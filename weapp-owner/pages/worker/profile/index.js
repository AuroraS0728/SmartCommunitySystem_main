const app = getApp()
const { request } = require("../../../api/request")

Page({
  data: {
    user: null,
    perf: null
  },
  async onShow() {
    try {
      const user = await request({ url: "/user/me" })
      this.setData({ user: user || null })
      if (user && user.id) {
        const perf = await request({ url: "/worker/performance", data: { workerId: user.id } })
        this.setData({ perf: perf || null })
      }
    } catch (error) {
      wx.showToast({ title: error?.message || "加载失败", icon: "none" })
    }
  },
  logout() {
    app.logout({ redirect: true })
  }
})
