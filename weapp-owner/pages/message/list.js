const { request } = require("../../api/request")

function formatTime(value) {
  if (!value) return ""
  return String(value).replace("T", " ").slice(0, 16)
}

Page({
  data: {
    loading: false,
    messages: [],
    pageNum: 1,
    pageSize: 20,
    total: 0,
    hasMore: true
  },

  onShow() {
    this.refresh()
  },

  onPullDownRefresh() {
    this.refresh().finally(() => wx.stopPullDownRefresh())
  },

  onReachBottom() {
    if (!this.data.hasMore || this.data.loading) return
    this.loadMessages(false)
  },

  async refresh() {
    this.setData({
      messages: [],
      pageNum: 1,
      total: 0,
      hasMore: true
    })
    await this.loadMessages(true)
  },

  async loadMessages(reset) {
    this.setData({ loading: true })
    try {
      const result = await request({
        url: "/messages",
        data: {
          pageNum: this.data.pageNum,
          pageSize: this.data.pageSize
        }
      })
      const records = Array.isArray(result?.records) ? result.records : []
      const normalized = records.map((item) => ({
        ...item,
        createTimeText: formatTime(item.createTime)
      }))
      const nextMessages = reset ? normalized : this.data.messages.concat(normalized)
      const total = Number(result?.total || 0)
      this.setData({
        messages: nextMessages,
        total,
        pageNum: this.data.pageNum + 1,
        hasMore: nextMessages.length < total
      })
    } catch (error) {
      wx.showToast({ title: error?.message || "消息加载失败", icon: "none" })
    } finally {
      this.setData({ loading: false })
    }
  },

  async markRead(e) {
    const id = e?.currentTarget?.dataset?.id
    if (!id) return
    const target = this.data.messages.find((item) => Number(item.id) === Number(id))
    if (!target || Number(target.isRead) === 1) return
    try {
      await request({ url: `/messages/${id}/read`, method: "PUT" })
      this.setData({
        messages: this.data.messages.map((item) => (
          Number(item.id) === Number(id) ? { ...item, isRead: 1 } : item
        ))
      })
    } catch (error) {
      wx.showToast({ title: error?.message || "操作失败", icon: "none" })
    }
  },

  async markAllRead() {
    if (!this.data.messages.some((item) => Number(item.isRead) === 0)) return
    try {
      await request({ url: "/messages/read-all", method: "PUT" })
      this.setData({
        messages: this.data.messages.map((item) => ({ ...item, isRead: 1 }))
      })
      wx.showToast({ title: "已全部标记", icon: "success" })
    } catch (error) {
      wx.showToast({ title: error?.message || "操作失败", icon: "none" })
    }
  }
})
