const { request } = require("../../api/request")

Page({
  data: {
    list: [],
    loading: false
  },

  onShow() {
    this.loadData()
  },

  async loadData() {
    this.setData({ loading: true })
    try {
      const list = await request({ url: '/activity/my/registrations' })
      this.setData({ list: Array.isArray(list) ? list : [] })
    } catch (error) {
      wx.showToast({ title: error?.message || '加载失败', icon: 'none' })
    } finally {
      this.setData({ loading: false })
    }
  },

  openDetail(event) {
    const id = event.currentTarget.dataset.id
    wx.navigateTo({ url: `/pages/activity/detail?id=${id}` })
  }
})
