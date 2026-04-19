const { request } = require("../../../api/request")

Page({
  data: {
    id: null,
    order: null,
    loading: false
  },
  onLoad(query) {
    this.setData({ id: Number(query.id) || null })
    this.loadDetail()
  },
  async loadDetail() {
    if (!this.data.id) return
    try {
      const detail = await request({ url: `/repair/${this.data.id}` })
      this.setData({ order: detail?.order || null })
    } catch (error) {
      wx.showToast({ title: error?.message || "加载失败", icon: "none" })
    }
  },
  async updateStatus(status, remark, successTip) {
    if (!this.data.id || this.data.loading) {
      return
    }
    this.setData({ loading: true })
    try {
      await request({
        url: "/repair/status",
        method: "POST",
        data: { orderId: this.data.id, status, remark }
      })
      wx.showToast({ title: successTip, icon: "none" })
      await this.loadDetail()
    } catch (error) {
      wx.showToast({ title: error?.message || "提交失败", icon: "none" })
    } finally {
      this.setData({ loading: false })
    }
  },
  start() {
    this.updateStatus(2, "service started", "已进入服务中")
  },
  finish() {
    this.updateStatus(3, "worker confirmed finish", "已提交结束确认")
  }
})

