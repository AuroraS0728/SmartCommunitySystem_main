const app = getApp()

Page({
  data: {
    user: wx.getStorageSync('userInfo') || null
  },

  onShow() {
    this.setData({ user: wx.getStorageSync('userInfo') || null })
  },

  logout() {
    app.logout({ redirect: true })
  }
})
