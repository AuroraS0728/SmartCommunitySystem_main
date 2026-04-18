const { request } = require('../../api/request')

function pad2(num) {
  return String(num).padStart(2, '0')
}

function toDateTimeParts(date) {
  return {
    date: `${date.getFullYear()}-${pad2(date.getMonth() + 1)}-${pad2(date.getDate())}`,
    time: `${pad2(date.getHours())}:${pad2(date.getMinutes())}`
  }
}

function formatCountdown(seconds) {
  const sec = Number(seconds || 0)
  if (sec <= 0) return '已到期'
  const h = Math.floor(sec / 3600)
  const m = Math.floor((sec % 3600) / 60)
  const s = sec % 60
  return `${pad2(h)}:${pad2(m)}:${pad2(s)}`
}

Page({
  data: {
    visitorName: '',
    visitorPhone: '',
    visitDate: '',
    visitClock: '',
    validityType: 'SINGLE_2H',
    customDate: '',
    customClock: '',
    submitting: false,
    latestInvite: null,
    invites: [],
    visitors: [],
    notifications: [],
    ticker: null
  },

  onLoad() {
    const now = new Date()
    const visitAt = new Date(now.getTime() + 10 * 60 * 1000)
    const customAt = new Date(now.getTime() + 2 * 60 * 60 * 1000)
    const visit = toDateTimeParts(visitAt)
    const custom = toDateTimeParts(customAt)
    this.setData({
      visitDate: visit.date,
      visitClock: visit.time,
      customDate: custom.date,
      customClock: custom.time
    })
  },

  onShow() {
    this.loadAll()
    this.startTicker()
  },

  onHide() {
    this.stopTicker()
  },

  onUnload() {
    this.stopTicker()
  },

  startTicker() {
    this.stopTicker()
    const timer = setInterval(() => {
      const list = (this.data.invites || []).map((item) => {
        if (item.status !== 'UNUSED') return item
        const left = Number(item.countdownSeconds || 0)
        if (left <= 0) {
          return {
            ...item,
            countdownSeconds: 0,
            countdownText: '已到期',
            status: 'EXPIRED',
            statusText: '已过期',
            canRenew: true
          }
        }
        return { ...item, countdownSeconds: left - 1, countdownText: formatCountdown(left - 1) }
      })
      this.setData({ invites: list })
    }, 1000)
    this.setData({ ticker: timer })
  },

  stopTicker() {
    if (this.data.ticker) {
      clearInterval(this.data.ticker)
      this.setData({ ticker: null })
    }
  },

  onName(e) {
    this.setData({ visitorName: e.detail.value })
  },

  onPhone(e) {
    this.setData({ visitorPhone: e.detail.value })
  },

  onVisitDateChange(e) {
    this.setData({ visitDate: e.detail.value })
  },

  onVisitClockChange(e) {
    this.setData({ visitClock: e.detail.value })
  },

  onCustomDateChange(e) {
    this.setData({ customDate: e.detail.value })
  },

  onCustomClockChange(e) {
    this.setData({ customClock: e.detail.value })
  },

  chooseValidity(e) {
    this.setData({ validityType: e.currentTarget.dataset.type })
  },

  useHistoryVisitor(e) {
    const idx = Number(e.currentTarget.dataset.index)
    const item = this.data.visitors[idx]
    if (!item) return
    this.setData({
      visitorName: item.visitorName || '',
      visitorPhone: item.visitorPhone || ''
    })
    wx.showToast({ title: '已填入访客信息' })
  },

  composeDateTime(dateText, clockText) {
    if (!dateText || !clockText) return ''
    return `${dateText} ${clockText}:00`
  },

  normalizeInvites(rawList) {
    return (rawList || []).map((item) => ({
      ...item,
      countdownSeconds: Number(item.countdownSeconds || 0),
      countdownText: formatCountdown(item.countdownSeconds),
      canRenew: !!item.canRenew
    }))
  },

  async loadAll() {
    await Promise.all([this.loadInvites(), this.loadVisitors(), this.loadNotifications()])
  },

  async loadInvites() {
    try {
      const list = await request({ url: '/access/visitor-records' })
      this.setData({ invites: this.normalizeInvites(Array.isArray(list) ? list : []) })
    } catch (error) {
      wx.showToast({ title: error?.message || '加载访客记录失败', icon: 'none' })
    }
  },

  async loadVisitors() {
    try {
      const list = await request({ url: '/access/my-visitors' })
      this.setData({ visitors: Array.isArray(list) ? list : [] })
    } catch (error) {
      this.setData({ visitors: [] })
    }
  },

  async loadNotifications() {
    try {
      const list = await request({ url: '/access/owner-notifications' })
      this.setData({ notifications: Array.isArray(list) ? list : [] })
    } catch (error) {
      this.setData({ notifications: [] })
    }
  },

  async submitInvite() {
    if (this.data.submitting) return

    const visitorName = (this.data.visitorName || '').trim()
    const visitorPhone = (this.data.visitorPhone || '').trim()
    if (!visitorName || !visitorPhone) {
      wx.showToast({ title: '请填写访客姓名和手机号', icon: 'none' })
      return
    }

    const visitTime = this.composeDateTime(this.data.visitDate, this.data.visitClock)
    if (!visitTime) {
      wx.showToast({ title: '请选择来访时间', icon: 'none' })
      return
    }

    const payload = {
      visitorName,
      visitorPhone,
      visitTime,
      validityType: this.data.validityType
    }

    if (this.data.validityType === 'CUSTOM') {
      const customExpireTime = this.composeDateTime(this.data.customDate, this.data.customClock)
      if (!customExpireTime) {
        wx.showToast({ title: '请选择自定义有效期', icon: 'none' })
        return
      }
      payload.customExpireTime = customExpireTime
    }

    this.setData({ submitting: true })
    try {
      const data = await request({
        url: '/access/invite',
        method: 'POST',
        data: payload
      })
      this.setData({ latestInvite: data || null })
      wx.showToast({ title: '邀请码已生成' })
      await this.loadAll()
    } catch (error) {
      wx.showToast({ title: error?.message || '生成失败', icon: 'none' })
    } finally {
      this.setData({ submitting: false })
    }
  },

  async renewInvite(e) {
    const id = e.currentTarget.dataset.id
    if (!id) return

    wx.showActionSheet({
      itemList: ['单次有效（2小时）', '当天有效（截止23:59）', '使用当前自定义时间'],
      success: async (res) => {
        const validityType = res.tapIndex === 0 ? 'SINGLE_2H' : res.tapIndex === 1 ? 'TODAY_END' : 'CUSTOM'
        const payload = { validityType }

        if (validityType === 'CUSTOM') {
          payload.customExpireTime = this.composeDateTime(this.data.customDate, this.data.customClock)
          if (!payload.customExpireTime) {
            wx.showToast({ title: '请先选择自定义有效期', icon: 'none' })
            return
          }
        }

        payload.visitTime = this.composeDateTime(this.data.visitDate, this.data.visitClock)

        try {
          const data = await request({
            url: `/access/invite/${id}/renew`,
            method: 'POST',
            data: payload
          })
          this.setData({ latestInvite: data || null })
          wx.showToast({ title: '续期成功' })
          await this.loadAll()
        } catch (error) {
          wx.showToast({ title: error?.message || '续期失败', icon: 'none' })
        }
      }
    })
  },

  copyCode() {
    const code = this.data.latestInvite?.code
    if (!code) return
    wx.setClipboardData({ data: code })
  },

  copyShareLink() {
    const link = this.data.latestInvite?.shareLink
    if (!link) return
    wx.setClipboardData({ data: link })
  },

  openDynamicVerify() {
    const code = this.data.latestInvite?.code
    if (!code) {
      wx.showToast({ title: '请先生成邀请码', icon: 'none' })
      return
    }
    wx.navigateTo({ url: `/pages/door/verify?code=${code}` })
  },

  onShareAppMessage() {
    const code = this.data.latestInvite?.code
    if (!code) {
      return {
        title: '访客核验邀请',
        path: '/pages/door/verify'
      }
    }
    return {
      title: `访客邀请码：${code}`,
      path: `/pages/door/verify?code=${code}`
    }
  },

  async removeInvite(e) {
    const id = e.currentTarget.dataset.id
    if (!id) return
    try {
      await request({ url: `/access/invite/${id}`, method: 'DELETE' })
      wx.showToast({ title: '已删除' })
      await this.loadAll()
    } catch (error) {
      wx.showToast({ title: error?.message || '删除失败', icon: 'none' })
    }
  }
})
