const { request } = require("../../../api/request")

function toMoney(value) {
  return Number(value || 0).toFixed(2)
}

function toTime(value) {
  if (!value) return "--"
  return String(value).replace("T", " ").slice(0, 19)
}

function diffDays(endTime) {
  const endTs = new Date(endTime || "").getTime()
  if (!Number.isFinite(endTs)) return null
  const now = Date.now()
  return Math.ceil((endTs - now) / (24 * 60 * 60 * 1000))
}

function buildCards(orders) {
  const rows = Array.isArray(orders) ? orders : []
  const monthOrders = rows.filter((item) => Number(item?.orderType) === 2)
  const unpaidByPlate = new Map()

  rows.forEach((item) => {
    const plate = (item?.vehicleNo || "").trim().toUpperCase()
    if (!plate) return
    const isUnpaid = Number(item?.status) !== 1
    if (!isUnpaid) return
    const nextAmount = (unpaidByPlate.get(plate) || 0) + Number(item?.amount || 0)
    unpaidByPlate.set(plate, nextAmount)
  })

  const monthCardByPlate = new Map()
  monthOrders.forEach((item) => {
    const plate = (item?.vehicleNo || "").trim().toUpperCase()
    if (!plate) return
    const prev = monthCardByPlate.get(plate)
    const prevTs = prev ? new Date(prev.endTime || "").getTime() : -Infinity
    const currTs = new Date(item?.endTime || "").getTime()
    if (!prev || currTs > prevTs) {
      monthCardByPlate.set(plate, item)
    }
  })

  return Array.from(monthCardByPlate.entries()).map(([plate, item]) => {
    const days = diffDays(item?.endTime)
    const unpaid = Number(unpaidByPlate.get(plate) || 0)
    let reminderText = "月卡状态正常"
    if (days !== null && days < 0) {
      reminderText = "月卡已过期，请尽快续费"
    } else if (days !== null && days <= 3) {
      reminderText = `月卡将在 ${days} 天内到期，请提前续费`
    }

    return {
      vehicleNo: plate,
      endTimeText: toTime(item?.endTime),
      remainingDaysText: days === null ? "--" : String(days),
      unpaidAmountText: toMoney(unpaid),
      reminderText
    }
  })
}

Page({
  data: {
    cards: [],
    loading: false
  },

  onShow() {
    this.loadCards()
  },

  async loadCards() {
    this.setData({ loading: true })
    try {
      const orders = await request({ url: "/fee/parking/orders" })
      this.setData({ cards: buildCards(orders) })
    } catch (error) {
      wx.showToast({ title: error?.message || "加载停车月卡失败", icon: "none" })
    } finally {
      this.setData({ loading: false })
    }
  },

  goPay(e) {
    const vehicleNo = e.currentTarget.dataset.vehicleNo || ""
    const encoded = encodeURIComponent(vehicleNo)
    wx.navigateTo({ url: `/pages/points/parking/index?vehicleNo=${encoded}` })
  }
})
