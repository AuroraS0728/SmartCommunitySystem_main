const { request } = require("../../api/request")

Page({
  data: {
    user: null,
    points: 0
  },

  onShow() {
    this.loadUser()
  },

  async loadUser() {
    try {
      const me = await request({ url: "/user/me" })
      this.setData({
        user: me,
        points: Number(me?.points || 0)
      })
    } catch (error) {
      wx.showToast({ title: error?.message || "加载失败", icon: "none" })
    }
  },

  goFeeBills() {
    wx.navigateTo({ url: "/pages/points/bills/index" })
  },

  goInvite() {
    wx.navigateTo({ url: "/pages/door/invite" })
  },

  goRepair() {
    wx.navigateTo({ url: "/pages/repair/submit" })
  },

  goRentSale() {
    wx.navigateTo({ url: "/pages/neighbor/secondhand/list" })
  },

  goService() {
    wx.switchTab({ url: "/pages/service/service" })
  }
})
