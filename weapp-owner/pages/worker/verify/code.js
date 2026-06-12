const { request } = require("../../../api/request")

Page({
  data: {
    orderId: "",
    code: "",
    result: "",
    verified: false,
    allVerified: false,
    pendingWorkerIds: []
  },

  onLoad(query) {
    if (query?.orderId) {
      this.setData({ orderId: String(query.orderId) })
    }
  },

  onOrderId(e) {
    this.setData({ orderId: e.detail.value.trim() })
  },

  onCode(e) {
    this.setData({ code: e.detail.value.trim() })
  },

  async verify() {
    if (!this.data.orderId || !this.data.code) {
      wx.showToast({ title: "请输入工单ID和验证码", icon: "none" })
      return
    }
    try {
      const resp = await request({
        url: "/worker/verify-code",
        method: "POST",
        data: {
          orderId: Number(this.data.orderId),
          code: this.data.code
        }
      })
      const pass = !!resp?.pass
      const allVerified = !!resp?.allVerified
      if (!pass) {
        this.setData({
          result: "验证码不正确，请重试",
          verified: false,
          allVerified: false,
          pendingWorkerIds: []
        })
        return
      }
      const pendingWorkerIds = Array.isArray(resp?.pendingWorkerIds) ? resp.pendingWorkerIds : []
      this.setData({
        verified: true,
        allVerified,
        pendingWorkerIds,
        result: allVerified
          ? "验证码通过，请继续完成人脸核验"
          : `验证码通过，待核验工人：${pendingWorkerIds.join(",") || "--"}`
      })
      if (allVerified) {
        setTimeout(() => {
          wx.navigateTo({ url: `/pages/worker/verify/face?orderId=${this.data.orderId}` })
        }, 400)
      }
    } catch (error) {
      wx.showToast({ title: error?.message || "校验失败", icon: "none" })
    }
  },

  goFaceVerify() {
    if (!this.data.orderId) {
      wx.showToast({ title: "工单ID无效", icon: "none" })
      return
    }
    wx.navigateTo({ url: `/pages/worker/verify/face?orderId=${this.data.orderId}` })
  },

  goProcess() {
    if (!this.data.orderId) {
      wx.showToast({ title: "工单ID无效", icon: "none" })
      return
    }
    wx.navigateTo({ url: `/pages/worker/work/process?id=${this.data.orderId}` })
  }
})
