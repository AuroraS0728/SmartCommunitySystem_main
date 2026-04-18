const app = getApp()

Page({
  data: {
    account: "",
    password: "",
    loading: false
  },
  onShow() {
    if (app.globalData.token) {
      app.goHome()
    }
  },
  onAccountInput(e) {
    this.setData({ account: e.detail.value.trim() })
  },
  onPasswordInput(e) {
    this.setData({ password: e.detail.value.trim() })
  },
  submit() {
    const { account, password, loading } = this.data
    if (loading) {
      return
    }
    if (!account || !password) {
      wx.showToast({ title: "请输入账号和密码", icon: "none" })
      return
    }
    this.setData({ loading: true })
    app.loginWithAccount(account, password)
      .then(() => {
        wx.showToast({ title: "登录成功", icon: "success" })
        app.goHome()
      })
      .catch((error) => {
        wx.showToast({ title: error?.message || "登录失败", icon: "none" })
      })
      .finally(() => {
        this.setData({ loading: false })
      })
  }
})
