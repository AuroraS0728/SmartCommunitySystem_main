const { request } = require('../../api/request')

function normalizeStatus(status) {
  return Number(status || 0) === 1 ? 1 : 0
}

function statusText(status) {
  return normalizeStatus(status) === 1 ? '已取件' : '待取件'
}

Page({
  data: {
    packages: [],
    loading: false
  },

  onShow() {
    this.loadData()
  },

  async loadData() {
    this.setData({ loading: true })
    try {
      const list = await request({ url: '/express/my' })
      const packages = (Array.isArray(list) ? list : []).map((item) => ({
        ...item,
        status: normalizeStatus(item.status),
        statusText: item.statusText || statusText(item.status)
      }))
      this.setData({ packages })
    } catch (error) {
      wx.showToast({ title: error?.message || '快递记录加载失败', icon: 'none' })
      this.setData({ packages: [] })
    } finally {
      this.setData({ loading: false })
    }
  },

  async confirmPickup(event) {
    const id = Number(event.currentTarget.dataset.id || 0)
    if (!id) {
      return
    }
    const confirmed = await new Promise((resolve) => {
      wx.showModal({
        title: '确认取件',
        content: '确认已完成该快递取件吗？',
        success: ({ confirm }) => resolve(!!confirm),
        fail: () => resolve(false)
      })
    })
    if (!confirmed) {
      return
    }

    try {
      await request({
        url: `/express/${id}/pickup`,
        method: 'POST'
      })
      wx.showToast({ title: '取件确认成功', icon: 'success' })
      await this.loadData()
    } catch (error) {
      wx.showToast({ title: error?.message || '取件确认失败', icon: 'none' })
    }
  }
})
