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

function satisfactionText(value) {
  const map = {
    1: '非常不满意',
    2: '不满意',
    3: '一般',
    4: '满意',
    5: '非常满意'
  }
  return map[Number(value)] || ''
}

Page({
  data: {
    title: '',
    content: '',
    submitting: false,
    loading: false,
    ratingId: null,
    ratingOptions: [1, 2, 3, 4, 5],
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
        replyTimeText: formatTime(item.replyTime),
        satisfactionText: satisfactionText(item.satisfaction),
        canRate: !!item.reply && !item.satisfaction
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
      await request({
        url: '/complaint/submit',
        method: 'POST',
        data: { type: 1, title, content, images: '[]' }
      })
      wx.showToast({ title: '提交成功', icon: 'success' })
      this.setData({ title: '', content: '' })
      await this.loadList()
    } catch (error) {
      wx.showToast({ title: error?.message || '提交失败', icon: 'none' })
    } finally {
      this.setData({ submitting: false })
    }
  },
  async submitSatisfaction(e) {
    const id = Number(e.currentTarget.dataset.id)
    const score = Number(e.currentTarget.dataset.score)
    if (!id || !score || this.data.ratingId) return

    this.setData({ ratingId: id })
    try {
      await request({
        url: `/complaint/${id}/satisfaction?satisfaction=${score}`,
        method: 'POST'
      })
      wx.showToast({ title: '评价成功', icon: 'success' })
      await this.loadList()
    } catch (error) {
      wx.showToast({ title: error?.message || '评价失败', icon: 'none' })
    } finally {
      this.setData({ ratingId: null })
    }
  }
})
