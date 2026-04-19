const { request } = require('../../../api/request')

function formatTime(value) {
  if (!value) return '--'
  return String(value).replace('T', ' ').slice(0, 19)
}

function typeText(item) {
  if (Number(item?.recordType) === 1) return '充值入账'
  return item?.remark || '积分消费'
}

Page({
  data: {
    points: 0,
    records: [],
    loading: false
  },

  onShow() {
    this.loadData()
  },

  async loadData() {
    this.setData({ loading: true })
    try {
      const [balance, records] = await Promise.all([
        request({ url: '/points/balance' }),
        request({ url: '/points/records' })
      ])
      const normalized = (Array.isArray(records) ? records : []).map((item) => {
        const income = Number(item.recordType) === 1
        const points = Number(item.points || 0)
        return {
          ...item,
          changeText: `${income ? '+' : '-'}${points}`,
          changeClass: income ? 'income' : 'expense',
          typeText: typeText(item),
          createTimeText: formatTime(item.createTime)
        }
      })
      this.setData({
        points: Number(balance?.points || 0),
        records: normalized
      })
    } catch (error) {
      wx.showToast({ title: error?.message || '积分明细加载失败', icon: 'none' })
    } finally {
      this.setData({ loading: false })
    }
  }
})
