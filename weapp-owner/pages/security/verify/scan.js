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

const SCAN_DEBOUNCE_MS = 2500

Page({
  data: {
    cameraAuthorized: false,
    checkingPermission: false,
    scanText: '',
    token: '',
    verifying: false,
    cameraError: '',
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

  onCameraScan(e) {
    const scanText = e?.detail?.result || ''
    const token = extractToken(scanText)
    if (!token || this.data.verifying) return

    const now = Date.now()
    if (this.lastScanToken === token && now - (this.lastScanTime || 0) < SCAN_DEBOUNCE_MS) {
      return
    }
    this.lastScanToken = token
    this.lastScanTime = now
    this.setData({ scanText, token, cameraError: '' })
    this.verifyToken(token)
  },

  onCameraError(error) {
    this.setData({ cameraError: error?.detail?.errMsg || '相机启动失败，请检查相机权限' })
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

  async verifyToken(tokenValue) {
    if (this.data.verifying) return
    const token = extractToken(tokenValue || this.data.scanText || this.data.token)
    if (!token) {
      wx.showToast({ title: '请先输入扫码内容', icon: 'none' })
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
        result: data?.message || '核验通过，访客可正常通行',
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
  },

  simulateScanVerify() {
    return this.verifyToken()
  }
})
