const { request } = require('../../../api/request')

function statusText(status) {
  const value = Number(status)
  if (value === 2) return '已缴清'
  if (value === 1) return '部分已缴'
  return '待缴费'
}

function calcNeedPoints(item) {
  const needPoints = Number(item?.needPoints || 0)
  if (needPoints > 0) return needPoints
  return Math.trunc(Number(item?.amount || 0))
}

Page({
  data: {
    points: 0,
    bills: [],
    loading: false
  },

  onShow() {
    this.loadData()
  },

  async loadData() {
    this.setData({ loading: true })
    try {
      const [balance, bills] = await Promise.all([
        request({ url: '/points/balance' }),
        request({ url: '/fee/bills' })
      ])
      const normalized = (Array.isArray(bills) ? bills : []).map((item) => ({
        ...item,
        statusText: statusText(item.status),
        needPoints: calcNeedPoints(item),
        canPay: Number(item.status) !== 2
      }))
      this.setData({
        points: Number(balance?.points || 0),
        bills: normalized
      })
    } catch (error) {
      wx.showToast({ title: error?.message || '账单加载失败', icon: 'none' })
    } finally {
      this.setData({ loading: false })
    }
  },

  onPayBill(e) {
    const billId = Number(e.currentTarget.dataset.id || 0)
    const needPoints = Number(e.currentTarget.dataset.points || 0)
    if (!billId || needPoints <= 0) {
      wx.showToast({ title: '账单数据异常', icon: 'none' })
      return
    }
    wx.showModal({
      title: '积分支付确认',
      content: `确认使用 ${needPoints} 积分支付该物业费账单吗？`,
      success: async ({ confirm }) => {
        if (!confirm) return
        try {
          await request({
            url: '/points/consume',
            method: 'POST',
            data: { businessType: 1, businessId: billId }
          })
          wx.showToast({ title: '支付成功', icon: 'success' })
          this.loadData()
        } catch (error) {
          const msg = error?.message || '支付失败'
          if (String(msg).includes('积分不足')) {
            wx.showToast({ title: '积分不足，请联系物业充值', icon: 'none' })
            return
          }
          wx.showToast({ title: msg, icon: 'none' })
        }
      }
    })
  }
})
