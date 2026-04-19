const app = getApp()
const { request } = require('../../api/request')

Page({
  data: {
    points: 0
  },

  onShow() {
    this.loadPoints()
  },

  async loadPoints() {
    try {
      const data = await request({ url: '/points/balance' })
      this.setData({ points: Number(data?.points || 0) })
    } catch (error) {
      wx.showToast({ title: error?.message || '积分加载失败', icon: 'none' })
    }
  },

  onLogout() {
    wx.showModal({
      title: '退出登录',
      content: '确认退出并返回登录页面吗？',
      success: ({ confirm }) => {
        if (!confirm) return
        app.logout()
      }
    })
  }
})
