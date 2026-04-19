const app = getApp()
const { request } = require('../../api/request')

Page({
  data: {
    points: 0,
    loadingPoints: false
  },

  onShow() {
    this.loadPoints()
  },

  async loadPoints() {
    this.setData({ loadingPoints: true })
    try {
      const data = await request({ url: '/points/balance' })
      this.setData({ points: Number(data?.points || 0) })
    } catch (error) {
      wx.showToast({ title: error?.message || '积分加载失败', icon: 'none' })
    } finally {
      this.setData({ loadingPoints: false })
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
