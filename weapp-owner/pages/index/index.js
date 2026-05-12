const { request } = require("../../api/request")

Page({
  data: {
    user: null,
    points: 0,
    recommendations: [],
    popupVisible: false,
    popupRecommendations: [],
    banners: [
      {
        id: 'secondhand',
        imageUrl: 'https://images.unsplash.com/photo-1517142089942-ba376ce32a2e?auto=format&fit=crop&w=1200&q=80',
        title: '二手交易',
        path: '/pages/neighbor/secondhand/list',
        mode: 'navigate'
      },
      {
        id: 'complaint',
        imageUrl: 'https://images.unsplash.com/photo-1521791055366-0d553872125f?auto=format&fit=crop&w=1200&q=80',
        title: '您留言我响应',
        path: '/pages/complaint/submit',
        mode: 'navigate'
      },
      {
        id: 'activity',
        imageUrl: 'https://images.unsplash.com/photo-1529156069898-49953e39b3ac?auto=format&fit=crop&w=1200&q=80',
        title: '活动专区',
        path: '/pages/activity/list',
        mode: 'navigate'
      }
    ]
  },

  onShow() {
    this.loadAll()
  },

  async loadAll() {
    await Promise.allSettled([this.loadUser(), this.loadRecommendations(), this.loadPopupRecommendations()])
  },

  async loadUser() {
    try {
      const me = await request({ url: "/user/me" })
      this.setData({
        user: me,
        points: Number(me?.points || 0)
      })
    } catch (error) {
      wx.showToast({ title: error?.message || "加载失败", icon: "none" })
    }
  },

  async loadRecommendations() {
    try {
      const list = await request({ url: "/recommend/services" })
      this.setData({ recommendations: Array.isArray(list) ? list : [] })
    } catch (error) {
      this.setData({ recommendations: [] })
    }
  },

  async loadPopupRecommendations() {
    const shown = wx.getStorageSync('recommendPopupShown')
    if (shown) {
      this.setData({ popupVisible: false, popupRecommendations: [] })
      return
    }
    try {
      const list = await request({ url: "/recommend/popup" })
      const popupRecommendations = Array.isArray(list) ? list.slice(0, 2) : []
      this.setData({
        popupRecommendations,
        popupVisible: popupRecommendations.length > 0
      })
    } catch (error) {
      this.setData({ popupVisible: false, popupRecommendations: [] })
    }
  },

  openRecommendation(event) {
    const item = event?.currentTarget?.dataset?.item || {}
    const path = item.actionPath || '/pages/service/service'
    this.closePopup()
    this.openPath(path)
  },

  closePopup() {
    wx.setStorageSync('recommendPopupShown', true)
    this.setData({ popupVisible: false })
  },

  tapBanner(event) {
    const item = event?.currentTarget?.dataset?.item || {}
    this.openPath(item.path, item.mode)
  },

  openPath(path, mode) {
    const target = path || '/pages/service/service'
    if (target.startsWith('/pages/index/index') || target.startsWith('/pages/service/service') || target.startsWith('/pages/user/profile')) {
      wx.switchTab({ url: target })
      return
    }
    if (mode === 'switchTab') {
      wx.switchTab({ url: target })
      return
    }
    wx.navigateTo({ url: target })
  },

  goFeeBills() {
    wx.navigateTo({ url: "/pages/points/bills/index" })
  },

  goInvite() {
    wx.navigateTo({ url: "/pages/door/invite" })
  },

  goRepair() {
    wx.navigateTo({ url: "/pages/repair/submit" })
  },

  goRentSale() {
    wx.navigateTo({ url: "/pages/neighbor/secondhand/list" })
  },

  goService() {
    wx.switchTab({ url: "/pages/service/service" })
  },

  goActivity() {
    wx.navigateTo({ url: "/pages/activity/list" })
  }
})
