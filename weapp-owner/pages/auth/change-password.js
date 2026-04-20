const app = getApp()
const { request } = require('../../api/request')

const PASSWORD_PATTERN = /^[A-Za-z0-9]{6,20}$/

Page({
  data: {
    oldPassword: '',
    newPassword: '',
    confirmPassword: '',
    loading: false
  },

  onShow() {
    if (!app.globalData.token) {
      app.gotoLogin()
    }
  },

  onOldInput(e) {
    this.setData({ oldPassword: (e.detail.value || '').trim() })
  },

  onNewInput(e) {
    this.setData({ newPassword: (e.detail.value || '').trim() })
  },

  onConfirmInput(e) {
    this.setData({ confirmPassword: (e.detail.value || '').trim() })
  },

  async submit() {
    if (this.data.loading) return
    const oldPassword = this.data.oldPassword
    const newPassword = this.data.newPassword
    const confirmPassword = this.data.confirmPassword

    if (!oldPassword) {
      wx.showToast({ title: '请输入旧密码', icon: 'none' })
      return
    }
    if (!PASSWORD_PATTERN.test(newPassword)) {
      wx.showToast({ title: '新密码需6-20位字母或数字', icon: 'none' })
      return
    }
    if (newPassword !== confirmPassword) {
      wx.showToast({ title: '两次输入的新密码不一致', icon: 'none' })
      return
    }
    if (oldPassword === newPassword) {
      wx.showToast({ title: '新密码不能和旧密码相同', icon: 'none' })
      return
    }

    this.setData({ loading: true })
    try {
      await request({
        url: '/auth/change-password',
        method: 'POST',
        data: { oldPassword, newPassword }
      })
      app.setMustChangePassword(false)
      wx.showToast({ title: '密码修改成功', icon: 'success' })
      setTimeout(() => {
        app.goHome()
      }, 300)
    } catch (error) {
      wx.showToast({ title: error?.message || '修改失败', icon: 'none' })
    } finally {
      this.setData({ loading: false })
    }
  }
})
