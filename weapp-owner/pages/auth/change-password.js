const app = getApp()
const { request } = require('../../api/request')

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
    if (!/^\d{6}$/.test(oldPassword) || !/^\d{6}$/.test(newPassword)) {
      wx.showToast({ title: '密码必须为6位数字', icon: 'none' })
      return
    }
    if (newPassword !== confirmPassword) {
      wx.showToast({ title: '两次输入的新密码不一致', icon: 'none' })
      return
    }
    if (oldPassword === newPassword) {
      wx.showToast({ title: '新密码不能与旧密码相同', icon: 'none' })
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
