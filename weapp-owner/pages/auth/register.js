const { request } = require('../../api/request')

Page({
  data: {
    phone: '',
    propertyCode: '',
    loading: false
  },

  onPhoneInput(e) {
    this.setData({ phone: (e.detail.value || '').trim() })
  },

  onPropertyCodeInput(e) {
    this.setData({ propertyCode: (e.detail.value || '').trim().toUpperCase() })
  },

  async submit() {
    if (this.data.loading) return
    const phone = this.data.phone
    const propertyCode = this.data.propertyCode
    if (!/^1\d{10}$/.test(phone)) {
      wx.showToast({ title: '请输入11位手机号', icon: 'none' })
      return
    }
    if (!/^YZ\d{8}$/.test(propertyCode)) {
      wx.showToast({ title: '房产号格式应为 YZ + 8位数字', icon: 'none' })
      return
    }

    this.setData({ loading: true })
    try {
      const data = await request({
        url: '/auth/owner-register',
        method: 'POST',
        data: { phone, propertyCode },
        skipAuth: true
      })
      wx.setStorageSync('prefillOwnerLogin', {
        account: data?.account || propertyCode,
        password: data?.initialPassword || ''
      })
      wx.showModal({
        title: '注册成功',
        content: `账号：${data?.account || propertyCode}\n初始密码：${data?.initialPassword || ''}\n请使用初始密码登录并立即修改密码。`,
        showCancel: false,
        success: () => {
          wx.navigateBack({ delta: 1 })
        }
      })
    } catch (error) {
      wx.showToast({ title: error?.message || '注册失败', icon: 'none' })
    } finally {
      this.setData({ loading: false })
    }
  }
})
