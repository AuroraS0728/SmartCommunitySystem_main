const { request } = require("../../api/request")

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
    beforeImages: "",
    afterImages: "",
    chargeAmount: "",
    chargeRemark: ""
  },
  onLoad(q) {
    this.setData({ id: Number(q.id) })
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
      const path = media?.tempFiles?.[0]?.tempFilePath
      if (!path) return
      const base64 = await readFileBase64(path)
      this.setData({ [field]: JSON.stringify([`data:image/jpeg;base64,${base64}`]) })
      wx.showToast({ title: "已上传", icon: "success" })
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
  async start() {
    await request({
      url: "/repair/status",
      method: "POST",
      data: {
        orderId: this.data.id,
        status: 2,
        remark: "维修中",
        beforeImages: this.data.beforeImages
      }
    })
    wx.showToast({ title: "已开始" })
  },
  async finish() {
    await request({
      url: "/repair/status",
      method: "POST",
      data: {
        orderId: this.data.id,
        status: 3,
        remark: "维修端已确认结束",
        afterImages: this.data.afterImages,
        chargeAmount: this.data.chargeAmount === "" ? null : Number(this.data.chargeAmount),
        chargeRemark: this.data.chargeRemark
      }
    })
    wx.showToast({ title: "已提交结束确认", icon: "none" })
  }
})
