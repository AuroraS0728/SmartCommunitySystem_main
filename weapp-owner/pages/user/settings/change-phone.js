const { request } = require("../../../api/request")

Page({
  data: {
    phone: "",
    submitting: false
  },

  onPhoneInput(e) {
    this.setData({ phone: String(e.detail.value || "").trim() })
  },

  async onSubmit() {
    if (this.data.submitting) return
    const phone = this.data.phone
    if (!/^1\d{10}$/.test(phone)) {
      wx.showToast({ title: "请输入11位手机号", icon: "none" })
      return
    }

    this.setData({ submitting: true })
    try {
      await request({
        url: "/auth/bind-phone",
        method: "POST",
        data: { phone }
      })
      const app = getApp()
      app.globalData.userInfo = {
        ...(app.globalData.userInfo || {}),
        phone
      }
      wx.setStorageSync("userInfo", app.globalData.userInfo)
      wx.showToast({ title: "修改成功", icon: "success" })
      setTimeout(() => wx.navigateBack(), 280)
    } catch (error) {
      wx.showToast({ title: error?.message || "修改失败", icon: "none" })
    } finally {
      this.setData({ submitting: false })
    }
  }
})
