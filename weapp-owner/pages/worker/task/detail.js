const { request } = require("../../../api/request")

function statusText(status) {
  const map = {
    1: "待上门验证",
    2: "服务中",
    3: "待业主评价",
    4: "已完成",
    5: "已取消"
  }
  return map[Number(status)] || "未知状态"
}

Page({
  data: {
    id: null,
    detail: null,
    loading: false
  },
  async onLoad(query) {
    const id = Number(query.id)
    if (!id) {
      wx.showToast({ title: "工单ID无效", icon: "none" })
      return
    }
    this.setData({ id })
    await this.loadDetail()
  },
  async onShow() {
    if (this.data.id) {
      await this.loadDetail()
    }
  },
  async loadDetail() {
    this.setData({ loading: true })
    try {
      const detail = await request({ url: `/repair/${this.data.id}` })
      const order = detail?.order || null
      this.setData({
        detail: detail
          ? {
              ...detail,
              statusText: statusText(order?.status)
            }
          : null
      })
    } catch (error) {
      wx.showToast({ title: error?.message || "加载失败", icon: "none" })
    } finally {
      this.setData({ loading: false })
    }
  },
  goVerify() {
    wx.navigateTo({ url: `/pages/worker/verify/code?orderId=${this.data.id}` })
  },
  goProcess() {
    wx.navigateTo({ url: `/pages/worker/work/process?id=${this.data.id}` })
  }
})
