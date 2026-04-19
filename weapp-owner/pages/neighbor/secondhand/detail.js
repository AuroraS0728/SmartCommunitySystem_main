const app = getApp()
const { request } = require("../../../api/request")

const REPORT_REASONS = ["疑似欺诈", "违规内容", "虚假信息", "侵权内容", "其他原因"]

function parseImages(images) {
  if (!images) return []
  if (Array.isArray(images)) return images
  try {
    const parsed = JSON.parse(images)
    return Array.isArray(parsed) ? parsed : []
  } catch (error) {
    return typeof images === "string" ? [images] : []
  }
}

function statusText(status) {
  const map = { 1: "在售", 2: "已售", 3: "下架" }
  return map[Number(status)] || "未知"
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
    actionLoading: false,
    item: null,
    favorited: false,
    isOwner: false
  },

  onLoad(options) {
    this.setData({ id: Number(options?.id || 0) || null })
  },

  onShow() {
    this.loadDetail()
  },

  async loadDetail() {
    if (!this.data.id) return
    this.setData({ loading: true })
    try {
      const data = await request({
        url: `/neighbor/second-hand/${this.data.id}`
      })
      const item = data?.item || null
      if (!item) throw new Error("商品不存在")
      const images = parseImages(item.images)
      const price = Number(item.price || 0)
      const uid = Number(app.globalData.userInfo?.id || 0)
      this.setData({
        item: {
          ...item,
          images,
          statusText: statusText(item.status),
          priceText: price > 0 ? `¥${price.toFixed(2)}` : "面议",
          timeText: formatTime(item.updateTime || item.createTime)
        },
        favorited: !!data?.favorited,
        isOwner: !!data?.isOwner || (uid > 0 && uid === Number(item.userId))
      })
    } catch (error) {
      wx.showToast({ title: error?.message || "加载失败", icon: "none" })
    } finally {
      this.setData({ loading: false })
    }
  },

  onPreviewImage(e) {
    const current = e.currentTarget.dataset.url
    const urls = this.data.item?.images || []
    if (!current || !urls.length) return
    wx.previewImage({ current, urls })
  },

  onContact() {
    const contact = this.data.item?.contact || "暂无联系方式"
    wx.showModal({
      title: "联系卖家",
      content: String(contact),
      showCancel: false
    })
  },

  async onToggleFavorite() {
    if (this.data.actionLoading || !this.data.id) return
    this.setData({ actionLoading: true })
    try {
      const favorited = this.data.favorited
      await request({
        url: `/neighbor/second-hand/${this.data.id}/favorite`,
        method: favorited ? "DELETE" : "POST"
      })
      this.setData({ favorited: !favorited })
      wx.showToast({
        title: favorited ? "已取消收藏" : "收藏成功",
        icon: "success"
      })
    } catch (error) {
      wx.showToast({ title: error?.message || "操作失败", icon: "none" })
    } finally {
      this.setData({ actionLoading: false })
    }
  },

  onReport() {
    if (this.data.actionLoading || !this.data.id) return
    wx.showActionSheet({
      itemList: REPORT_REASONS,
      success: ({ tapIndex }) => {
        const reason = REPORT_REASONS[tapIndex]
        if (!reason) return
        this.submitReport(reason)
      }
    })
  },

  async submitReport(reason) {
    this.setData({ actionLoading: true })
    try {
      await request({
        url: `/neighbor/second-hand/${this.data.id}/report`,
        method: "POST",
        data: { reason }
      })
      wx.showToast({ title: "举报已提交", icon: "success" })
    } catch (error) {
      wx.showToast({ title: error?.message || "举报失败", icon: "none" })
    } finally {
      this.setData({ actionLoading: false })
    }
  },

  onMarkSold() {
    this.changeStatus("sold", "标记已售")
  },

  onOffline() {
    this.changeStatus("offline", "下架商品")
  },

  changeStatus(action, title) {
    if (this.data.actionLoading || !this.data.id) return
    wx.showModal({
      title,
      content: "确认继续？",
      success: ({ confirm }) => {
        if (!confirm) return
        this.doChangeStatus(action)
      }
    })
  },

  async doChangeStatus(action) {
    this.setData({ actionLoading: true })
    try {
      await request({
        url: `/neighbor/second-hand/${this.data.id}/${action}`,
        method: "POST"
      })
      wx.showToast({ title: "操作成功", icon: "success" })
      await this.loadDetail()
    } catch (error) {
      wx.showToast({ title: error?.message || "操作失败", icon: "none" })
    } finally {
      this.setData({ actionLoading: false })
    }
  },

  onDelete() {
    if (this.data.actionLoading || !this.data.id) return
    wx.showModal({
      title: "删除商品",
      content: "删除后不可恢复，确认删除？",
      success: ({ confirm }) => {
        if (!confirm) return
        this.deleteItem()
      }
    })
  },

  async deleteItem() {
    this.setData({ actionLoading: true })
    try {
      await request({
        url: `/neighbor/second-hand/${this.data.id}`,
        method: "DELETE"
      })
      wx.showToast({ title: "已删除", icon: "success" })
      setTimeout(() => wx.navigateBack({ delta: 1 }), 350)
    } catch (error) {
      wx.showToast({ title: error?.message || "删除失败", icon: "none" })
    } finally {
      this.setData({ actionLoading: false })
    }
  }
})
