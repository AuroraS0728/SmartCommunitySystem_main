const { request } = require("../../../api/request")

Page({
  data: {
    orderId: "",
    code: "",
    result: ""
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
        this.setData({ result: "验证码不正确，请重试" })
        return
      }
      this.setData({
        result: allVerified
          ? "验证码通过，全部工人已完成核验，可进入服务流程"
          : `验证码通过，待核验工人：${(resp?.pendingWorkerIds || []).join(",") || "--"}`
      })
      if (allVerified) {
        setTimeout(() => {
          wx.navigateTo({ url: `/pages/worker/work/process?id=${this.data.orderId}` })
        }, 400)
      }
    } catch (error) {
      wx.showToast({ title: error?.message || "校验失败", icon: "none" })
    }
  }
})

