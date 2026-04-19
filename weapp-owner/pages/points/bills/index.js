const { request } = require('../../../api/request')

function statusText(status) {
  const value = Number(status)
  if (value === 2) return '已缴清'
  if (value === 1) return '部分已缴'
  return '待缴费'
}

function toMoney(value) {
  return Number(value || 0).toFixed(2)
}

function formatTime(value) {
  if (!value) return '--'
  return String(value).replace('T', ' ').slice(0, 19)
}

Page({
  data: {
    bills: [],
    loading: false
  },

  onShow() {
    this.loadData()
  },

  async loadData() {
    this.setData({ loading: true })
    try {
      const bills = await request({ url: '/fee/bills' })
      const normalized = (Array.isArray(bills) ? bills : []).map((item) => ({
        ...item,
        statusText: statusText(item.status),
        amountText: toMoney(item.amount),
        areaText: toMoney(item.areaSnapshot),
        unitPriceText: toMoney(item.unitPrice || 5),
        dueDateText: formatTime(item.dueDate),
        canPay: Number(item.status) !== 2
      }))
      this.setData({ bills: normalized })
    } catch (error) {
      wx.showToast({ title: error?.message || '账单加载失败', icon: 'none' })
    } finally {
      this.setData({ loading: false })
    }
  },

  onPayBill(e) {
    const billId = Number(e.currentTarget.dataset.id || 0)
    if (!billId) {
      wx.showToast({ title: '账单数据异常', icon: 'none' })
      return
    }
    wx.showModal({
      title: '确认支付',
      content: '确认发起微信支付吗？（当前为模拟回调）',
      success: async ({ confirm }) => {
        if (!confirm) return
        try {
          const payResp = await request({
            url: '/fee/pay/wechat',
            method: 'POST',
            data: { billId }
          })
          const outTradeNo = payResp?.outTradeNo
          if (!outTradeNo) {
            throw new Error('支付下单失败')
          }
          // 个人开发环境使用模拟回调，线上应由微信回调触发。
          await request({
            url: '/fee/pay/callback',
            method: 'POST',
            data: { outTradeNo, success: true }
          })
          wx.showToast({ title: '支付成功', icon: 'success' })
          this.loadData()
        } catch (error) {
          wx.showToast({ title: error?.message || '支付失败', icon: 'none' })
        }
      }
    })
  }
})
