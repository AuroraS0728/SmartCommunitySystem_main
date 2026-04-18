const { request } = require('../../api/request')

Page({
  data: { list: [] },
  async onShow() {
    try {
      const list = await request({ url: '/notice/list' })
      this.setData({ list: Array.isArray(list) ? list : [] })
    } catch (error) {
      wx.showToast({ title: error?.message || '加载失败', icon: 'none' })
      this.setData({ list: [] })
    }
  }
})
