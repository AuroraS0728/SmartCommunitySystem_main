const { request } = require("../../api/request")

const TIME_SLOTS = [
  "09:00-11:00",
  "11:00-13:00",
  "13:00-15:00",
  "15:00-17:00",
  "17:00-19:00"
]

function formatDate(date) {
  const year = date.getFullYear()
  const month = String(date.getMonth() + 1).padStart(2, "0")
  const day = String(date.getDate()).padStart(2, "0")
  return `${year}-${month}-${day}`
}

Page({
  data: {
    points: 0,
    loadingPoints: false,
    loadingServices: false,
    loadingOrders: false,
    guestMode: false,
    today: "",
    services: [],
    selectedServiceId: "",
    selectedService: null,
    appointmentDate: "",
    appointmentTimeSlot: TIME_SLOTS[0],
    timeSlots: TIME_SLOTS,
    remark: "",
    submitting: false,
    recentOrders: []
  },

  onLoad() {
    const today = formatDate(new Date())
    this.setData({
      today,
      appointmentDate: today
    })
  },

  onShow() {
    const app = getApp()
    const guestMode = !!app.globalData.guestMode
    this.setData({ guestMode })
    this.loadPageData()
  },

  async loadPageData() {
    if (this.data.guestMode) {
      this.setData({
        points: 0,
        services: [],
        selectedServiceId: "",
        selectedService: null,
        recentOrders: []
      })
      return
    }
    await Promise.allSettled([
      this.loadPoints(),
      this.loadServices(),
      this.loadRecentOrders()
    ])
    this.consumePendingService()
  },

  async loadPoints() {
    this.setData({ loadingPoints: true })
    try {
      const data = await request({ url: "/points/balance" })
      this.setData({ points: Number(data?.points || 0) })
    } catch (error) {
      wx.showToast({ title: error?.message || "积分加载失败", icon: "none" })
    } finally {
      this.setData({ loadingPoints: false })
    }
  },

  async loadServices() {
    this.setData({ loadingServices: true })
    try {
      const list = await request({ url: "/additional-services/catalog" })
      const services = Array.isArray(list) ? list : []
      const preferred = services.find((item) => item.recommended) || services[0] || null
      this.setData({
        services,
        selectedServiceId: preferred?.serviceId || "",
        selectedService: preferred
      })
    } catch (error) {
      this.setData({
        services: [],
        selectedServiceId: "",
        selectedService: null
      })
      wx.showToast({ title: error?.message || "附加服务加载失败", icon: "none" })
    } finally {
      this.setData({ loadingServices: false })
    }
  },

  async loadRecentOrders() {
    this.setData({ loadingOrders: true })
    try {
      const list = await request({ url: "/additional-services/orders/my", data: { limit: 3 } })
      this.setData({ recentOrders: Array.isArray(list) ? list : [] })
    } catch (error) {
      this.setData({ recentOrders: [] })
    } finally {
      this.setData({ loadingOrders: false })
    }
  },

  consumePendingService() {
    const pending = wx.getStorageSync("pendingAdditionalService")
    if (!pending) {
      return
    }
    wx.removeStorageSync("pendingAdditionalService")
    const serviceId = typeof pending === "string" ? pending : pending.serviceId
    if (!serviceId) {
      return
    }
    this.selectServiceById(serviceId)
    wx.nextTick(() => {
      wx.pageScrollTo({
        selector: "#additional-service-section",
        duration: 220
      })
    })
  },

  selectServiceById(serviceId) {
    const selectedService = (this.data.services || []).find((item) => item.serviceId === serviceId) || null
    if (!selectedService) {
      return
    }
    this.setData({
      selectedServiceId: serviceId,
      selectedService
    })
  },

  onSelectService(event) {
    const serviceId = event.currentTarget.dataset.serviceId
    if (!serviceId || serviceId === this.data.selectedServiceId) {
      return
    }
    this.selectServiceById(serviceId)
  },

  onDateChange(event) {
    this.setData({ appointmentDate: event.detail.value })
  },

  onSlotTap(event) {
    const slot = event.currentTarget.dataset.slot
    if (!slot) {
      return
    }
    this.setData({ appointmentTimeSlot: slot })
  },

  onRemarkInput(event) {
    this.setData({ remark: event.detail.value || "" })
  },

  async submitAdditionalService() {
    if (!getApp().requireFeatureLogin()) {
      return
    }
    if (this.data.submitting) {
      return
    }
    const selectedService = this.data.selectedService
    if (!selectedService?.serviceId) {
      wx.showToast({ title: "请先选择附加服务", icon: "none" })
      return
    }
    if (!this.data.appointmentDate) {
      wx.showToast({ title: "请选择预约日期", icon: "none" })
      return
    }
    if (!this.data.appointmentTimeSlot) {
      wx.showToast({ title: "请选择预约时段", icon: "none" })
      return
    }

    this.setData({ submitting: true })
    try {
      const result = await request({
        url: "/additional-services/orders",
        method: "POST",
        data: {
          serviceId: selectedService.serviceId,
          appointmentDate: this.data.appointmentDate,
          appointmentTimeSlot: this.data.appointmentTimeSlot,
          remark: (this.data.remark || "").trim()
        }
      })
      const order = result?.order || null
      const balance = Number(result?.balance || 0)
      const recentOrders = order ? [order, ...(this.data.recentOrders || [])].slice(0, 3) : this.data.recentOrders
      this.setData({
        points: balance,
        recentOrders,
        remark: ""
      })
      wx.showToast({ title: "预约成功", icon: "success" })
    } catch (error) {
      wx.showToast({ title: error?.message || "预约失败", icon: "none" })
    } finally {
      this.setData({ submitting: false })
    }
  },

  openPointsDetail() {
    if (!getApp().requireFeatureLogin()) {
      return
    }
    wx.navigateTo({ url: "/pages/points/detail/index" })
  },

  focusAdditionalServiceSection() {
    wx.pageScrollTo({
      selector: "#additional-service-section",
      duration: 220
    })
  },

  goPage(event) {
    const url = event.currentTarget.dataset.url
    if (!url) {
      return
    }
    if (!getApp().requireFeatureLogin()) {
      return
    }
    if (url.startsWith("/pages/service/service")) {
      wx.switchTab({ url })
      return
    }
    wx.navigateTo({ url })
  }
})
