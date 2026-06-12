const { request } = require("../../api/request")

function statusText(status) {
  const map = {
    1: "待派单/待上门验证",
    2: "服务中",
    3: "待评价",
    4: "已完成",
    5: "已取消"
  }
  return map[Number(status)] || "未知状态"
}

function statusClass(status) {
  const map = {
    1: "status-warning",
    2: "status-warning",
    3: "status-warning",
    4: "status-success",
    5: "status-danger"
  }
  return map[Number(status)] || "status-warning"
}

Page({
  data: {
    list: []
  },

  async onShow() {
    try {
      const list = await request({ url: "/repair/list" })
      const normalized = (Array.isArray(list) ? list : []).map((item) => ({
        ...item,
        statusText: item.statusText || statusText(item.status),
        statusClass: statusClass(item.status)
      }))
      this.setData({ list: normalized })
    } catch (error) {
      wx.showToast({
        title: error?.message || "加载失败",
        icon: "none"
      })
      this.setData({ list: [] })
    }
  },

  goDetail(e) {
    const id = e.currentTarget.dataset.id
    if (!id) {
      wx.showToast({
        title: "工单ID不存在",
        icon: "none"
      })
      return
    }

    wx.navigateTo({
      url: `/pages/repair/detail?id=${id}`
    })
  }
})
