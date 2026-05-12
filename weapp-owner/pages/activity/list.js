const { request } = require("../../api/request")

Page({
  data: {
    tabs: ['全部', '公益', '爬山', '露营', '亲子', '宠物', '夕阳红'],
    activeTab: '全部',
    loading: false,
    list: []
  },

  onShow() {
    this.loadData()
  },

  async loadData() {
    this.setData({ loading: true })
    try {
      const type = this.data.activeTab === '全部' ? undefined : this.data.activeTab
      const list = await request({ url: '/activity/list', data: { type } })
      this.setData({ list: Array.isArray(list) ? list : [] })
    } catch (error) {
      wx.showToast({ title: error?.message || '活动加载失败', icon: 'none' })
    } finally {
      this.setData({ loading: false })
    }
  },

  switchTab(event) {
    this.setData({ activeTab: event.currentTarget.dataset.type }, () => this.loadData())
  },

  openDetail(event) {
    const id = event.currentTarget.dataset.id
    wx.navigateTo({ url: `/pages/activity/detail?id=${id}` })
  }
})
