const { request } = require('../../api/request')

Page({
  data: { title: '', content: '', submitting: false },
  onTitle(e) {
    this.setData({ title: e.detail.value })
  },
  onContent(e) {
    this.setData({ content: e.detail.value })
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
    } catch (error) {
      wx.showToast({ title: error?.message || '提交失败', icon: 'none' })
    } finally {
      this.setData({ submitting: false })
    }
  }
})
