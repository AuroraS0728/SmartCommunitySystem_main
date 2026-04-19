const { request } = require("../../../api/request")

const BOARD_OPTIONS = [
  { label: "闲聊", value: "chat" },
  { label: "求助", value: "help" },
  { label: "活动", value: "activity" },
  { label: "分享", value: "share" }
]

Page({
  data: {
    boardOptions: BOARD_OPTIONS,
    board: "chat",
    title: "",
    content: "",
    submitting: false
  },

  onBoardTap(e) {
    const value = e.currentTarget.dataset.value
    if (!value || value === this.data.board) return
    this.setData({ board: value })
  },

  onTitleInput(e) {
    this.setData({ title: e.detail.value || "" })
  },

  onContentInput(e) {
    this.setData({ content: e.detail.value || "" })
  },

  async onSubmit() {
    if (this.data.submitting) return
    const title = (this.data.title || "").trim()
    const content = (this.data.content || "").trim()

    if (!title) {
      wx.showToast({ title: "请输入标题", icon: "none" })
      return
    }
    if (!content) {
      wx.showToast({ title: "请输入正文内容", icon: "none" })
      return
    }

    this.setData({ submitting: true })
    try {
      await request({
        url: "/neighbor/forum/publish",
        method: "POST",
        data: {
          board: this.data.board || "chat",
          title,
          content
        }
      })
      wx.showToast({ title: "发布成功", icon: "success" })
      setTimeout(() => {
        wx.redirectTo({ url: "/pages/neighbor/forum/list" })
      }, 320)
    } catch (error) {
      wx.showToast({ title: error?.message || "发布失败", icon: "none" })
    } finally {
      this.setData({ submitting: false })
    }
  }
})
