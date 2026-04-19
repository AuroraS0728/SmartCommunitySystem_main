const { request } = require('../../../api/request')

function statusText(status) {
  return Number(status) === 1 ? '已支付' : '待支付'
}

function orderTypeText(orderType) {
  return Number(orderType) === 2 ? '月卡订单' : '临停订单'
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
    vehicleNo: '',
    queryResult: null,
    orders: [],
    loading: false
  },

  onShow() {
    this.loadRecords()
  },

  async loadRecords() {
    this.setData({ loading: true })
    try {
      const orders = await request({ url: '/fee/parking/orders' })
      const normalized = (Array.isArray(orders) ? orders : []).map((item) => ({
        ...item,
        statusText: statusText(item.status),
        orderTypeText: orderTypeText(item.orderType),
        amountText: toMoney(item.amount),
        startTimeText: formatTime(item.startTime),
        endTimeText: formatTime(item.endTime),
        canPay: Number(item.status) !== 1
      }))
      this.setData({ orders: normalized })
    } catch (error) {
      wx.showToast({ title: error?.message || '停车记录加载失败', icon: 'none' })
    } finally {
      this.setData({ loading: false })
    }
  },

  onInputVehicle(e) {
    this.setData({ vehicleNo: (e.detail.value || '').toUpperCase() })
  },

  async onQueryByPlate() {
    const vehicleNo = (this.data.vehicleNo || '').trim().toUpperCase()
    if (!vehicleNo) {
      wx.showToast({ title: '请输入车牌号', icon: 'none' })
      return
    }
    try {
      const data = await request({
        url: '/parking/pay-entry',
        method: 'GET',
        data: { vehicleNo }
      })
      this.setData({ queryResult: data })
    } catch (error) {
      wx.showToast({ title: error?.message || '查询失败', icon: 'none' })
    }
  },

  async onSettleExit() {
    const vehicleNo = (this.data.vehicleNo || '').trim().toUpperCase()
    if (!vehicleNo) {
      wx.showToast({ title: '请输入车牌号', icon: 'none' })
      return
    }
    try {
      await request({
        url: '/parking/exit',
        method: 'POST',
        data: { vehicleNo }
      })
      wx.showToast({ title: '出场结算完成', icon: 'success' })
      this.onQueryByPlate()
      this.loadRecords()
    } catch (error) {
      wx.showToast({ title: error?.message || '结算失败', icon: 'none' })
    }
  },

  async onRenewMonthCard() {
    const vehicleNo = (this.data.vehicleNo || '').trim().toUpperCase()
    if (!vehicleNo) {
      wx.showToast({ title: '请输入车牌号', icon: 'none' })
      return
    }
    try {
      const payResp = await request({
        url: '/parking/month-card/renew',
        method: 'POST',
        data: { vehicleNo }
      })
      const outTradeNo = payResp?.payParams?.package
        ? String(payResp.payParams.package).replace('prepay_id=mock_', '')
        : ''
      if (!outTradeNo) throw new Error('月卡下单失败')
      await request({
        url: '/parking/pay/callback',
        method: 'POST',
        data: { outTradeNo, success: true }
      })
      wx.showToast({ title: '月卡续费成功', icon: 'success' })
      this.onQueryByPlate()
      this.loadRecords()
    } catch (error) {
      wx.showToast({ title: error?.message || '续费失败', icon: 'none' })
    }
  },

  async onPayTempOrder(e) {
    const orderId = Number(e.currentTarget.dataset.id || 0)
    if (!orderId) {
      wx.showToast({ title: '订单异常', icon: 'none' })
      return
    }
    try {
      const payResp = await request({
        url: '/parking/pay/wechat',
        method: 'POST',
        data: { orderId }
      })
      const outTradeNo = payResp?.outTradeNo || ''
      if (!outTradeNo) throw new Error('临停下单失败')
      await request({
        url: '/parking/pay/callback',
        method: 'POST',
        data: { outTradeNo, success: true }
      })
      wx.showToast({ title: '支付成功', icon: 'success' })
      this.onQueryByPlate()
      this.loadRecords()
    } catch (error) {
      wx.showToast({ title: error?.message || '支付失败', icon: 'none' })
    }
  }
})
