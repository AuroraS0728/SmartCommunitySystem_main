const { request } = require("../../../api/request")

Page({
  data: {
    phone: ""
  },

  onShow() {
    this.loadMe()
  },

  async loadMe() {
    try {
      const me = await request({ url: "/user/me" })
      this.setData({ phone: me?.phone || "" })
    } catch (error) {
      wx.showToast({ title: error?.message || "加载失败", icon: "none" })
    }
  },

  goChangePhone() {
    wx.navigateTo({ url: "/pages/user/settings/change-phone" })
  },

  goChangePassword() {
    wx.navigateTo({ url: "/pages/auth/change-password" })
  }
})
