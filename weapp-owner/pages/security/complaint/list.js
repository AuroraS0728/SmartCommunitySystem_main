const { request } = require("../../../api/request")

const STATUS_OPTIONS = [
  { label: "All", value: "" },
  { label: "Pending", value: "1" },
  { label: "Replied", value: "3" }
]

function statusText(status) {
  const map = {
    1: "Pending",
    2: "Processing",
    3: "Replied"
  }
  return map[Number(status)] || "Unknown"
}

Page({
  data: {
    loading: false,
    replying: false,
    list: [],
    statusOptions: STATUS_OPTIONS,
    statusValue: "",
    replyDrafts: {}
  },

  onShow() {
    this.loadList()
  },

  async loadList() {
    this.setData({ loading: true })
    try {
      const params = {}
      if (this.data.statusValue !== "") {
        params.status = Number(this.data.statusValue)
      }
      const rawList = await request({
        url: "/complaint/list",
        data: params
      })
      const list = (Array.isArray(rawList) ? rawList : []).map((item) => ({
        ...item,
        statusText: statusText(item.status)
      }))
      const replyDrafts = { ...this.data.replyDrafts }
      list.forEach((item) => {
        if (item?.id === undefined || item?.id === null) return
        if (replyDrafts[item.id] === undefined) {
          replyDrafts[item.id] = ""
        }
      })
      this.setData({ list, replyDrafts })
    } catch (error) {
      wx.showToast({ title: error?.message || "Load failed", icon: "none" })
      this.setData({ list: [] })
    } finally {
      this.setData({ loading: false })
    }
  },

  onStatusChange(e) {
    const idx = Number(e?.detail?.value)
    const option = this.data.statusOptions[idx]
    const value = option ? option.value : ""
    this.setData({ statusValue: value }, () => this.loadList())
  },

  onReplyInput(e) {
    const id = e.currentTarget.dataset.id
    if (!id) return
    this.setData({
      [`replyDrafts.${id}`]: e.detail.value
    })
  },

  async submitReply(e) {
    if (this.data.replying) return
    const id = e.currentTarget.dataset.id
    if (!id) return
    const reply = (this.data.replyDrafts?.[id] || "").trim()
    if (!reply) {
      wx.showToast({ title: "Please input reply", icon: "none" })
      return
    }

    this.setData({ replying: true })
    try {
      await request({
        url: `/complaint/${id}/reply`,
        method: "POST",
        data: { reply }
      })
      wx.showToast({ title: "Reply sent", icon: "success" })
      this.setData({ [`replyDrafts.${id}`]: "" })
      await this.loadList()
    } catch (error) {
      wx.showToast({ title: error?.message || "Reply failed", icon: "none" })
    } finally {
      this.setData({ replying: false })
    }
  }
})

