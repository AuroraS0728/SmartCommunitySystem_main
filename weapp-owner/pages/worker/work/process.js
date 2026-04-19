const { request } = require("../../../api/request")

function readFileBase64(path) {
  return new Promise((resolve, reject) => {
    wx.getFileSystemManager().readFile({
      filePath: path,
      encoding: "base64",
      success: (res) => resolve(res.data),
      fail: () => reject(new Error("图片读取失败"))
    })
  })
}

Page({
  data: {
    id: null,
    order: null,
    loading: false,
    beforeImages: "",
    afterImages: "",
    chargeAmount: "",
    chargeRemark: ""
  },
  onLoad(query) {
    this.setData({ id: Number(query.id) || null })
    this.loadDetail()
  },
  async loadDetail() {
    if (!this.data.id) return
    try {
      const detail = await request({ url: `/repair/${this.data.id}` })
      this.setData({
        order: detail?.order || null,
        beforeImages: detail?.order?.beforeImages || "",
        afterImages: detail?.order?.afterImages || "",
        chargeAmount: detail?.order?.chargeAmount || "",
        chargeRemark: detail?.order?.chargeRemark || ""
      })
    } catch (error) {
      wx.showToast({ title: error?.message || "加载失败", icon: "none" })
    }
  },
  async chooseImage(e) {
    const field = e?.currentTarget?.dataset?.field
    if (!field) return
    try {
      const media = await wx.chooseMedia({
        count: 1,
        mediaType: ["image"],
        sourceType: ["camera"],
        camera: "back"
      })
      const file = media?.tempFiles?.[0]
      if (!file?.tempFilePath) return
      const base64 = await readFileBase64(file.tempFilePath)
      this.setData({ [field]: JSON.stringify([`data:image/jpeg;base64,${base64}`]) })
      wx.showToast({ title: "上传成功", icon: "success" })
    } catch (error) {
      if (error?.errMsg?.includes("cancel")) return
      wx.showToast({ title: error?.message || "上传失败", icon: "none" })
    }
  },
  onChargeAmountInput(e) {
    this.setData({ chargeAmount: e.detail.value })
  },
  onChargeRemarkInput(e) {
    this.setData({ chargeRemark: e.detail.value })
  },
  async updateStatus(status, remark, successTip, extra = {}) {
    if (!this.data.id || this.data.loading) {
      return
    }
    this.setData({ loading: true })
    try {
      await request({
        url: "/repair/status",
        method: "POST",
        data: { orderId: this.data.id, status, remark, ...extra }
      })
      wx.showToast({ title: successTip, icon: "none" })
      await this.loadDetail()
    } catch (error) {
      wx.showToast({ title: error?.message || "提交失败", icon: "none" })
    } finally {
      this.setData({ loading: false })
    }
  },
  start() {
    this.updateStatus(2, "service started", "已进入服务中", {
      beforeImages: this.data.beforeImages
    })
  },
  finish() {
    this.updateStatus(3, "worker confirmed finish", "已提交结束确认", {
      afterImages: this.data.afterImages,
      chargeAmount: this.data.chargeAmount === "" ? null : Number(this.data.chargeAmount),
      chargeRemark: this.data.chargeRemark
    })
  }
})
