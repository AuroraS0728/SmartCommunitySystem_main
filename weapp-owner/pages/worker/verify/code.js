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
      this.setData({ result: pass ? "验证通过，已进入服务中" : "验证失败，请核对验证码" })
      if (pass) {
        setTimeout(() => {
          wx.navigateTo({ url: `/pages/worker/work/process?id=${this.data.orderId}` })
        }, 400)
      }
    } catch (error) {
      wx.showToast({ title: error?.message || "校验失败", icon: "none" })
    }
  }
})
