const { request } = require('../../api/request')

function normalizeStatus(status) {
  const value = Number(status || 1)
  return [1, 2, 3].includes(value) ? value : 1
}

function statusText(status) {
  const value = normalizeStatus(status)
  if (value === 2) {
    return '维护中'
  }
  if (value === 3) {
    return '停用'
  }
  return '正常开放'
}

Page({
  data: {
    facilities: [],
    loading: false
  },

  onShow() {
    this.loadData()
  },

  async loadData() {
    this.setData({ loading: true })
    try {
      const list = await request({ url: '/facility/list' })
      const facilities = (Array.isArray(list) ? list : []).map((item) => ({
        ...item,
        status: normalizeStatus(item.status),
        statusText: statusText(item.status)
      }))
      this.setData({ facilities })
    } catch (error) {
      wx.showToast({ title: error?.message || '设施信息加载失败', icon: 'none' })
      this.setData({ facilities: [] })
    } finally {
      this.setData({ loading: false })
    }
  },

  goRepair(event) {
    const facilityId = Number(event.currentTarget.dataset.id || 0)
    const facilityName = event.currentTarget.dataset.name || ''
    const location = event.currentTarget.dataset.location || ''
    if (!facilityId) {
      return
    }
    const query = [
      `facilityId=${facilityId}`,
      `facilityName=${encodeURIComponent(facilityName)}`,
      `facilityLocation=${encodeURIComponent(location)}`
    ].join('&')
    wx.navigateTo({ url: `/pages/repair/submit?${query}` })
  }
})
