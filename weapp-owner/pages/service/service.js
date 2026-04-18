const app = getApp()

Page({
  data: {},
  onLoad() {},
  onLogout() {
    wx.showModal({
      title: "退出登录",
      content: "确认退出并返回登录页吗？",
      success: ({ confirm }) => {
        if (!confirm) {
          return
        }
        app.logout()
      }
    })
  }
})
