const { request } = require("../../api/request")

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

Page({
  data: {
    list: [],
    page: 1,
    size: 10,
    total: 0,
    loading: false
  },

  onShow() {
    this.reload()
  },

  onPullDownRefresh() {
    this.reload().finally(() => wx.stopPullDownRefresh())
  },

  onReachBottom() {
    this.loadList(false)
  },

  reload() {
    this.setData({
      list: [],
      page: 1,
      total: 0
    })
    return this.loadList(true)
  },

  async loadList(force) {
    if (this.data.loading) return
    if (!force && this.data.total > 0 && this.data.list.length >= this.data.total) return
    this.setData({ loading: true })
    try {
      const page = force ? 1 : this.data.page
      const data = await request({
        url: "/neighbor/second-hand/favorite/list",
        data: {
          page,
          size: this.data.size
        }
      })
      const items = Array.isArray(data?.items) ? data.items : []
      const mapped = items.map((item) => {
        const imageList = parseImages(item.images)
        const price = Number(item.price || 0)
        return {
          ...item,
          cover: imageList[0] || "",
          priceText: price > 0 ? `${price.toFixed(2)} 元` : "面议",
          timeText: formatTime(item.updateTime || item.createTime)
        }
      })
      const list = force ? mapped : this.data.list.concat(mapped)
      this.setData({
        list,
        total: Number(data?.total || 0),
        page: Number(data?.page || page) + 1
      })
    } catch (error) {
      wx.showToast({ title: error?.message || "加载收藏失败", icon: "none" })
    } finally {
      this.setData({ loading: false })
    }
  },

  goDetail(e) {
    const id = Number(e.currentTarget.dataset.id || 0)
    if (!id) return
    wx.navigateTo({ url: `/pages/neighbor/secondhand/detail?id=${id}` })
  }
})
