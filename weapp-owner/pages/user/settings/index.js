const app = getApp()

Page({
  goAccountSecurity() {
    if (!app.requireFeatureLogin()) return
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
  },

  goOwnerLogin() {
    app.gotoLogin(1)
  },

  goSecurityLogin() {
    app.gotoLogin(2)
  },

  goWorkerLogin() {
    app.gotoLogin(3)
  }
})
