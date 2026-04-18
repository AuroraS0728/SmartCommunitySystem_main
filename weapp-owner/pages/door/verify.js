const { request } = require('../../api/request')

function pad2(num) {
  return String(num).padStart(2, '0')
}

function formatSeconds(seconds) {
  const sec = Number(seconds || 0)
  if (sec <= 0) return '00:00'
  const m = Math.floor(sec / 60)
  const s = sec % 60
  return `${pad2(m)}:${pad2(s)}`
}

Page({
  data: {
    code: '',
    dynamicToken: '',
    qrContent: '',
    qrImageUrl: '',
    countdown: 0,
    countdownText: '00:00',
    refreshing: false,
    refreshTimer: null,
    countdownTimer: null
  },

  onLoad(options) {
    if (options && options.code) {
      this.setData({ code: String(options.code).toUpperCase() })
    }
  },

  onShow() {
    if (this.data.code) {
      this.startDynamicFlow()
    }
  },

  onHide() {
    this.stopTimers()
  },

  onUnload() {
    this.stopTimers()
  },

  onCode(e) {
    this.setData({ code: String(e.detail.value || '').toUpperCase() })
  },

  stopTimers() {
    if (this.data.refreshTimer) {
      clearInterval(this.data.refreshTimer)
    }
    if (this.data.countdownTimer) {
      clearInterval(this.data.countdownTimer)
    }
    this.setData({
      refreshTimer: null,
      countdownTimer: null
    })
  },

  startCountdownTimer() {
    if (this.data.countdownTimer) {
      clearInterval(this.data.countdownTimer)
    }
    const timer = setInterval(() => {
      const left = Number(this.data.countdown || 0)
      const next = left > 0 ? left - 1 : 0
      this.setData({
        countdown: next,
        countdownText: formatSeconds(next)
      })
    }, 1000)
    this.setData({ countdownTimer: timer })
  },

  startRefreshTimer() {
    if (this.data.refreshTimer) {
      clearInterval(this.data.refreshTimer)
    }
    const timer = setInterval(() => {
      this.refreshDynamicToken()
    }, 30000)
    this.setData({ refreshTimer: timer })
  },

  async startDynamicFlow() {
    this.stopTimers()
    await this.refreshDynamicToken()
    this.startCountdownTimer()
    this.startRefreshTimer()
  },

  async refreshDynamicToken() {
    if (this.data.refreshing) return
    const code = (this.data.code || '').trim()
    if (!code) {
      wx.showToast({ title: '请输入邀请码', icon: 'none' })
      return
    }

    this.setData({ refreshing: true })
    try {
      const data = await request({
        url: '/access/invite/dynamic-token',
        method: 'POST',
        skipAuth: true,
        data: { code }
      })
      const qrContent = data?.qrContent || ''
      this.setData({
        dynamicToken: data?.token || '',
        qrContent,
        qrImageUrl: qrContent
          ? `https://api.qrserver.com/v1/create-qr-code/?size=280x280&data=${encodeURIComponent(qrContent)}`
          : '',
        countdown: Number(data?.expireSeconds || 30),
        countdownText: formatSeconds(Number(data?.expireSeconds || 30))
      })
    } catch (error) {
      wx.showToast({ title: error?.message || '刷新二维码失败', icon: 'none' })
    } finally {
      this.setData({ refreshing: false })
    }
  }
})
