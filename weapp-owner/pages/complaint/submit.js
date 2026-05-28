const { request } = require('../../api/request')

function formatTime(value) {
  if (!value) return ''
  return String(value).replace('T', ' ').slice(0, 16)
}

function statusText(status) {
  const map = {
    1: '待处理',
    2: '处理中',
    3: '已回复'
  }
  return map[Number(status)] || '待处理'
}

Page({
  data: {
    title: '',
    content: '',
    submitting: false,
    loading: false,
    list: []
  },
  onShow() {
    this.loadList()
  },
  onTitle(e) {
    this.setData({ title: e.detail.value })
  },
  onContent(e) {
    this.setData({ content: e.detail.value })
  },
  async loadList() {
    this.setData({ loading: true })
    try {
      const rawList = await request({ url: '/complaint/list' })
      const list = (Array.isArray(rawList) ? rawList : []).map((item) => ({
        ...item,
        statusText: statusText(item.status),
        createTimeText: formatTime(item.createTime),
        replyTimeText: formatTime(item.replyTime)
      }))
      this.setData({ list })
    } catch (error) {
      this.setData({ list: [] })
    } finally {
      this.setData({ loading: false })
    }
  },
  async submit() {
    if (this.data.submitting) return

    const title = (this.data.title || '').trim()
    const content = (this.data.content || '').trim()
    if (!title || !content) {
      wx.showToast({ title: '请填写完整主题和内容', icon: 'none' })
      return
    }

    this.setData({ submitting: true })
    try {
      const complaint = await request({
        url: '/complaint/submit',
        method: 'POST',
        data: { type: 1, title, content, images: '[]' }
      })
      const risk = complaint?.riskLevel ? `，风险${complaint.riskLevel}` : ''
      wx.showToast({ title: `提交成功${risk}`, icon: 'none' })
      this.setData({ title: '', content: '' })
      await this.loadList()
    } catch (error) {
      wx.showToast({ title: error?.message || '提交失败', icon: 'none' })
    } finally {
      this.setData({ submitting: false })
    }
  }
})
