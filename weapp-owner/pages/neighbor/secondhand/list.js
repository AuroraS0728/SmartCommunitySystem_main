const { request } = require("../../../api/request")

const CATEGORIES = [
  { label: "全部", value: "" },
  { label: "家电", value: "appliance" },
  { label: "家具", value: "furniture" },
  { label: "母婴", value: "baby" },
  { label: "图书", value: "book" },
  { label: "数码", value: "digital" },
  { label: "其他", value: "other" }
]

const SORT_OPTIONS = [
  { label: "最新发布", value: "" },
  { label: "浏览最多", value: "hot" },
  { label: "价格升序", value: "price_asc" },
  { label: "价格降序", value: "price_desc" }
]

const MINE_STATUS = [
  { label: "全部", value: "", key: "" },
  { label: "在售", value: 1, key: "1" },
  { label: "已售", value: 2, key: "2" },
  { label: "下架", value: 3, key: "3" }
]

const CATEGORY_LABELS = CATEGORIES.reduce((map, item) => {
  map[item.value] = item.label
  return map
}, {})

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

function statusText(status) {
  const map = { 1: "在售", 2: "已售", 3: "下架" }
  return map[Number(status)] || "未知"
}

Page({
  data: {
    categories: CATEGORIES,
    statusOptions: MINE_STATUS,
    sortOptions: SORT_OPTIONS,
    skeletonRows: [1, 2, 3],
    category: "",
    mineStatus: "",
    mineStatusKey: "",
    sortBy: "",
    sortLabel: "最新发布",
    keyword: "",
    mine: false,
    page: 1,
    size: 10,
    total: 0,
    list: [],
    loading: false,
    showSkeleton: true,
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

  onCategoryTap(e) {
    const value = e.currentTarget.dataset.value
    if (value === this.data.category) return
    this.setData({ category: value }, () => this.reload())
  },

  onMineSwitch(e) {
    const mine = !!e.detail.value
    if (mine && !getApp().requireFeatureLogin()) {
      this.setData({ mine: false, mineStatus: "", mineStatusKey: "" })
      return
    }
    this.setData({ mine, mineStatus: "", mineStatusKey: "" }, () => this.reload())
  },

  onMineStatusTap(e) {
    const value = e.currentTarget.dataset.value
    const key = e.currentTarget.dataset.key || ""
    if (key === this.data.mineStatusKey) return
    this.setData({ mineStatus: value, mineStatusKey: key }, () => this.reload())
  },

  onSortTap() {
    wx.showActionSheet({
      itemList: SORT_OPTIONS.map((item) => item.label),
      success: ({ tapIndex }) => {
        const target = SORT_OPTIONS[tapIndex] || SORT_OPTIONS[0]
        this.setData(
          {
            sortBy: target.value,
            sortLabel: target.label
          },
          () => this.reload()
        )
      }
    })
  },

  goPublish() {
    if (!getApp().requireFeatureLogin()) return
    wx.navigateTo({ url: "/pages/neighbor/secondhand/publish" })
  },

  goDetail(e) {
    const id = e.currentTarget.dataset.id
    if (!id) return
    wx.navigateTo({ url: `/pages/neighbor/secondhand/detail?id=${id}` })
  },

  reload() {
    this.setData({
      page: 1,
      total: 0,
      list: [],
      hasMore: true,
      showSkeleton: true
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
        category: this.data.category || undefined,
        keyword: this.data.keyword || undefined,
        sortBy: this.data.sortBy || undefined
      }
      if (this.data.mine && this.data.mineStatus !== "") {
        params.status = Number(this.data.mineStatus)
      }
      const data = await request({
        url: "/neighbor/second-hand/list",
        data: params,
        skipAuth: !this.data.mine
      })
      const items = Array.isArray(data?.items) ? data.items : []
      const mapped = items.map((item) => {
        const imageList = parseImages(item.images)
        const price = Number(item.price || 0)
        return {
          ...item,
          imageList,
          cover: imageList[0] || "",
          categoryLabel: CATEGORY_LABELS[item.category] || "其他",
          statusText: statusText(item.status),
          timeText: formatTime(item.updateTime || item.createTime),
          priceText: price > 0 ? `¥${price.toFixed(2)}` : "面议"
        }
      })
      const nextList = this.data.list.concat(mapped)
      const total = Number(data?.total || 0)
      const page = Number(data?.page || this.data.page)
      const hasMore = nextList.length < total
      this.setData({
        list: nextList,
        total,
        page: page + 1,
        hasMore
      })
    } catch (error) {
      wx.showToast({ title: error?.message || "加载失败", icon: "none" })
    } finally {
      this.setData({ loading: false, showSkeleton: false })
    }
  }
})
