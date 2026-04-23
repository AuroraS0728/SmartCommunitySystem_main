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

function normalizeAmount(raw) {
  const text = String(raw ?? "").trim()
  if (!text) {
    return 0
  }
  const n = Number(text)
  if (!Number.isFinite(n)) {
    return null
  }
  return n
}

Page({
  data: {
    id: null,
    order: null,
    loading: false,
    verifying: false,
    beforeImages: "",
    afterImages: "",
    verifyCode: "",
    verifyPassed: false,
    pendingWorkerIds: [],
    participants: [],
    workerId: null,
    techFee: "",
    materialFee: "",
    highAltitudeFee: "",
    otherFee: "",
    feeRemark: ""
  },

  onLoad(query) {
    const userInfo = wx.getStorageSync("userInfo") || {}
    this.setData({
      id: Number(query.id) || null,
      workerId: userInfo?.id ? Number(userInfo.id) : null
    })
    this.loadDetail()
  },

  async loadDetail() {
    if (!this.data.id) return
    try {
      const detail = await request({ url: `/repair/${this.data.id}` })
      const order = detail?.order || null
      const participants = Array.isArray(detail?.participants) ? detail.participants : []
      const feeDetails = Array.isArray(detail?.repairFeeDetails) ? detail.repairFeeDetails : []
      const selfFee = feeDetails.find((item) => Number(item?.workerId) === Number(this.data.workerId)) || {}
      this.setData({
        order,
        beforeImages: order?.beforeImages || "",
        afterImages: order?.afterImages || "",
        verifyPassed: !!detail?.verifyPassed,
        pendingWorkerIds: Array.isArray(detail?.pendingWorkerIds) ? detail.pendingWorkerIds : [],
        participants,
        techFee: selfFee?.techFee == null ? "" : String(selfFee.techFee),
        materialFee: selfFee?.materialFee == null ? "" : String(selfFee.materialFee),
        highAltitudeFee: selfFee?.highAltitudeFee == null ? "" : String(selfFee.highAltitudeFee),
        otherFee: selfFee?.otherFee == null ? "" : String(selfFee.otherFee),
        feeRemark: selfFee?.remark || ""
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

  onVerifyCodeInput(e) {
    this.setData({ verifyCode: e.detail.value.trim() })
  },

  onFeeInput(e) {
    const field = e?.currentTarget?.dataset?.field
    if (!field) return
    this.setData({ [field]: e.detail.value })
  },

  onFeeRemarkInput(e) {
    this.setData({ feeRemark: e.detail.value })
  },

  async submitVerifyCode() {
    if (!this.data.id || this.data.verifying) return
    if (!this.data.verifyCode) {
      wx.showToast({ title: "请输入现场验证码", icon: "none" })
      return
    }
    this.setData({ verifying: true })
    try {
      const resp = await request({
        url: "/worker/verify-code",
        method: "POST",
        data: {
          orderId: this.data.id,
          code: this.data.verifyCode
        }
      })
      if (resp?.pass) {
        wx.showToast({ title: "验证码校验通过", icon: "success" })
        await this.loadDetail()
        return
      }
      wx.showToast({ title: "验证码不正确", icon: "none" })
    } catch (error) {
      wx.showToast({ title: error?.message || "校验失败", icon: "none" })
    } finally {
      this.setData({ verifying: false })
    }
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
    const current = Number(this.data.order?.status || 0)
    if (current === 2) {
      wx.showToast({ title: "工单已在服务中", icon: "none" })
      return
    }
    if (!this.data.verifyPassed) {
      const pending = this.data.pendingWorkerIds.join(",")
      wx.showToast({ title: pending ? `待核验工人:${pending}` : "请先完成现场核验", icon: "none" })
      return
    }
    this.updateStatus(2, "service started", "已进入服务中", {
      beforeImages: this.data.beforeImages
    })
  },

  finish() {
    const current = Number(this.data.order?.status || 0)
    if (current !== 2) {
      wx.showToast({ title: "请先开始服务", icon: "none" })
      return
    }
    const techFee = normalizeAmount(this.data.techFee)
    const materialFee = normalizeAmount(this.data.materialFee)
    const highAltitudeFee = normalizeAmount(this.data.highAltitudeFee)
    const otherFee = normalizeAmount(this.data.otherFee)
    if ([techFee, materialFee, highAltitudeFee, otherFee].some((v) => v === null || v < 0)) {
      wx.showToast({ title: "费用格式不正确", icon: "none" })
      return
    }
    const total = Number(techFee + materialFee + highAltitudeFee + otherFee)
    this.updateStatus(3, "worker confirmed finish", "已提交结束确认", {
      afterImages: this.data.afterImages,
      chargeAmount: total,
      chargeRemark: this.data.feeRemark || "",
      workerFees: [
        {
          workerId: Number(this.data.workerId),
          techFee,
          materialFee,
          highAltitudeFee,
          otherFee,
          remark: this.data.feeRemark || ""
        }
      ]
    })
  }
})

