const { request } = require('../../../api/request')

function extractToken(raw) {
  const text = String(raw || '').trim()
  if (!text) return ''
  const tokenKey = 'token='
  const idx = text.indexOf(tokenKey)
  if (idx >= 0) {
    return text.slice(idx + tokenKey.length).split('&')[0].trim()
  }
  return text
}

Page({
  data: {
    cameraAuthorized: false,
    checkingPermission: false,
    scanText: '',
    token: '',
    verifying: false,
    result: '',
    resultType: ''
  },

  onShow() {
    this.checkCameraPermission()
  },

  checkCameraPermission() {
    this.setData({ checkingPermission: true })
    wx.getSetting({
      success: (res) => {
        this.setData({
          cameraAuthorized: !!res.authSetting['scope.camera'],
          checkingPermission: false
        })
      },
      fail: () => {
        this.setData({ checkingPermission: false })
      }
    })
  },

  requestCameraPermission() {
    wx.authorize({
      scope: 'scope.camera',
      success: () => {
        this.setData({ cameraAuthorized: true })
        wx.showToast({ title: '相机权限已开启' })
      },
      fail: () => {
        wx.showModal({
          title: '需要相机权限',
          content: '请在设置中开启相机权限，用于门禁扫码核验。',
          success: ({ confirm }) => {
            if (confirm) {
              wx.openSetting({
                success: (res) => {
                  this.setData({ cameraAuthorized: !!res.authSetting['scope.camera'] })
                }
              })
            }
          }
        })
      }
    })
  },

  onScanTextInput(e) {
    const scanText = e.detail.value || ''
    this.setData({ scanText, token: extractToken(scanText) })
  },

  readFromClipboard() {
    wx.getClipboardData({
      success: ({ data }) => {
        const scanText = data || ''
        this.setData({ scanText, token: extractToken(scanText) })
      },
      fail: () => {
        wx.showToast({ title: '读取剪贴板失败', icon: 'none' })
      }
    })
  },

  async simulateScanVerify() {
    if (this.data.verifying) return
    const token = extractToken(this.data.scanText || this.data.token)
    if (!token) {
      wx.showToast({ title: '请先输入扫码内容或token', icon: 'none' })
      return
    }

    this.setData({ verifying: true, token, result: '', resultType: '' })
    try {
      const data = await request({
        url: '/access/verify-token',
        method: 'POST',
        data: { token }
      })
      this.setData({
        result: data?.message || '核验通过，访客可入场',
        resultType: 'success'
      })
      wx.showToast({ title: '核验成功' })
    } catch (error) {
      this.setData({
        result: error?.message || '核验失败',
        resultType: 'error'
      })
      wx.showToast({ title: error?.message || '核验失败', icon: 'none' })
    } finally {
      this.setData({ verifying: false })
    }
  }
})
