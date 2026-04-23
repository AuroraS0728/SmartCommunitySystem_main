const { request } = require("../../api/request")

function calcBillStats(list) {
  const rows = Array.isArray(list) ? list : []
  const paid = rows.filter((item) => {
    const type = Number(item?.businessType || 0)
    const status = Number(item?.status || 0)
    if (type === 1) return status === 2
    return status === 1
  }).length
  return {
    total: rows.length,
    paid,
    unpaid: Math.max(rows.length - paid, 0)
  }
}

function calcParkingCardStats(orders) {
  const rows = Array.isArray(orders) ? orders : []
  const monthCards = rows.filter((item) => Number(item?.orderType) === 2)
  const vehicleSet = new Set()
  let expiringSoonCount = 0
  const now = Date.now()

  monthCards.forEach((item) => {
    const plate = (item?.vehicleNo || "").trim().toUpperCase()
    if (plate) vehicleSet.add(plate)

    const endTs = new Date(item?.endTime || "").getTime()
    if (!Number.isFinite(endTs)) return
    const diffDays = Math.ceil((endTs - now) / (24 * 60 * 60 * 1000))
    if (diffDays >= 0 && diffDays <= 3) {
      expiringSoonCount += 1
    }
  })

  return {
    vehicleCount: vehicleSet.size,
    expiringSoonCount
  }
}

Page({
  data: {
    points: 0,
    user: null,
    stats: {
      billCount: 0,
      billPaidCount: 0,
      billUnpaidCount: 0,
      monthCardVehicleCount: 0,
      monthCardExpiringSoonCount: 0,
      secondhandFavoriteCount: 0
    }
  },

  onShow() {
    this.loadAll()
  },

  async loadAll() {
    await Promise.allSettled([this.loadUser(), this.loadStats()])
  },

  async loadUser() {
    const app = getApp()
    try {
      const user = await request({ url: "/user/me" })
      this.setData({
        user,
        points: Number(user?.points || 0)
      })
      app.globalData.userInfo = user
      wx.setStorageSync("userInfo", user)
    } catch (error) {
      wx.showToast({ title: error?.message || "加载个人信息失败", icon: "none" })
    }
  },

  async loadStats() {
    try {
      const [subjects, parkingOrders, secondhandFavorites] = await Promise.all([
        request({ url: "/fee/subjects" }),
        request({ url: "/fee/parking/orders" }),
        request({ url: "/neighbor/second-hand/favorite/list", data: { page: 1, size: 1 } })
      ])

      const billStats = calcBillStats(subjects)
      const cardStats = calcParkingCardStats(parkingOrders)

      this.setData({
        stats: {
          billCount: billStats.total,
          billPaidCount: billStats.paid,
          billUnpaidCount: billStats.unpaid,
          monthCardVehicleCount: cardStats.vehicleCount,
          monthCardExpiringSoonCount: cardStats.expiringSoonCount,
          secondhandFavoriteCount: Number(secondhandFavorites?.total || 0)
        }
      })
    } catch (error) {
      wx.showToast({ title: error?.message || "加载统计失败", icon: "none" })
    }
  },

  goInfo() {
    wx.navigateTo({ url: "/pages/user/info" })
  },

  goBills() {
    wx.navigateTo({ url: "/pages/points/bills/index" })
  },

  goParkingCards() {
    wx.navigateTo({ url: "/pages/user/parking-cards/index" })
  },

  goFavorites() {
    wx.navigateTo({ url: "/pages/user/favorites" })
  },

  goComplaint() {
    wx.navigateTo({ url: "/pages/complaint/submit" })
  },

  goSettings() {
    wx.navigateTo({ url: "/pages/user/settings/index" })
  }
})
