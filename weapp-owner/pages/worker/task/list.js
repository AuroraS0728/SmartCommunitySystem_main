const { request } = require("../../../api/request")

function statusText(status) {
  const map = {
    1: "待上门核验",
    2: "服务中",
    3: "待业主评价",
    4: "已完成",
    5: "已取消"
  }
  return map[Number(status)] || "未知状态"
}

function navTitleByStatus(status) {
  const map = {
    1: "待上门工单",
    2: "服务中工单",
    3: "待评价工单"
  }
  return map[Number(status)] || "工单列表"
}

Page({
  data: {
    list: [],
    statusFilter: null
  },

  onLoad(query) {
    const status = Number(query?.status || 0)
    const statusFilter = Number.isFinite(status) && status > 0 ? status : null
    this.setData({ statusFilter })
    wx.setNavigationBarTitle({ title: navTitleByStatus(statusFilter) })
  },

  onShow() {
    this.loadList()
  },

  async loadList() {
    try {
      const params = this.data.statusFilter ? { status: this.data.statusFilter } : {}
      const list = await request({ url: "/repair/list", data: params })
      this.setData({
        list: (Array.isArray(list) ? list : []).map((item) => ({
          ...item,
          statusText: statusText(item.status)
        }))
      })
    } catch (error) {
      wx.showToast({ title: error?.message || "加载失败", icon: "none" })
      this.setData({ list: [] })
    }
  }
})

