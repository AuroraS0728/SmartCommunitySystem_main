const { request } = require('../../../api/request')

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

function isPaid(item) {
  const type = Number(item?.businessType || 0)
  const status = Number(item?.status || 0)
  if (type === 1) return status === 2
  return status === 1
}

function resolveStatusText(item) {
  if (item?.statusText) return item.statusText
  return isPaid(item) ? '已缴费' : '待缴费'
}

function resolveTypeText(type) {
  const map = {
    1: '物业费缴纳',
    2: '停车费缴纳',
    3: '维修费用缴纳'
  }
  return map[Number(type)] || '费用缴纳'
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
      const [balance, subjects] = await Promise.all([
        request({ url: '/points/balance' }),
        request({ url: '/fee/subjects' })
      ])
      const normalized = (Array.isArray(subjects) ? subjects : []).map((item) => {
        const paid = isPaid(item)
        return {
          ...item,
          rowId: `${item.businessType || 0}-${item.businessId || 0}`,
          businessTypeText: item.businessTypeText || resolveTypeText(item.businessType),
          statusText: resolveStatusText(item),
          amountText: toMoney(item.amount),
          paidAmountText: toMoney(item.paidAmount),
          dueDateText: formatTime(item.dueDate),
          needPointsText: toNeedPoints(item),
          propertyCodeText: item.propertyCode || '--',
          ownerNameText: item.ownerName || '--',
          businessRefText: item.businessRef || '--',
          canPay: !paid
        }
      })
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
    const businessType = Number(e.currentTarget.dataset.businessType || 0)
    const businessId = Number(e.currentTarget.dataset.businessId || 0)
    const needPoints = Number(e.currentTarget.dataset.needPoints || 0)
    const typeText = e.currentTarget.dataset.typeText || resolveTypeText(businessType)
    const subjectName = e.currentTarget.dataset.subjectName || '--'
    if (!businessType || !businessId) {
      wx.showToast({ title: '账单数据异常', icon: 'none' })
      return
    }
    wx.showModal({
      title: '确认积分支付',
      content: `类型：${typeText}\n主体：${subjectName}\n确认使用 ${needPoints} 积分支付吗？`,
      success: async ({ confirm }) => {
        if (!confirm) return
        try {
          await request({
            url: '/points/consume',
            method: 'POST',
            data: { businessType, businessId }
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
