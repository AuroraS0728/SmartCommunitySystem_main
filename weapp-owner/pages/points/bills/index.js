const { request } = require('../../../api/request')

function statusText(status) {
  const value = Number(status)
  if (value === 2) return '已缴清'
  if (value === 1) return '部分已缴'
  return '未缴费'
}

function toMoney(value) {
  return Number(value || 0).toFixed(2)
}

function formatTime(value) {
  if (!value) return '--'
  return String(value).replace('T', ' ').slice(0, 19)
}

function toNeedPoints(item) {
  const direct = Number(item?.needPoints || 0)
  if (direct > 0) return direct
  return Math.ceil(Number(item?.amount || 0))
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
      const [balance, bills] = await Promise.all([request({ url: '/points/balance' }), request({ url: '/fee/bills' })])
      const normalized = (Array.isArray(bills) ? bills : []).map((item) => ({
        ...item,
        statusText: statusText(item.status),
        amountText: toMoney(item.amount),
        areaText: toMoney(item.areaSnapshot),
        unitPriceText: toMoney(item.unitPrice || 5),
        dueDateText: formatTime(item.dueDate),
        needPointsText: toNeedPoints(item),
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
    const needPoints = Number(e.currentTarget.dataset.needPoints || 0)
    if (!billId) {
      wx.showToast({ title: '账单数据异常', icon: 'none' })
      return
    }
    wx.showModal({
      title: '确认积分支付',
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
          await this.loadData()
        } catch (error) {
          wx.showToast({ title: error?.message || '支付失败', icon: 'none' })
        }
      }
    })
  }
})
