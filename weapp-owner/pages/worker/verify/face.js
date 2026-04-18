const { request } = require('../../../api/request')

function readBase64(filePath) {
  return new Promise((resolve, reject) => {
    wx.getFileSystemManager().readFile({
      filePath,
      encoding: 'base64',
      success: (res) => resolve(res.data),
      fail: reject
    })
  })
}

Page({
  data: {
    orderId: '',
    workerId: '',
    imagePath: '',
    loading: false,
    verified: false,
    score: 0
  },

  onLoad(query) {
    const orderId = query?.orderId ? String(query.orderId) : ''
    const userInfo = wx.getStorageSync('userInfo') || {}
    const workerId = userInfo?.id ? String(userInfo.id) : ''
    this.setData({ orderId, workerId })
  },

  async startFaceVerify() {
    if (this.data.loading) return
    if (!this.data.workerId) {
      wx.showToast({ title: '未获取到维修员身份', icon: 'none' })
      return
    }

    // Only allow taking photo from camera for on-site verification.
    wx.chooseMedia({
      count: 1,
      mediaType: ['image'],
      sourceType: ['camera'],
      camera: 'front',
      success: async (res) => {
        const filePath = res?.tempFiles?.[0]?.tempFilePath
        if (!filePath) {
          wx.showToast({ title: '拍照失败，请重试', icon: 'none' })
          return
        }
        this.setData({ imagePath: filePath, loading: true, verified: false })
        try {
          const imageBase64 = await readBase64(filePath)
          const data = await request({
            url: '/face/verify',
            method: 'POST',
            data: {
              workerId: Number(this.data.workerId),
              imageBase64
            }
          })
          const match = !!data?.match
          const score = Number(data?.score || 0)
          this.setData({ verified: match, score })
          if (match) {
            wx.showToast({ title: '人脸验证通过', icon: 'success' })
            setTimeout(() => {
              if (this.data.orderId) {
                wx.navigateTo({ url: `/pages/worker/work/process?id=${this.data.orderId}` })
              } else {
                wx.navigateTo({ url: '/pages/worker/task/list' })
              }
            }, 500)
          } else {
            wx.showToast({ title: '人脸不匹配，请确认是否为本人', icon: 'none' })
          }
        } catch (error) {
          wx.showToast({ title: error?.message || '人脸验证失败', icon: 'none' })
        } finally {
          this.setData({ loading: false })
        }
      },
      fail: () => {
        wx.showToast({ title: '未完成拍照', icon: 'none' })
      }
    })
  },

  retryVerify() {
    if (this.data.loading) return
    this.setData({ verified: false, score: 0 })
    this.startFaceVerify()
  }
})
