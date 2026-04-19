const { request } = require("../../api/request")

Page({
  data: {
    id: null,
    detail: {},
    loading: false,
    evaluating: false,
    confirmingFinish: false,
    rating: 5,
    comment: ""
  },
  onLoad(query) {
    const id = Number(query?.id)
    if (!id) {
      wx.showToast({ title: "工单ID无效", icon: "none" })
      return
    }
    this.setData({ id })
    this.loadDetail()
  },
  onShow() {
    if (this.data.id) {
      this.loadDetail()
    }
  },
  async loadDetail() {
    if (!this.data.id) return
    this.setData({ loading: true })
    try {
      const detail = (await request({ url: `/repair/${this.data.id}` })) || {}
      this.setData({
        detail,
        rating: detail?.evaluation?.rating || 5,
        comment: detail?.evaluation?.comment || ""
      })
      if (detail?.order?.status === 1 && !detail?.verifyCode) {
        await this.fetchVerifyCode()
      }
    } catch (error) {
      wx.showToast({ title: error?.message || "加载失败", icon: "none" })
      this.setData({ detail: {} })
    } finally {
      this.setData({ loading: false })
    }
  },
  async fetchVerifyCode() {
    try {
      const resp = await request({ url: `/repair/${this.data.id}/verify-code` })
      this.setData({
        detail: {
          ...this.data.detail,
          verifyCode: resp?.verifyCode || this.data.detail.verifyCode
        }
      })
    } catch (error) {
      // keep silent
    }
  },
  onRatingTap(e) {
    const rating = Number(e.currentTarget.dataset.value)
    if (!rating) return
    this.setData({ rating })
  },
  onCommentInput(e) {
    this.setData({ comment: e.detail.value })
  },
  async confirmFinish() {
    if (!this.data.id || this.data.confirmingFinish) return
    const confirmed = await new Promise((resolve) => {
      wx.showModal({
        title: "确认结束服务",
        content: "确认本次家政/维修服务已完成吗？确认后将等待维修端共同确认。",
        success: (res) => resolve(!!res.confirm),
        fail: () => resolve(false)
      })
    })
    if (!confirmed) return
    this.setData({ confirmingFinish: true })
    try {
      await request({
        url: "/repair/status",
        method: "POST",
        data: {
          orderId: this.data.id,
          status: 3,
          remark: "owner confirmed finish"
        }
      })
      wx.showToast({ title: "已提交结束确认", icon: "none" })
      await this.loadDetail()
    } catch (error) {
      wx.showToast({ title: error?.message || "提交失败", icon: "none" })
    } finally {
      this.setData({ confirmingFinish: false })
    }
  },
  async submitEvaluation() {
    if (this.data.evaluating || !this.data.id) return
    if (this.data.rating < 1 || this.data.rating > 5) {
      wx.showToast({ title: "请选择1-5星", icon: "none" })
      return
    }
    this.setData({ evaluating: true })
    try {
      await request({
        url: `/repair/${this.data.id}/evaluate`,
        method: "POST",
        data: {
          rating: this.data.rating,
          comment: this.data.comment,
          anonymous: 0
        }
      })
      wx.showToast({ title: "评价成功", icon: "success" })
      await this.loadDetail()
    } catch (error) {
      wx.showToast({ title: error?.message || "评价失败", icon: "none" })
    } finally {
      this.setData({ evaluating: false })
    }
  }
})

