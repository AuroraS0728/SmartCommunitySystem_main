const { request } = require('../../../api/request')

function statusText(status) {
  return Number(status) === 1 ? '已支付' : '待支付'
}

function orderTypeText(orderType) {
  return Number(orderType) === 2 ? '月卡续费' : '临时停车'
}

function toNeedPoints(item) {
  return Math.trunc(Number(item?.amount || 0))
}

function toBusinessType(item) {
  return Number(item?.orderType) === 2 ? 2 : 3
}

Page({
  data: {
    points: 0,
    orders: [],
    loading: false
  },

  onShow() {
    this.loadData()
  },

  async loadData() {
    this.setData({ loading: true })
    try {
      const [balance, orders] = await Promise.all([
        request({ url: '/points/balance' }),
        request({ url: '/fee/parking/orders' })
      ])
      const normalized = (Array.isArray(orders) ? orders : []).map((item) => ({
        ...item,
        statusText: statusText(item.status),
        orderTypeText: orderTypeText(item.orderType),
        needPoints: toNeedPoints(item),
        businessType: toBusinessType(item),
        canPay: Number(item.status) !== 1
      }))
      this.setData({
        points: Number(balance?.points || 0),
        orders: normalized
      })
    } catch (error) {
      wx.showToast({ title: error?.message || '停车订单加载失败', icon: 'none' })
    } finally {
      this.setData({ loading: false })
    }
  },

  onPayOrder(e) {
    const orderId = Number(e.currentTarget.dataset.id || 0)
    const needPoints = Number(e.currentTarget.dataset.points || 0)
    const businessType = Number(e.currentTarget.dataset.businessType || 3)
    if (!orderId || needPoints <= 0) {
      wx.showToast({ title: '订单数据异常', icon: 'none' })
      return
    }
    wx.showModal({
      title: '积分支付确认',
      content: `确认使用 ${needPoints} 积分支付该停车订单吗？`,
      success: async ({ confirm }) => {
        if (!confirm) return
        try {
          await request({
            url: '/points/consume',
            method: 'POST',
            data: { businessType, businessId: orderId }
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
