const { request } = require("../../../api/request")

const TYPE_OPTIONS = [
  { label: "全部", value: "" },
  { label: "失物", value: 1 },
  { label: "招领", value: 2 }
]

const STATUS_OPTIONS = [
  { label: "全部状态", value: "" },
  { label: "进行中", value: 1 },
  { label: "已完成", value: 2 }
]

function parseImages(images) {
  if (!images) return []
  if (Array.isArray(images)) return images
  try {
    const parsed = JSON.parse(images)
    return Array.isArray(parsed) ? parsed : []
  } catch (error) {
    return typeof images === "string" ? [images] : []
  }
}

function formatTime(value) {
  if (!value) return "--"
  const text = String(value).replace("T", " ")
  return text.length > 16 ? text.slice(0, 16) : text
}

function typeText(type) {
  return Number(type) === 2 ? "招领" : "失物"
}

function statusText(status) {
  return Number(status) === 2 ? "已完成" : "进行中"
}

Page({
  data: {
    typeOptions: TYPE_OPTIONS,
    statusOptions: STATUS_OPTIONS,
    type: "",
    status: "",
    keyword: "",
    mine: false,
    page: 1,
    size: 10,
    total: 0,
    list: [],
    loading: false,
    hasMore: true
  },

  onShow() {
    this.reload()
  },

  onPullDownRefresh() {
    this.reload().finally(() => wx.stopPullDownRefresh())
  },

  onReachBottom() {
    this.loadList()
  },

  onKeywordInput(e) {
    this.setData({ keyword: (e.detail.value || "").trim() })
  },

  onSearch() {
    this.reload()
  },

  onTypeTap(e) {
    const value = e.currentTarget.dataset.value
    if (String(value) === String(this.data.type)) return
    this.setData({ type: value }, () => this.reload())
  },

  onMineSwitch(e) {
    const mine = !!e.detail.value
    this.setData({ mine, status: "" }, () => this.reload())
  },

  onStatusTap(e) {
    const value = e.currentTarget.dataset.value
    if (String(value) === String(this.data.status)) return
    this.setData({ status: value }, () => this.reload())
  },

  goPublish() {
    wx.navigateTo({ url: "/pages/neighbor/lostfound/publish" })
  },

  goDetail(e) {
    const id = Number(e.currentTarget.dataset.id || 0)
    if (!id) return
    wx.navigateTo({ url: `/pages/neighbor/lostfound/detail?id=${id}` })
  },

  reload() {
    this.setData({
      page: 1,
      total: 0,
      list: [],
      hasMore: true
    })
    return this.loadList(true)
  },

  async loadList(force = false) {
    if (this.data.loading) return
    if (!force && !this.data.hasMore) return
    this.setData({ loading: true })
    try {
      const params = {
        page: this.data.page,
        size: this.data.size,
        mine: this.data.mine,
        keyword: this.data.keyword || undefined,
        type: this.data.type === "" ? undefined : Number(this.data.type)
      }
      if (this.data.mine && this.data.status !== "") {
        params.status = Number(this.data.status)
      }
      const data = await request({
        url: "/neighbor/lost-found/list",
        data: params
      })
      const items = Array.isArray(data?.items) ? data.items : []
      const mapped = items.map((item) => {
        const imageList = parseImages(item.images)
        return {
          ...item,
          imageList,
          cover: imageList[0] || "",
          typeText: typeText(item.type),
          statusText: statusText(item.status),
          timeText: formatTime(item.updateTime || item.createTime)
        }
      })
      const list = this.data.list.concat(mapped)
      const total = Number(data?.total || 0)
      const page = Number(data?.page || this.data.page)
      this.setData({
        list,
        total,
        page: page + 1,
        hasMore: list.length < total
      })
    } catch (error) {
      wx.showToast({ title: error?.message || "加载失败", icon: "none" })
    } finally {
      this.setData({ loading: false })
    }
  }
})
