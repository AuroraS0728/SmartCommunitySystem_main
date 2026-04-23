const app = getApp()

Page({
  goAccountSecurity() {
    wx.navigateTo({ url: "/pages/user/settings/account-security" })
  },

  onLogout() {
    wx.showModal({
      title: "退出登录",
      content: "确认退出登录吗？",
      success: ({ confirm }) => {
        if (!confirm) return
        app.logout()
      }
    })
  }
})
