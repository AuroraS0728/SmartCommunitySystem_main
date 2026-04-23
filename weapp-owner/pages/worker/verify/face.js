const { request } = require("../../../api/request")

const LOOP_INTERVAL_MS = 1200
const REQUIRED_CONSECUTIVE_PASS = 2

function readBase64(filePath) {
  return new Promise((resolve, reject) => {
    wx.getFileSystemManager().readFile({
      filePath,
      encoding: "base64",
      success: (res) => resolve(res.data),
      fail: () => reject(new Error("图片读取失败"))
    })
  })
}

function toNumber(v, fallback = 0) {
  const n = Number(v)
  return Number.isFinite(n) ? n : fallback
}

Page({
  data: {
    orderId: "",
    workerId: "",
    running: false,
    inFlight: false,
    passCount: 0,
    attempts: 0,
    statusText: "未开始检测",
    pendingWorkerIds: [],
    resultRows: [],
    score: 0,
    threshold: 0,
    livenessLabel: "--"
  },

  onLoad(query) {
    const orderId = query?.orderId ? String(query.orderId) : ""
    const userInfo = wx.getStorageSync("userInfo") || {}
    const workerId = userInfo?.id ? String(userInfo.id) : ""
    this.setData({ orderId, workerId })
    this.cameraContext = wx.createCameraContext()
    this.loopTimer = null
  },

  onHide() {
    this.stopLoop()
  },

  onUnload() {
    this.stopLoop()
  },

  startLiveVerify() {
    if (this.data.running) return
    if (!this.data.workerId) {
      wx.showToast({ title: "未获取到工人身份", icon: "none" })
      return
    }
    this.setData({
      running: true,
      inFlight: false,
      passCount: 0,
      attempts: 0,
      statusText: "实时检测中...",
      pendingWorkerIds: [],
      resultRows: [],
      score: 0,
      threshold: 0,
      livenessLabel: "--"
    })
    this.runLoopOnce()
  },

  stopLiveVerify() {
    this.stopLoop()
    this.setData({
      running: false,
      inFlight: false,
      statusText: "检测已停止"
    })
  },

  stopLoop() {
    if (this.loopTimer) {
      clearTimeout(this.loopTimer)
      this.loopTimer = null
    }
  },

  scheduleNext() {
    this.stopLoop()
    if (!this.data.running) return
    this.loopTimer = setTimeout(() => this.runLoopOnce(), LOOP_INTERVAL_MS)
  },

  async runLoopOnce() {
    if (!this.data.running || this.data.inFlight) {
      this.scheduleNext()
      return
    }
    this.setData({ inFlight: true })
    try {
      const photo = await new Promise((resolve, reject) => {
        this.cameraContext.takePhoto({
          quality: "low",
          success: resolve,
          fail: reject
        })
      })
      const path = photo?.tempImagePath
      if (!path) {
        throw new Error("相机取帧失败")
      }
      const imageBase64 = await readBase64(path)

      if (this.data.orderId) {
        await this.verifyMulti(imageBase64)
      } else {
        await this.verifySingle(imageBase64)
      }
    } catch (error) {
      this.setData({
        statusText: error?.message || "实时检测失败"
      })
    } finally {
      this.setData({ inFlight: false })
      this.scheduleNext()
    }
  },

  async verifySingle(imageBase64) {
    const resp = await request({
      url: "/face/live-verify",
      method: "POST",
      data: {
        workerId: Number(this.data.workerId),
        imageBase64
      }
    })
    const livenessPassed = !!resp?.livenessPassed
    const match = !!resp?.match
    const pass = livenessPassed && match
    const attempts = this.data.attempts + 1
    const passCount = pass ? this.data.passCount + 1 : 0
    this.setData({
      attempts,
      passCount,
      livenessLabel: resp?.livenessLabel || "--",
      score: toNumber(resp?.score),
      threshold: toNumber(resp?.threshold),
      statusText: `第${attempts}次：活体=${resp?.livenessLabel || "--"}，相似度=${toNumber(resp?.score).toFixed(4)}`,
      resultRows: [],
      pendingWorkerIds: pass ? [] : [Number(this.data.workerId)]
    })
    if (passCount >= REQUIRED_CONSECUTIVE_PASS) {
      this.onVerifySuccess()
    }
  },

  async verifyMulti(imageBase64) {
    const resp = await request({
      url: "/face/live-verify-multi",
      method: "POST",
      data: {
        orderId: Number(this.data.orderId),
        imageBase64
      }
    })
    const attempts = this.data.attempts + 1
    const allVerified = !!resp?.allVerified
    const passCount = allVerified ? this.data.passCount + 1 : 0
    const results = Array.isArray(resp?.results) ? resp.results : []
    const self = results.find((item) => String(item?.workerId) === String(this.data.workerId)) || {}
    const rows = results.map((item) => {
      const matched = !!item?.match
      const name = item?.workerName || `工人${item?.workerId || "--"}`
      const score = toNumber(item?.score).toFixed(4)
      return `${name}: ${matched ? "通过" : "未通过"} (${score})`
    })
    const pendingWorkerIds = Array.isArray(resp?.pendingWorkerIds) ? resp.pendingWorkerIds : []
    this.setData({
      attempts,
      passCount,
      livenessLabel: self?.livenessLabel || "--",
      score: toNumber(self?.score),
      threshold: toNumber(self?.threshold),
      pendingWorkerIds,
      resultRows: rows,
      statusText: allVerified
        ? `第${attempts}次：全部工人核验通过`
        : `第${attempts}次：待通过工人 ${pendingWorkerIds.join(",") || "--"}`
    })
    if (passCount >= REQUIRED_CONSECUTIVE_PASS) {
      this.onVerifySuccess()
    }
  },

  onVerifySuccess() {
    this.stopLoop()
    this.setData({
      running: false,
      inFlight: false,
      statusText: "连续检测通过，核验成功"
    })
    wx.showToast({ title: "核验通过", icon: "success" })
    setTimeout(() => {
      if (this.data.orderId) {
        wx.navigateTo({ url: `/pages/worker/work/process?id=${this.data.orderId}` })
      } else {
        wx.navigateTo({ url: "/pages/worker/task/list" })
      }
    }, 450)
  }
})

