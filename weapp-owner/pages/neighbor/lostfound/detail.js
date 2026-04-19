const app = getApp()
const { request } = require("../../../api/request")

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

function formatTime(value) {
  if (!value) return "--"
  const text = String(value).replace("T", " ")
  return text.length > 19 ? text.slice(0, 19) : text
}

function typeText(type) {
  return Number(type) === 2 ? "招领" : "失物"
}

function statusText(status) {
  return Number(status) === 2 ? "已完成" : "进行中"
}

function claimStatusText(status) {
  const value = Number(status)
  if (value === 1) return "已通过"
  if (value === 2) return "已驳回"
  return "待审核"
}

Page({
  data: {
    id: null,
    loading: false,
    actionLoading: false,
    claimsLoading: false,
    item: null,
    isOwner: false,
    claimPopup: false,
    claimProof: "",
    claims: []
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
      const row = await request({ url: `/neighbor/lost-found/${this.data.id}` })
      if (!row) throw new Error("记录不存在")
      const userId = Number(app.globalData.userInfo?.id || 0)
      const isOwner = userId > 0 && userId === Number(row.userId)
      const item = {
        ...row,
        imageList: parseImages(row.images),
        typeText: typeText(row.type),
        statusText: statusText(row.status),
        timeText: formatTime(row.updateTime || row.createTime)
      }
      this.setData({ item, isOwner })
      if (isOwner && Number(item.type) === 2) {
        this.loadClaims()
      } else {
        this.setData({ claims: [] })
      }
    } catch (error) {
      wx.showToast({ title: error?.message || "加载失败", icon: "none" })
    } finally {
      this.setData({ loading: false })
    }
  },

  async loadClaims() {
    if (!this.data.id) return
    this.setData({ claimsLoading: true })
    try {
      const data = await request({
        url: `/neighbor/lost-found/${this.data.id}/claims`,
        data: { page: 1, size: 50 }
      })
      const rows = Array.isArray(data?.items) ? data.items : []
      const claims = rows.map((item) => ({
        ...item,
        statusText: claimStatusText(item.status),
        timeText: formatTime(item.createTime)
      }))
      this.setData({ claims })
    } catch (error) {
      wx.showToast({ title: error?.message || "认领记录加载失败", icon: "none" })
    } finally {
      this.setData({ claimsLoading: false })
    }
  },

  onPreviewImage(e) {
    const current = e.currentTarget.dataset.url
    const urls = this.data.item?.imageList || []
    if (!current || !urls.length) return
    wx.previewImage({ current, urls })
  },

  onContact() {
    const contact = this.data.item?.contact || "未填写联系方式"
    wx.showModal({
      title: "联系方式",
      content: String(contact),
      showCancel: false
    })
  },

  openClaimPopup() {
    if (this.data.actionLoading) return
    this.setData({ claimPopup: true, claimProof: "" })
  },

  closeClaimPopup() {
    if (this.data.actionLoading) return
    this.setData({ claimPopup: false, claimProof: "" })
  },

  onClaimProofInput(e) {
    this.setData({ claimProof: e.detail.value || "" })
  },

  async submitClaim() {
    if (this.data.actionLoading || !this.data.id) return
    const proof = (this.data.claimProof || "").trim()
    if (!proof) {
      wx.showToast({ title: "请填写认领说明", icon: "none" })
      return
    }
    this.setData({ actionLoading: true })
    try {
      await request({
        url: `/neighbor/lost-found/${this.data.id}/claim`,
        method: "POST",
        data: { proof }
      })
      wx.showToast({ title: "认领申请已提交", icon: "success" })
      this.setData({ claimPopup: false, claimProof: "" })
      this.loadDetail()
    } catch (error) {
      wx.showToast({ title: error?.message || "提交失败", icon: "none" })
    } finally {
      this.setData({ actionLoading: false })
    }
  },

  onResolve() {
    if (this.data.actionLoading || !this.data.id) return
    wx.showModal({
      title: "确认完结",
      content: "确认将当前记录标记为已完成吗？",
      success: ({ confirm }) => {
        if (!confirm) return
        this.resolveItem()
      }
    })
  },

  async resolveItem() {
    this.setData({ actionLoading: true })
    try {
      await request({
        url: `/neighbor/lost-found/${this.data.id}/resolve`,
        method: "POST"
      })
      wx.showToast({ title: "已标记完成", icon: "success" })
      this.loadDetail()
    } catch (error) {
      wx.showToast({ title: error?.message || "操作失败", icon: "none" })
    } finally {
      this.setData({ actionLoading: false })
    }
  },

  onDelete() {
    if (this.data.actionLoading || !this.data.id) return
    wx.showModal({
      title: "删除记录",
      content: "删除后不可恢复，确定删除吗？",
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
        url: `/neighbor/lost-found/${this.data.id}`,
        method: "DELETE"
      })
      wx.showToast({ title: "已删除", icon: "success" })
      setTimeout(() => wx.navigateBack({ delta: 1 }), 320)
    } catch (error) {
      wx.showToast({ title: error?.message || "删除失败", icon: "none" })
    } finally {
      this.setData({ actionLoading: false })
    }
  },

  onReviewClaim(e) {
    if (this.data.actionLoading) return
    const claimId = Number(e.currentTarget.dataset.id || 0)
    const status = Number(e.currentTarget.dataset.status || 0)
    if (!claimId || (status !== 1 && status !== 2)) return
    const title = status === 1 ? "通过认领" : "驳回认领"
    wx.showModal({
      title,
      content: "确认提交本次审核结果吗？",
      success: ({ confirm }) => {
        if (!confirm) return
        this.reviewClaim(claimId, status)
      }
    })
  },

  async reviewClaim(claimId, status) {
    this.setData({ actionLoading: true })
    try {
      await request({
        url: `/neighbor/lost-found/claim/${claimId}/review`,
        method: "POST",
        data: { status }
      })
      wx.showToast({ title: "审核成功", icon: "success" })
      await this.loadDetail()
    } catch (error) {
      wx.showToast({ title: error?.message || "审核失败", icon: "none" })
    } finally {
      this.setData({ actionLoading: false })
    }
  }
})
