const app = getApp()
const { request } = require("../../../api/request")

const BOARD_LABEL_MAP = {
  chat: "闲聊",
  help: "求助",
  activity: "活动",
  share: "分享"
}

function boardText(board) {
  if (!board) return "闲聊"
  return BOARD_LABEL_MAP[board] || board
}

function formatTime(value) {
  if (!value) return "--"
  const text = String(value).replace("T", " ")
  return text.length > 19 ? text.slice(0, 19) : text
}

Page({
  data: {
    id: null,
    loading: false,
    commentsLoading: false,
    actionLoading: false,
    commentSubmitting: false,
    post: null,
    liked: false,
    isOwner: false,
    comments: [],
    commentText: "",
    replyToId: null,
    replyHint: ""
  },

  onLoad(options) {
    this.setData({ id: Number(options?.id || 0) || null })
  },

  onShow() {
    this.loadAll()
  },

  async loadAll() {
    if (!this.data.id) return
    this.setData({ loading: true })
    try {
      await Promise.all([this.loadDetail(), this.loadComments()])
    } finally {
      this.setData({ loading: false })
    }
  },

  async loadDetail() {
    if (!this.data.id) return
    try {
      const data = await request({ url: `/neighbor/forum/${this.data.id}` })
      const post = data?.post || null
      if (!post) throw new Error("帖子不存在")
      this.setData({
        post: {
          ...post,
          boardText: boardText(post.board),
          timeText: formatTime(post.updateTime || post.createTime)
        },
        liked: !!data?.liked,
        isOwner: !!data?.isOwner
      })
    } catch (error) {
      wx.showToast({ title: error?.message || "加载失败", icon: "none" })
    }
  },

  async loadComments() {
    if (!this.data.id) return
    this.setData({ commentsLoading: true })
    try {
      const data = await request({
        url: `/neighbor/forum/${this.data.id}/comments`,
        data: { page: 1, size: 200 }
      })
      const userId = Number(app.globalData.userInfo?.id || 0)
      const isAdmin = Number(app.globalData.role || 0) === 2
      const rows = Array.isArray(data?.items) ? data.items : []
      const comments = rows.map((item) => ({
        ...item,
        timeText: formatTime(item.createTime),
        canDelete: isAdmin || (userId > 0 && userId === Number(item.userId))
      }))
      this.setData({ comments })
    } catch (error) {
      wx.showToast({ title: error?.message || "评论加载失败", icon: "none" })
    } finally {
      this.setData({ commentsLoading: false })
    }
  },

  async onToggleLike() {
    if (this.data.actionLoading || !this.data.id) return
    this.setData({ actionLoading: true })
    try {
      const data = await request({
        url: `/neighbor/forum/${this.data.id}/like`,
        method: "POST"
      })
      const liked = !!data?.liked
      this.setData({
        liked,
        post: this.data.post
          ? {
              ...this.data.post,
              likeCnt: Number(data?.likeCnt || 0)
            }
          : this.data.post
      })
      wx.showToast({ title: liked ? "点赞成功" : "已取消点赞", icon: "success" })
    } catch (error) {
      wx.showToast({ title: error?.message || "操作失败", icon: "none" })
    } finally {
      this.setData({ actionLoading: false })
    }
  },

  onReply(e) {
    const id = Number(e.currentTarget.dataset.id || 0)
    const userId = Number(e.currentTarget.dataset.userId || 0)
    if (!id) return
    this.setData({
      replyToId: id,
      replyHint: userId ? `回复用户 #${userId}` : "回复评论"
    })
  },

  cancelReply() {
    this.setData({ replyToId: null, replyHint: "" })
  },

  onCommentInput(e) {
    this.setData({ commentText: e.detail.value || "" })
  },

  async submitComment() {
    if (this.data.commentSubmitting || !this.data.id) return
    const content = (this.data.commentText || "").trim()
    if (!content) {
      wx.showToast({ title: "请输入评论内容", icon: "none" })
      return
    }
    this.setData({ commentSubmitting: true })
    try {
      await request({
        url: "/neighbor/forum/comment",
        method: "POST",
        data: {
          postId: this.data.id,
          parentId: this.data.replyToId || null,
          content
        }
      })
      this.setData({ commentText: "", replyToId: null, replyHint: "" })
      wx.showToast({ title: "评论成功", icon: "success" })
      await Promise.all([this.loadComments(), this.loadDetail()])
    } catch (error) {
      wx.showToast({ title: error?.message || "评论失败", icon: "none" })
    } finally {
      this.setData({ commentSubmitting: false })
    }
  },

  onDeletePost() {
    if (!this.data.id || this.data.actionLoading) return
    wx.showModal({
      title: "删除帖子",
      content: "删除后不可恢复，确认删除吗？",
      success: ({ confirm }) => {
        if (!confirm) return
        this.deletePost()
      }
    })
  },

  async deletePost() {
    this.setData({ actionLoading: true })
    try {
      await request({
        url: `/neighbor/forum/${this.data.id}`,
        method: "DELETE"
      })
      wx.showToast({ title: "帖子已删除", icon: "success" })
      setTimeout(() => wx.navigateBack({ delta: 1 }), 320)
    } catch (error) {
      wx.showToast({ title: error?.message || "删除失败", icon: "none" })
    } finally {
      this.setData({ actionLoading: false })
    }
  },

  onDeleteComment(e) {
    if (this.data.actionLoading) return
    const id = Number(e.currentTarget.dataset.id || 0)
    if (!id) return
    wx.showModal({
      title: "删除评论",
      content: "确认删除这条评论吗？",
      success: ({ confirm }) => {
        if (!confirm) return
        this.deleteComment(id)
      }
    })
  },

  async deleteComment(id) {
    this.setData({ actionLoading: true })
    try {
      await request({
        url: `/neighbor/forum/comment/${id}`,
        method: "DELETE"
      })
      wx.showToast({ title: "评论已删除", icon: "success" })
      await Promise.all([this.loadComments(), this.loadDetail()])
    } catch (error) {
      wx.showToast({ title: error?.message || "删除失败", icon: "none" })
    } finally {
      this.setData({ actionLoading: false })
    }
  }
})
