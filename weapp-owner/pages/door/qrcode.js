const { request } = require('../../api/request')

Page({
  data: { token: '' },
  async onShow() {
    try {
      const data = await request({ url: '/access/qrcode', method: 'POST' })
      this.setData({ token: data?.token || '' })
    } catch (error) {
      wx.showToast({ title: error?.message || '获取失败', icon: 'none' })
    }
  }
})
