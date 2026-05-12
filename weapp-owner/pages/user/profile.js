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
    guestMode: false,
    unreadMessageCount: 0,
    savingProfile: false,
    profileForm: {
      hasElderly: 0,
      hasChild: 0,
      hasPet: 0,
      houseArea: "",
      roomCount: ""
    },
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
    const app = getApp()
    const guestMode = !!app.globalData.guestMode
    this.setData({ guestMode })
    if (guestMode) {
      return
    }
    this.loadAll()
  },

  async loadAll() {
    await Promise.allSettled([this.loadUser(), this.loadStats(), this.loadUnreadMessages()])
  },

  async loadUser() {
    const app = getApp()
    try {
      const user = await request({ url: "/user/info" })
      this.setData({
        user,
        points: Number(user?.points || 0),
        profileForm: {
          hasElderly: Number(user?.hasElderly || 0),
          hasChild: Number(user?.hasChild || 0),
          hasPet: Number(user?.hasPet || 0),
          houseArea: user?.houseArea === null || user?.houseArea === undefined ? "" : String(user.houseArea),
          roomCount: user?.roomCount === null || user?.roomCount === undefined ? "" : String(user.roomCount)
        }
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

  async loadUnreadMessages() {
    try {
      const result = await request({ url: "/messages/unread-count" })
      this.setData({ unreadMessageCount: Number(result?.count || 0) })
    } catch (error) {
      this.setData({ unreadMessageCount: 0 })
    }
  },

  onToggleProfile(e) {
    const key = e.currentTarget.dataset.key
    if (!key) return
    this.setData({ [`profileForm.${key}`]: e.detail.value ? 1 : 0 })
  },

  onProfileNumberInput(e) {
    const key = e.currentTarget.dataset.key
    if (!key) return
    this.setData({ [`profileForm.${key}`]: e.detail.value || "" })
  },

  normalizeOptionalInt(value) {
    if (value === "" || value === null || value === undefined) {
      return null
    }
    const numberValue = Number(value)
    return Number.isInteger(numberValue) ? numberValue : NaN
  },

  async saveProfile() {
    if (this.data.savingProfile) return
    const houseArea = this.normalizeOptionalInt(this.data.profileForm.houseArea)
    const roomCount = this.normalizeOptionalInt(this.data.profileForm.roomCount)
    if (Number.isNaN(houseArea) || houseArea < 0) {
      wx.showToast({ title: "房屋面积需为非负整数", icon: "none" })
      return
    }
    if (Number.isNaN(roomCount) || roomCount < 0) {
      wx.showToast({ title: "房间数需为非负整数", icon: "none" })
      return
    }

    this.setData({ savingProfile: true })
    try {
      const updated = await request({
        url: "/user/profile",
        method: "PUT",
        data: {
          hasElderly: Number(this.data.profileForm.hasElderly || 0),
          hasChild: Number(this.data.profileForm.hasChild || 0),
          hasPet: Number(this.data.profileForm.hasPet || 0),
          houseArea,
          roomCount
        }
      })
      const nextUser = { ...(this.data.user || {}), ...(updated || {}) }
      this.setData({
        user: nextUser,
        profileForm: {
          hasElderly: Number(nextUser?.hasElderly || 0),
          hasChild: Number(nextUser?.hasChild || 0),
          hasPet: Number(nextUser?.hasPet || 0),
          houseArea: nextUser?.houseArea === null || nextUser?.houseArea === undefined ? "" : String(nextUser.houseArea),
          roomCount: nextUser?.roomCount === null || nextUser?.roomCount === undefined ? "" : String(nextUser.roomCount)
        }
      })
      const app = getApp()
      app.globalData.userInfo = nextUser
      wx.setStorageSync("userInfo", nextUser)
      wx.showToast({ title: "保存成功", icon: "success" })
    } catch (error) {
      wx.showToast({ title: error?.message || "保存失败", icon: "none" })
    } finally {
      this.setData({ savingProfile: false })
    }
  },

  goInfo() {
    if (!getApp().requireFeatureLogin()) return
    wx.navigateTo({ url: "/pages/user/info" })
  },

  goBills() {
    if (!getApp().requireFeatureLogin()) return
    wx.navigateTo({ url: "/pages/points/bills/index" })
  },

  goParkingCards() {
    if (!getApp().requireFeatureLogin()) return
    wx.navigateTo({ url: "/pages/user/parking-cards/index" })
  },

  goFavorites() {
    if (!getApp().requireFeatureLogin()) return
    wx.navigateTo({ url: "/pages/user/favorites" })
  },

  goComplaint() {
    if (!getApp().requireFeatureLogin()) return
    wx.navigateTo({ url: "/pages/complaint/submit" })
  },

  goMessages() {
    if (!getApp().requireFeatureLogin()) return
    wx.navigateTo({ url: "/pages/message/list" })
  },

  goSettings() {
    wx.navigateTo({ url: "/pages/user/settings/index" })
  },

  goOwnerLogin() {
    getApp().gotoLogin(1)
  },

  goSecurityLogin() {
    getApp().gotoLogin(2)
  },

  goWorkerLogin() {
    getApp().gotoLogin(3)
  }
})
