const { request } = require("../../api/request")

Page({
  data: {
    points: 0,
    loadingPoints: false,
    guestMode: false
  },

  onShow() {
    const app = getApp()
    const guestMode = !!app.globalData.guestMode
    this.setData({ guestMode })
    if (guestMode) {
      this.setData({ points: 0, loadingPoints: false })
      return
    }
    this.loadPoints()
  },

  async loadPoints() {
    this.setData({ loadingPoints: true })
    try {
      const data = await request({ url: "/points/balance" })
      this.setData({ points: Number(data?.points || 0) })
    } catch (error) {
      wx.showToast({ title: error?.message || "积分加载失败", icon: "none" })
    } finally {
      this.setData({ loadingPoints: false })
    }
  },

  ensureLoginAndNavigate(event) {
    const url = event.currentTarget.dataset.url
    if (!getApp().requireFeatureLogin()) return
    wx.navigateTo({ url })
  }
})
