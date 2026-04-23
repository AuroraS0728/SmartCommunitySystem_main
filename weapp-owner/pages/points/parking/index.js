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

function toNeedPoints(amount) {
  return Math.ceil(Number(amount || 0))
}

Page({
  data: {
    points: 0,
    vehicleNo: '',
    autoQueryPlate: '',
    queryResult: null,
    orders: [],
    loading: false
  },

  onLoad(options) {
    const vehicleNo = decodeURIComponent((options && options.vehicleNo) || '').trim().toUpperCase()
    if (!vehicleNo) return
    this.setData({
      vehicleNo,
      autoQueryPlate: vehicleNo
    })
  },

  async onShow() {
    await this.loadAll()
    if (!this.data.autoQueryPlate) return
    this.setData({ autoQueryPlate: '' })
    await this.onQueryByPlate()
  },

  async loadAll() {
    this.setData({ loading: true })
    try {
      const [balance, orders] = await Promise.all([request({ url: '/points/balance' }), request({ url: '/fee/parking/orders' })])
      const normalized = (Array.isArray(orders) ? orders : []).map((item) => ({
        ...item,
        statusText: statusText(item.status),
        orderTypeText: orderTypeText(item.orderType),
        amountText: toMoney(item.amount),
        needPointsText: toNeedPoints(item.amount),
        startTimeText: formatTime(item.startTime),
        endTimeText: formatTime(item.endTime),
        canPay: Number(item.status) !== 1
      }))
      this.setData({
        points: Number(balance?.points || 0),
        orders: normalized
      })
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
      const normalized = data
        ? {
            ...data,
            unpaidNeedPoints: toNeedPoints(data?.unpaidTempOrder?.amount)
          }
        : null
      this.setData({ queryResult: normalized })
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
      await this.onQueryByPlate()
      await this.loadAll()
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
    const confirmed = await new Promise((resolve) => {
      wx.showModal({
        title: '月卡续费',
        content: '确认使用 500 积分购买/续费月卡吗？',
        success: ({ confirm }) => resolve(!!confirm),
        fail: () => resolve(false)
      })
    })
    if (!confirmed) return

    try {
      const renewResp = await request({
        url: '/parking/month-card/renew',
        method: 'POST',
        data: { vehicleNo }
      })
      const orderId = Number(renewResp?.orderId || 0)
      if (!orderId) {
        throw new Error('月卡订单创建失败')
      }
      await request({
        url: '/points/consume',
        method: 'POST',
        data: { businessType: 2, businessId: orderId }
      })
      wx.showToast({ title: '月卡续费成功', icon: 'success' })
      await this.onQueryByPlate()
      await this.loadAll()
    } catch (error) {
      wx.showToast({ title: error?.message || '续费失败', icon: 'none' })
    }
  },

  async onPayTempOrder(e) {
    const orderId = Number(e.currentTarget.dataset.id || 0)
    const needPoints = Number(e.currentTarget.dataset.needPoints || 0)
    if (!orderId) {
      wx.showToast({ title: '订单异常', icon: 'none' })
      return
    }
    const confirmed = await new Promise((resolve) => {
      wx.showModal({
        title: '确认积分支付',
        content: `确认使用 ${needPoints} 积分支付该停车订单吗？`,
        success: ({ confirm }) => resolve(!!confirm),
        fail: () => resolve(false)
      })
    })
    if (!confirmed) return

    try {
      await request({
        url: '/points/consume',
        method: 'POST',
        data: { businessType: 2, businessId: orderId }
      })
      wx.showToast({ title: '支付成功', icon: 'success' })
      await this.onQueryByPlate()
      await this.loadAll()
    } catch (error) {
      wx.showToast({ title: error?.message || '支付失败', icon: 'none' })
    }
  }
})
