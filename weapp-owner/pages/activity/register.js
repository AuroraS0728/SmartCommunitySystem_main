const { request } = require("../../api/request")

Page({
  data: {
    id: '',
    submitting: false,
    form: {
      nickname: '',
      phone: '',
      age: '',
      hasChild: 0,
      hasPet: 0,
      remark: ''
    }
  },

  onLoad(options) {
    this.setData({ id: options.id || '' })
  },

  handleInput(event) {
    const field = event.currentTarget.dataset.field
    this.setData({ [`form.${field}`]: event.detail.value })
  },

  handleSwitch(event) {
    const field = event.currentTarget.dataset.field
    this.setData({ [`form.${field}`]: event.detail.value ? 1 : 0 })
  },

  async submit() {
    if (!this.data.form.nickname || !this.data.form.phone || !this.data.form.age) {
      wx.showToast({ title: '请填写姓名、手机号和年龄', icon: 'none' })
      return
    }
    this.setData({ submitting: true })
    try {
      await request({
        url: `/activity/${this.data.id}/register`,
        method: 'POST',
        data: {
          ...this.data.form,
          age: Number(this.data.form.age)
        }
      })
      wx.showToast({ title: '报名成功', icon: 'success' })
      setTimeout(() => {
        wx.redirectTo({ url: `/pages/activity/detail?id=${this.data.id}` })
      }, 600)
    } catch (error) {
      wx.showToast({ title: error?.message || '报名失败', icon: 'none' })
    } finally {
      this.setData({ submitting: false })
    }
  }
})
