const { request } = require("../../../api/request")

const LOOP_INTERVAL_MS = 1200
const FACE_PASS_THRESHOLD = 0.7
const REQUIRED_CONSECUTIVE_PASS = 1

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

function formatScore(value) {
  return toNumber(value).toFixed(4)
}

function passThreshold(item) {
  const threshold = toNumber(item?.threshold, FACE_PASS_THRESHOLD)
  return threshold > 0 ? threshold : FACE_PASS_THRESHOLD
}

function workerDisplayName(workerId, rawName) {
  const id = Number(workerId)
  const text = String(rawName || "").trim()
  if (id === 20 || /^HK-Clean-01$/i.test(text)) return "宋庆涵"
  if (/^HK-Clean-(\d+)$/i.test(text)) return `家政保洁员${text.match(/^HK-Clean-(\d+)$/i)[1]}`
  if (/^Repair-Plumber-(\d+)$/i.test(text)) return `水工维修员${text.match(/^Repair-Plumber-(\d+)$/i)[1]}`
  if (/^Repair-Electric-(\d+)$/i.test(text)) return `电工维修员${text.match(/^Repair-Electric-(\d+)$/i)[1]}`
  if (/^Repair-Appliance-(\d+)$/i.test(text)) return `家电维修员${text.match(/^Repair-Appliance-(\d+)$/i)[1]}`
  if (/^Repair-Outsource-(\d+)$/i.test(text)) return `外包维修团队${text.match(/^Repair-Outsource-(\d+)$/i)[1]}`
  return text || `工人${workerId || "--"}`
}

function facePreChecksPassed(item) {
  return !!item?.livenessPassed
    && item?.maskPassed !== false
    && item?.eyesOpenPassed !== false
    && item?.qualityPassed !== false
}

function facePassed(item) {
  return toNumber(item?.score) >= passThreshold(item)
}

function faceFailureReasons(item) {
  const reasons = []
  if (!item) return ["未返回核验结果"]

  if (item.livenessPassed === false) {
    reasons.push(`活体未通过${item.livenessLabel ? `（${item.livenessLabel}）` : ""}`)
  }
  if (item.maskCheckSupported && item.maskPassed === false) {
    reasons.push("口罩/遮挡未通过，请摘掉口罩并露出完整面部")
  }
  if (item.eyeStateCheckSupported && item.eyesOpenPassed === false) {
    const left = item.leftEyeLabel || "--"
    const right = item.rightEyeLabel || "--"
    reasons.push(`眼睛状态未通过，请睁眼正对镜头（左眼 ${left}，右眼 ${right}）`)
  }
  if (item.qualityCheckSupported && item.qualityPassed === false) {
    const level = item.qualityLevelLabel || "--"
    const score = formatScore(item.qualityScore)
    reasons.push(`图片质量低，请靠近镜头、光线充足并保持稳定（质量 ${level}，得分 ${score}）`)
  }
  if (!reasons.length || !facePassed(item)) {
    reasons.push(`相似度低于阈值（${formatScore(item.score)} / ${formatScore(passThreshold(item))}）`)
  }
  return reasons
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
    verifyStateText: "现场核验状态加载中",
    siteVerifyPassed: false,
    pendingWorkerIds: [],
    pendingWorkerText: "",
    resultRows: [],
    checkDetailRows: [],
    score: 0,
    threshold: 0,
    scoreText: "0.0000",
    thresholdText: "0.0000",
    livenessLabel: "--"
  },

  onLoad(query) {
    const orderId = query?.orderId ? String(query.orderId) : ""
    const userInfo = wx.getStorageSync("userInfo") || {}
    const workerId = userInfo?.id ? String(userInfo.id) : ""
    this.setData({ orderId, workerId })
    this.cameraContext = wx.createCameraContext()
    this.loopTimer = null
    this.loadOrderVerifyState()
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
      pendingWorkerText: "",
      resultRows: [],
      checkDetailRows: [],
      score: 0,
      threshold: 0,
      scoreText: "0.0000",
      thresholdText: "0.0000",
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

  async loadOrderVerifyState() {
    if (!this.data.orderId) {
      this.setData({
        verifyStateText: "未绑定工单",
        siteVerifyPassed: false
      })
      return
    }
    try {
      const detail = await request({ url: `/repair/${this.data.orderId}` })
      const participants = Array.isArray(detail?.participants) ? detail.participants : []
      const self = participants.find((item) => String(item?.workerId) === String(this.data.workerId)) || null
      const siteVerifyPassed = !!detail?.verifyPassed || !!self?.verifyPassed
      this.setData({
        siteVerifyPassed,
        verifyStateText: siteVerifyPassed ? "现场核验已通过，可进入服务流程" : "现场核验未通过，请完成验证码或人脸核验"
      })
    } catch (error) {
      this.setData({
        verifyStateText: "现场核验状态加载失败",
        siteVerifyPassed: false
      })
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
          quality: "high",
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
    const pass = facePassed(resp)
    const attempts = this.data.attempts + 1
    const passCount = pass ? this.data.passCount + 1 : 0
    const score = toNumber(resp?.score)
    const threshold = toNumber(resp?.threshold)
    this.setData({
      attempts,
      passCount,
      livenessLabel: resp?.livenessLabel || "--",
      score,
      threshold,
      scoreText: formatScore(score),
      thresholdText: formatScore(threshold),
      statusText: `第${attempts}次：活体=${resp?.livenessLabel || "--"}，相似度=${formatScore(score)}`,
      resultRows: [],
      checkDetailRows: pass ? [] : faceFailureReasons(resp),
      pendingWorkerIds: pass ? [] : [Number(this.data.workerId)],
      pendingWorkerText: pass ? "" : String(this.data.workerId)
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
    const results = Array.isArray(resp?.results) ? resp.results : []
    const self = results.find((item) => String(item?.workerId) === String(this.data.workerId)) || {}
    const selfPassed = facePassed(self)
    const pass = selfPassed
    const passCount = pass ? this.data.passCount + 1 : 0
    const rows = results.map((item) => {
      const matched = facePassed(item)
      const name = workerDisplayName(item?.workerId, item?.workerName)
      const score = formatScore(item?.score)
      const threshold = formatScore(item?.threshold)
      return `${name}: ${matched ? "通过" : "未通过"} (相似度 ${score} / 阈值 ${threshold})`
    })
    const pendingWorkerIds = pass ? [] : results
      .filter((item) => String(item?.workerId) === String(this.data.workerId) && !facePassed(item))
      .map((item) => item?.workerId)
      .filter(Boolean)
    const pendingWorkerText = pendingWorkerIds.join(",")
    const checkDetailRows = pass ? [] : results
      .filter((item) => String(item?.workerId) === String(this.data.workerId) && !facePassed(item))
      .flatMap((item) => {
        const name = workerDisplayName(item?.workerId, item?.workerName)
        return faceFailureReasons(item).map((reason) => `${name}: ${reason}`)
      })
    const score = toNumber(self?.score)
    const threshold = toNumber(self?.threshold)
    this.setData({
      attempts,
      passCount,
      livenessLabel: self?.livenessLabel || "--",
      score,
      threshold,
      scoreText: formatScore(score),
      thresholdText: formatScore(threshold),
      pendingWorkerIds,
      pendingWorkerText,
      resultRows: rows,
      checkDetailRows,
      statusText: pass
        ? `第${attempts}次：本次人脸核验通过`
        : `第${attempts}次：本次人脸未通过，待通过工人 ${pendingWorkerText || "--"}`,
      siteVerifyPassed: pass || !!resp?.allVerified || this.data.siteVerifyPassed,
      verifyStateText: (pass || !!resp?.allVerified || this.data.siteVerifyPassed)
        ? "现场核验已通过，可进入服务流程"
        : "现场核验未通过，请继续检测"
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
      statusText: "相似度达标，核验通过",
      siteVerifyPassed: true,
      verifyStateText: "现场核验已通过，可进入服务流程",
      pendingWorkerIds: [],
      pendingWorkerText: "",
      checkDetailRows: []
    })
    wx.showToast({ title: "核验通过", icon: "success" })
    setTimeout(() => {
      if (this.data.orderId) {
        wx.navigateTo({ url: `/pages/worker/work/process?id=${this.data.orderId}` })
      } else {
        wx.navigateTo({ url: "/pages/worker/task/list" })
      }
    }, 450)
  },

  goProcess() {
    if (!this.data.orderId) {
      wx.showToast({ title: "工单ID无效", icon: "none" })
      return
    }
    wx.navigateTo({ url: `/pages/worker/work/process?id=${this.data.orderId}` })
  }
})
