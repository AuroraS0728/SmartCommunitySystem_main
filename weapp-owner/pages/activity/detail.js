const { request } = require("../../api/request")

Page({
  data: {
    id: '',
    detail: null
  },

  onLoad(options) {
    this.setData({ id: options.id || '' })
  },

  onShow() {
    this.loadDetail()
  },

  async loadDetail() {
    if (!this.data.id) return
    try {
      const detail = await request({ url: `/activity/${this.data.id}` })
      this.setData({ detail })
    } catch (error) {
      wx.showToast({ title: error?.message || '详情加载失败', icon: 'none' })
    }
  },

  goRegister() {
    if (!this.data.detail?.canRegister) return
    wx.navigateTo({ url: `/pages/activity/register?id=${this.data.id}` })
  }
})
